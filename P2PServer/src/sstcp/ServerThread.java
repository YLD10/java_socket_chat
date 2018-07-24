package sstcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ServerThread.java
 * 
 * @Author : YLD
 * @EditTime : 2017/12/1 23:32
 * @Mean: 专门负责为某一位用户处理消息并进行转发的一个服务线程。 即把这一位用户发送的消息转发给指定的收信人。
 * 
 */
public class ServerThread extends Thread {
	private String			loginName		= null;		// 登录昵称，为用户唯一标识符
	private Socket			socket			= null;		// socket对象
	private BufferedReader	br				= null;		// 数据输入流
	private PrintWriter		pw				= null;		// 数据输出流
	private Long			lastChatTime	= null;		// 最近的对话时间
	private boolean			isInLine		= true;		// 是否在线标志位，默认在线
	private boolean			isCanRemove		= false;	// socket是否可以移除标志位，默认不可移除

	// 构造函数，初始化socket对象和最近对话时间
	public ServerThread(Socket socket) throws IOException {
		setSocket(socket);
		setLastChatTime(System.currentTimeMillis());
	}

	// 线程的执行函数，线程构造完成后启动时默认执行此函数
	public void run() {
		System.out.println(
				"\n新连接，客户端的IP：" + getSocket().getInetAddress().getHostAddress() + " ，端口：" + getSocket().getPort());

		while (true) { // 循环等待消息
			StringBuffer sBuffer = new StringBuffer(); // 消息载体
			boolean flag = false; // 判断是否为对话数据类型的消息，默认不是对话，
									// 而是建立/断开连接或者列表更新的消息

			try {
				// 将得到的socket输入流转换为封装后的缓冲输入流并赋值给br
				setBr(new BufferedReader(new InputStreamReader(getSocket().getInputStream())));
				String str = ""; // 暂存一行消息的载体
				System.out.println("Before Read");

				while (true) { // 循环接收消息直至一则消息接收完毕
					str = getBr().readLine(); // 读取消息的一行，会把换行符去除
					if (str.length() > 6) { // 此行消息长度大于6，则有可能是消息首部或者消息尾部
						if (str.length() > 7) { // 且此行消息长度大于7
							// 如果此行消息前8个字符是"!@#$%^&*"
							if (str.substring(0, 8).equals("!@#$%^&*")) {
								flag = true; // 则说明这一行是对话数据的消息首部，
												// 消息首部会被保留下来，
												// 在match函数里才会被丢弃
							}

							// 此行消息长度等于7
							// 如果此行消息前7个字符是"@#end#@"
						} else if (str.substring(0, 7).equals("@#end#@")) {
							break; // 则说明这一行是这则消息的尾部，消息已经全部接收完毕，
									// 应该跳出接收消息的循环，但不跳出等待消息的循环
									// 消息尾部在此处被丢掉了
						}
					}
					sBuffer.append(str); // 将接收到的一行消息拼接到已接收到的消息内容的尾部
					if (flag) { // 如果是对话数据类型的一行消息
						sBuffer.append("\n"); // 则还应该补上这一行末尾的换行符
					}
				}

				System.out.println("After Read");
				if (flag) { // 如果是对话数据类型的消息，flag为true
					System.out.println("match");
					match(sBuffer); // 为已接收到的对话消息匹配指定收信人并进行转发

					// 如果已接收到的消息首部前8个字符是"}{|\":?><"，则说明此消息为请求更新列表的消息
				} else if (sBuffer.substring(0, 8).equals("}{|\":?><")) {
					System.out.println("update");
					updatePerson(); // 更新会话对象列表

					// 如果已接收到的消息首部前8个字符是")(*&^%$#"，则说明此消息为请求建立连接的消息
				} else if (sBuffer.substring(0, 8).equals(")(*&^%$#")) {
					// 把此建立连接的消息首部从第8个字符起到出现"<>?:\"|{}"为止之间的字符串拿出来作为用户登录昵称
					setLoginName(sBuffer.substring(8, sBuffer.indexOf("<>?:\"|{}")));
					System.out.println("昵称：" + getLoginName());
					updateAllPerson(); // 有一位新用户登录了，所有用户的会话对象列表都要进行更新

					// 如果已接收到的消息首部前8个字符是"!!@@##$$"，则说明此消息为请求断开连接的消息
				} else if (sBuffer.substring(0, 8).equals("!!@@##$$")) {
					System.out.println("disconnect");
					break; // 跳出等待消息的循环，即准备断开连接，关闭socket连接
				}
			} catch (Exception e) { // 不知道为啥，就算没有socket连接过来，
									// 服务端还是会时不时收到陌生的socket连接，
									// 然后过了5s就会抛出消息读取异常
				// e.printStackTrace();
				System.out.println("出现未知错误，线程强制关闭！");
				break; // 此时也应该跳出等待消息的循环，关闭这个错误的连接
			}
		}

		Server.conns.remove(this); // 将本线程从服务线程队列中移除，即没法再找到本线程
		updateAllPerson(); // 有一位用户退出了，所有用户的会话对象列表都要进行更新
		try {
			sendClose(); // 回送关闭消息，通知客户端可以断开连接了
			getPw().close(); // 关闭输出流
			getBr().close(); // 关闭输入流
			getSocket().close(); // 关闭socket，断开服务端这边的连接
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 做好一些善后工作后，本服务线程的执行函数结束，进程也就随着自然消亡
	}

	// 收信人匹配函数
	private void match(StringBuffer sBuffer) {
		// 计数器，收信人昵称与群发标志位的拼接字符串的结束下标
		// "<>?:\"|{}"在这里起的作用与建立连接信息中的作用有所不同
		// 在这里不仅定界收信人昵称，同时也定界群发标志位
		int i = 0, end = sBuffer.indexOf("<>?:\"|{}");

		String addName = sBuffer.substring(8, end - 1); // 收信人昵称,8起始是为跳过消息首部
		String isMulti = sBuffer.substring(end - 1, end); // 是否群发的标志位
		System.out.println(isMulti.equals("1") ? "群发" : "单聊"); // “1”为群发，“0”为单聊

		// 遍历所有服务进程，匹配收信人所在的服务进程，把对话消息交与此服务进程
		for (; i < Server.conns.size(); ++i) {
			String senName = getLoginName(); // 发信人的昵称，默认为本服务线程的用户昵称

			if (isMulti.equals("0")) { // 单聊
				// 如果遍历到的服务进程的用户在线且此用户就是收信人
				if (Server.conns.get(i).isInLine() && Server.conns.get(i).getLoginName().equals(addName)) {
					if (addName.equals(senName)) { // 如果收信人就是发信人自己
						senName = "自己"; // 那么发信人一栏应该显示“自己”
					}
					System.out.println("send to " + addName);
					// end+8为跳过"<>?:\"|{}"，在此之后就是对话的真正内容
					System.out.println(sBuffer.substring(end + 8));
					// 为真正的对话内容加上首部信息，并调用收信人的服务进程中的消息发送函数sendMsg
					// 只有收信人的服务进程才能将消息通过socket发送给收信人的客户端
					Server.conns.get(i).sendMsg("@#data#@" + senName + "<>?:\"|{}" + sBuffer.substring(end + 8));
					break; // 跳出遍历，结束match函数
				}
			} else { // 群发
				// 如果遍历到的服务进程的用户在线
				if (Server.conns.get(i).isInLine()) {
					// 且此用户是发信人自己
					if (Server.conns.get(i).getLoginName().equals(senName)) {
						System.out.println("Not send to myself"); // 群发消息不发给自己
						continue; // 不往下走，直接到遍历到下一个用户服务进程
					}
					System.out.println("send to " + Server.conns.get(i).getLoginName());
					// end+8为跳过"<>?:\"|{}"，在此之后就是对话的真正内容
					System.out.println(sBuffer.substring(end + 8));
					// 为真正的对话内容加上首部信息，并此用户的服务进程中的消息发送函数sendMsg
					// 只有此用户的服务进程才能将消息通过socket发送给此用户的客户端
					Server.conns.get(i).sendMsg("@#data#@" + senName + "<>?:\"|{}" + sBuffer.substring(end + 8));
				}
			}
		}
	}

	// 消息发送函数，通过socket发送消息给客户端
	// msg为消息首部加上消息真正内容
	public void sendMsg(String msg) {
		try {
			// System.out.println(msg);
			// 将得到的socket输出流转换为封装后的打印输出流并赋值给pw
			setPw(new PrintWriter(new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8")),
					true));
			getPw().println(msg + "\n@#end#@"); // 给消息加上尾部信息后放进输出流
			getPw().flush(); // 输出流刷新缓冲区，将里面的消息发送出去
			setLastChatTime(System.currentTimeMillis()); // 设置最近对话时间
			System.out.println("Successful send");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 更新会话对象列表
	public void updatePerson() {
		// 消息载体，初始化为列表更新请求消息的消息首部"@#person#@"
		StringBuffer sBuffer = new StringBuffer("@#person#@");
		boolean flag = false; // 判断是否有在线用户的标志位，默认没有用户在线

		// 遍历所有服务进程
		for (int i = 0; i < Server.conns.size(); ++i) {
			// 如果遍历到的服务进程的用户在线
			if (Server.conns.get(i).isInLine()) {
				flag = true; // 那就确实有用户在线
				// 在线用户之间的昵称用"@##@"隔开并放到消息内容中
				sBuffer.append(Server.conns.get(i).getLoginName() + "@##@");
			}
		}
		if (flag) { // 有在线用户
			sendMsg(sBuffer.toString()); // 向本线程的用户即自己的客户端发送更新后的会话对象列表消息
		}
	}

	// 更新所有用户的会话对象列表
	private void updateAllPerson() {
		// 遍历所有服务进程，并调用其会话对象列表更新函数
		for (int i = 0; i < Server.conns.size(); ++i) {
			Server.conns.get(i).updatePerson();
		}
	}

	// 回送关闭消息
	private void sendClose() throws IOException {
		System.out.println("服务端回送关闭命令中...");
		// 将得到的socket输出流转换为封装后的打印输出流并赋值给pw
		setPw(new PrintWriter(new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8")),
				true));
		// 给消息加上首部和尾部信息后放进输出流
		// 关闭通知消息没有消息内容，只有首尾部
		getPw().println("!!@@##$$" + "\n@#end#@");
		getPw().flush(); // 输出流刷新缓冲区，将里面的消息发送出去
		System.out.println("关闭命令回送成功！\n");
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public BufferedReader getBr() {
		return br;
	}

	public void setBr(BufferedReader br) {
		this.br = br;
	}

	public PrintWriter getPw() {
		return pw;
	}

	public void setPw(PrintWriter pw) {
		this.pw = pw;
	}

	public Long getLastChatTime() {
		return lastChatTime;
	}

	public void setLastChatTime(Long lastChatTime) {
		this.lastChatTime = lastChatTime;
	}

	public boolean isInLine() {
		return isInLine;
	}

	public void setInLine(boolean isInLine) {
		this.isInLine = isInLine;
	}

	public boolean isCanRemove() {
		return isCanRemove;
	}

	public void setCanRemove(boolean isCanRemove) {
		this.isCanRemove = isCanRemove;
	}
}
