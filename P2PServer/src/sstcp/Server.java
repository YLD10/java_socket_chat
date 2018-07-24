package sstcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Server.java
 * 
 * @Author : YLD
 * @EditTime : 2017/12/1 23:30
 * @Mean : 服务端程序。接受客户端的连接请求，并维护所有的客户端连接。
 *
 */
public class Server {
	// 客户端连接维护队列
	public static List<ServerThread>	conns			= new ArrayList<ServerThread>();
	private Socket						socket			= null;							// 客户端socket连接对象（主动请求）
	private ServerSocket				serversocket	= null;							// 服务端socket连接对象（被动接受）

	// 构造函数，另开线程运行程序使得构造函数可以正常结束
	public Server() {
		new Thread() { // 另创建一个线程
			public void run() { // 具体的执行函数
				Run(); // 主要执行函数
			};
		}.start(); // 线程启动
	}

	// 主要执行函数
	public void Run() {
		try {
			// 初始化服务端socket连接，监听端口32768
			setServersocket(new ServerSocket(32768));
			System.out.println("服务端启动成功！");

			while (true) { // 一直循环等待客户端连接的到来
				try {
					setSocket(getServersocket().accept()); // 把得到的客户端连接保存起来
					System.out.println("服务端监听到客户端请求！");

					// 创建针对这一客户端连接的维护线程
					ServerThread sThread = new ServerThread(getSocket());
					sThread.start(); // 启动维护线程
					conns.add(sThread); // 将维护线程添加到维护队列中，便于管理

					System.out.println("当前客户端数量：" + conns.size());
					// 遍历维护队列中的所有维护线程，打印出当前已有的客户端连接
					for (int i = 0; i < conns.size(); i++) {
						System.out.println("客户端的IP：" + conns.get(i).getSocket().getInetAddress().getHostAddress()
								+ " ，端口：" + conns.get(i).getSocket().getPort());
					}
					System.out.println();
				} catch (IOException e) {
					// e.printStackTrace();
					System.out.println("服务端连接失败！");
				}
			}
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("服务端启动失败！");
		} finally {
			try {
				// 如果服务端socket连接对象不为空
				if (getServersocket() != null) {
					getServersocket().close(); // 关闭服务端连接对象
					System.out.println("成功关闭服务器！");
				}
			} catch (IOException e) {
				// e.printStackTrace();
				System.out.println("关闭服务器失败！");
			}
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public ServerSocket getServersocket() {
		return serversocket;
	}

	public void setServersocket(ServerSocket serversocket) {
		this.serversocket = serversocket;
	}
}
