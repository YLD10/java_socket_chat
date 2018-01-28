package sctcp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 * Client.java
 * 
 * @Author : YLD
 * @EditTime : 2017/12/2 00:07
 * @Mean: 某一位用户的客户端主程序，主要负责接收服务端发来的消息并进行处理，之后呈现给用户
 * 
 **/
public class Client extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L; // 确保对象的序列与反序列正确的版本号校验
														// 然而我就没用到这个
	private JPanel panel = null; // 主窗口面板，包含所有控件
	private JScrollPane scrollPaneManifest = null; // 对话显示窗口的滚动面板
	private JTextPane textManifest = null; // 对话显示窗口的文本显示控件
	private JScrollPane scrollPaneInput = null; // 文本输入窗口的滚动面板
	private JTextArea textInput = null; // 文本输入窗口的文本输入控件
	private JButton senButton = null; // 发送按钮
	private JLabel labPerson = null; // 会话对象列表的提示标签
	private JComboBox<String> comPerson = null; // 显示会话对象列表下拉框
	private ButtonGroup bGroup = null; // 单聊和群发的单选框组
	private JRadioButton rSButton = null; // 单聊单选框
	private JRadioButton rMButton = null; // 群发单选框

	private Socket socket = null; // socket对象
	private BufferedReader br = null; // 数据输入流
	private PrintWriter pw = null; // 数据输出流

	// 构造函数，初始化整个显示窗口以及建立socket连接
	public Client(String loginName) {
		// 可以获取到显示器屏幕大小的对象
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setTitle(loginName); // 设置主窗口的标题，标题位于主窗口的左上角
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置主窗口的关闭事件为正常退出
		// 设置主窗口在屏幕的中间位置出现(距屏幕坐标x,y和主窗口大小width:370,height:600)
		this.setBounds((int) (screensize.getWidth() / 2 - 185), (int) (screensize.getHeight() / 2 - 300), 370, 600);

		setPanel(new JPanel()); // 初始化主面板
		getPanel().setBorder(new EmptyBorder(5, 5, 5, 5)); // 设置各控件与边界的距离
		getPanel().setLayout(null); // 初始化主面板布局
		this.setContentPane(getPanel()); // 设置主面板为主窗口的内容面板

		setTextManifest(new JTextPane()); // 初始化文本显示控件
		// 添加文本显示控件到对话窗口的滚动面板中
		setScrollPaneManifest(new JScrollPane(getTextManifest()));
		setTextInput(new JTextArea()); // 初始化文本输入控件
		// 添加文本输入控件到输入窗口的滚动面板中
		setScrollPaneInput(new JScrollPane(getTextInput()));
		setSenButton(new JButton("发送")); // 初始化发送按钮
		setLabPerson(new JLabel("请选择会话对象：")); // 初始化会话对象列表提示标签
		setComPerson(new JComboBox<String>()); // 初始化会话对象列表下拉框
		setbGroup(new ButtonGroup()); // 初始化单聊和群发的单选框组
		setrSButton(new JRadioButton("单聊", true)); // 初始化单聊单选框，默认选中
		setrMButton(new JRadioButton("群发")); // 初始化群发单选框

		// 获得文本显示控件的一个空文本样式对象
		Style def = getTextManifest().getStyledDocument().addStyle(null, null);
		StyleConstants.setFontFamily(def, "verdana"); // 设置文本为vardana字体
		StyleConstants.setFontSize(def, 12); // 设置文本大小为12
		// 将样式def重命名为normal，并添加到文本显示控件中
		Style normal = getTextManifest().addStyle("normal", def);
		// 将normal拷贝一份命名为green，并添加到文本显示控件中
		Style g = getTextManifest().addStyle("green", normal);
		// 将normal拷贝一份命名为blue，并添加到文本显示控件中
		Style b = getTextManifest().addStyle("blue", normal);
		// 设置样式g的字体颜色RGB
		StyleConstants.setForeground(g, new Color(0, 180, 64));
		// 设置样式b的字体颜色RGB
		StyleConstants.setForeground(b, new Color(42, 0, 255));
		// 设置文本显示的段落样式为normal并覆盖原有的样式
		getTextManifest().setParagraphAttributes(normal, true);
		// 设置文本显示的背景颜色
		getTextManifest().setBackground(new Color(230, 231, 235));
		getLabPerson().setFont(new Font("楷体", 1, 18)); // 设置会话对象列表提示标签的字体

		// 设置对话显示窗口的滚动面板的位置(距主面板坐标x,y)与大小(宽，高)
		getScrollPaneManifest().setBounds(47, 30, 270, 300);
		// 设置文本输入窗口的滚动面板的位置(距主面板坐标x,y)与大小(宽，高)
		getScrollPaneInput().setBounds(30, 400, 200, 25);
		// 设置发送按钮的位置(距主面板坐标x,y)与大小(宽，高)
		getSenButton().setBounds(250, 400, 80, 25);
		// 设置会话对象列表提示标签的位置(距主面板坐标x,y)与大小(宽，高)
		getLabPerson().setBounds(30, 450, 200, 25);
		// 设置会话对象列表下拉框的位置(距主面板坐标x,y)与大小(宽，高)
		getComPerson().setBounds(30, 480, 150, 25);
		// 设置单聊单选框的位置(距主面板坐标x,y)与大小(宽，高)
		getrSButton().setBounds(220, 480, 60, 25);
		// 设置群发单选框的位置(距主面板坐标x,y)与大小(宽，高)
		getrMButton().setBounds(280, 480, 60, 25);
		getbGroup().add(getrSButton()); // 将单聊单选框添加到单选框组中
		getbGroup().add(getrMButton()); // 将群发单选框添加到单选框组中

		getTextManifest().setEditable(false); // 设置文本显示窗口不可编辑
		getTextInput().setLineWrap(true); // 设置文本输入窗口可以自动换行

		getSenButton().addActionListener(this); // 发送按钮监听主窗口动作事件，这是前提

		// 设置主窗口的默认按钮是发送按钮，即点击发送按钮会触发主窗口的动作事件actionPerformed，这是后续
		this.getRootPane().setDefaultButton(getSenButton());

		// 文本输入控件监听键盘事件
		getTextInput().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) { // 键盘按下事件
				if (e.getKeyCode() == KeyEvent.VK_ENTER) { // 如果按的是回车键
					new Thread() { // 另创建一个线程
						public void run() { // 具体的执行函数
							sendMsg(); // 要发送消息
						};
					}.start(); // 线程启动
				}
			}
		});

		getPanel().add(getScrollPaneManifest()); // 将对话显示窗口的滚动面板添加到主面板中
		getPanel().add(getScrollPaneInput()); // 将文本输入窗口的滚动面板添加到主面板中
		getPanel().add(getSenButton()); // 将发送按钮添加到主面板中
		getPanel().add(getLabPerson()); // 将会话对象列表提示标签添加到主面板中
		getPanel().add(getComPerson()); // 将会话对象列表下拉框添加到主面板中
		getPanel().add(getrSButton()); // 将单聊单选框添加到主面板中
		getPanel().add(getrMButton()); // 将群发单选框添加到主面板中

		try {
			// 初始化socket对象，ip:60.205.184.5，端口:32768
			setSocket(new Socket("60.205.184.5", 32768));
			getSocket().setReuseAddress(true); // 允许开多个同ip不同port的socket

			sendLoginName(); // 发送登录昵称给服务端
			this.setResizable(false); // 设置主窗口大小不可调整
			this.setVisible(true); // 设置主窗口可视
			System.out.println("客户端打开成功！");

			new Thread() { // 创建一个线程
				public void run() { // 具体的执行函数
					try {
						getDataServer(); // 启动监听消息函数
					} catch (IOException e) {
						// e.printStackTrace();
						System.out.println("连接已关闭，无法接收消息！");
					}
				};
			}.start(); // 启动线程

			// 主窗口添加窗口事件
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) { // 窗口关闭时
					super.windowClosing(e);
					close(); // 关闭处理函数
				}
			});
		} catch (Exception e) {
			// e.printStackTrace();
			// 没有网络时弹窗提示
			JOptionPane.showMessageDialog(null, "电波无法到达...", "警告", JOptionPane.ERROR_MESSAGE);
			new Register(); // 重启注册界面
			this.dispose(); // 主窗口强制销毁
		}
	}

	// 发送登录昵称给服务端
	private void sendLoginName() {
		System.out.println("客户端注册中...");
		System.out.println("客户端更新会话对象中...");
		try {
			// 将得到的socket输出流转换为封装后的打印输出流并赋值给pw
			setPw(new PrintWriter(new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8")),
					true));
			// 给消息加上头部和尾部信息后放进输出流，getTitle得到的是登录昵称。即：头部开始定界符+昵称+头部结束定界符+尾部定界符
			getPw().println(")(*&^%$#" + this.getTitle() + "<>?:\"|{}" + "\n@#end#@");
			getPw().flush(); // 输出流刷新缓冲区，将里面的消息发送出去
			System.out.println("客户端注册成功！");
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("客户端注册失败！");
		}
	}

	/*
	 * // 手动更新会话对象列表 private void getLoginName() {
	 * System.out.println("客户端更新会话对象中..."); try { setPw(new PrintWriter(new
	 * BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(),
	 * "UTF-8")), true)); getPw().println("}{|\":?><" + "\n@#end#@");
	 * getPw().flush(); getDataServer(); } catch (Exception e) {
	 * e.printStackTrace(); System.out.println("会话对象更新失败！"); } }
	 */

	// 循环监听消息的一个主函数
	private void getDataServer() throws IOException {
		while (true) { // 循环等待消息
			StringBuffer sBuffer = new StringBuffer(); // 消息载体
			// 将得到的socket输入流转换为封装后的缓冲输入流并赋值给br，而且是utf-8格式
			setBr(new BufferedReader(new InputStreamReader(getSocket().getInputStream(), "UTF-8")));
			String str = ""; // 暂存一行消息的载体
			boolean flag = false; // 判断是否为对话数据类型的消息，默认不是对话，
									// 而是建立/断开连接或者列表更新的消息

			while (true) { // 循环接收消息直至一则消息接收完毕
				str = getBr().readLine(); // 读取消息的一行，会把换行符去除
				if (str.length() > 6) { // 此行消息长度大于6，则有可能是消息首部或者消息尾部
					if (str.length() > 7) { // 且此行消息长度大于7
						// 如果此行消息前8个字符是"@#data#@"
						if (str.substring(0, 8).equals("@#data#@")) {
							flag = true; // 则说明这一行是对话数据的消息首部，
											// 消息首部会被保留下来，
											// 在下面设message变量的时候才会被丢弃
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

			if (flag) { // 如果是对话数据类型的消息，flag为true
				int end = sBuffer.indexOf("<>?:\"|{}"); // 发信人昵称字符串的结束下标
				String senName = sBuffer.substring(8, end); // 发信人昵称,8起始是为跳过消息首部
				// end+8为跳过"<>?:\"|{}"，在此之后就是对话的真正内容，尾部在之前被丢弃了
				String message = sBuffer.substring(end + 8);
				// 为调试程序而设置的打印消息的一个变量
				StringBuffer sb = new StringBuffer("消息接收成功！消息如下：\n\n==startMessage==\n发信人：");

				sb.append(senName); // 拼接发信人昵称
				sb.append("\n消息内容：\n");
				sb.append(message); // 拼接对话内容
				sb.append("===endMessage===\n");
				System.out.println(sb.toString()); // 打印出调试结果

				sb = new StringBuffer(); // 重复利用sb，存放对话显示窗口中的一条对话数据
				sb.append(senName); // 拼接发信人昵称
				sb.append(" "); // 拼接空格
				// 拼接对话日期
				sb.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
				sb.append("\n"); // 拼接换行

				try {
					// 得到当前对话显示窗口的字符串总长度
					int width = getTextManifest().getDocument().getLength();
					// 在当前窗口显示的文本后面拼接 发信人+对话日期 ，设置拼接的字符串为"blue"格式
					getTextManifest().getDocument().insertString(width, sb.toString(),
							getTextManifest().getStyle("blue"));
					// getTextManifest().getDocument().insertString(width, ">>"
					// + message.toString(),
					// getTextManifest().getStyle("normal"));
					// 根据对话显示窗口的大小适当调整要显示的消息，并自动换行
					textPaneLine(">>" + message.toString());

				} catch (Exception e) {
					e.printStackTrace();
				}

				// 不是对话数据，那如果消息长度大于9且消息前10个字符是"@#person#@"，则说明此消息是更新会话对象列表
			} else if (sBuffer.length() > 9 && sBuffer.substring(0, 10).equals("@#person#@")) {
				// 得到当前下拉框所选择的会话对象的昵称
				String sel = String.valueOf(getComPerson().getSelectedItem());
				// 删除掉下拉框中的所有选项
				getComPerson().removeAllItems();
				// 把得到的更新会话对象的消息从第10个字符后开始按"@##@"分割，得到一组昵称
				String name[] = sBuffer.substring(10).split("@##@");

				for (String loginName : name) { // 遍历得到的这一组昵称
					// 如果遍历到的当前昵称跟之前下拉框所选择的会话对象昵称相同
					if (loginName.equals(sel)) {
						// 本来flag为false，这里置为true。表示这新的一组昵称里面有之前选中的会话对象
						flag = true;
					}
					// 把昵称添加到会话对象列表的下拉框选项中
					getComPerson().addItem(loginName);
				}
				System.out.println("会话对象更新成功！");
				if (flag) { // 为true则表明此前选择的会话对象在新的会话对象列表里依然存在
					getComPerson().setSelectedItem(sel); // 那么继续选中该会话对象
				}

				// 如果已接收到的消息首部前8个字符是"!!@@##$$"，则说明此消息为请求断开连接的消息
			} else if (sBuffer.substring(0, 8).equals("!!@@##$$")) { // 断开连接
				System.out.println("断开连接中...");
				break; // 跳出等待消息的循环，即准备断开连接，关闭socket连接，该客户端也即将关闭
			}
		}
	}

	// 消息发送函数
	private void sendMsg() {
		// 得到文本输入框的文本，去掉首尾空格
		String message = getTextInput().getText().trim();
		System.out.println(message.length());
		
		if (message.isEmpty()) { // 如果没有输入任何内容
			// 弹出提示
			JOptionPane.showMessageDialog(null, "发送内容不能为空！", "警告", JOptionPane.ERROR_MESSAGE);
		} else { // 如果有输入内容
			if (getSocket() != null) { // socket没断开的话
				try {
					// 对话显示窗口中自己发的消息记录
					StringBuffer sb = new StringBuffer();
					sb.append("自己 "); // 发信人应该是自己
					// 加上对话日期
					sb.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
					sb.append("\n"); // 换行开始消息内容

					try {
						// 得到当前对话显示窗口的字符串总长度
						int width = getTextManifest().getDocument().getLength();
						// 在当前窗口显示的文本后面拼接 "自己"+对话日期 ，设置拼接的字符串为"green"格式
						getTextManifest().getDocument().insertString(width, sb.toString(),
								getTextManifest().getStyle("green"));
						// getTextManifest().getDocument().insertString(width,
						// ">>"
						// + message.toString(),
						// getTextManifest().getStyle("normal"));
						// 根据对话显示窗口的大小适当调整要显示的消息，并自动换行
						textPaneLine(">>" + message.toString() + "\n");

					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("\n客户端发送消息中...");
					// 将得到的socket输出流转换为封装后的打印输出流并赋值给pw
					setPw(new PrintWriter(
							new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8")), true));
					// 给消息加上首部和尾部信息后放进输出流
					getPw().println("!@#$%^&*" + getComPerson().getSelectedItem().toString()
							+ (getrMButton().isSelected() ? "1" : "0") + "<>?:\"|{}" + message + "\n@#end#@");
					getPw().flush(); // 输出流刷新缓冲区，将里面的消息发送出去
					System.out.println("消息发送成功！\n");

					getTextInput().setText(""); // 文本输入框内容清空
				} catch (IOException e) {
					// e.printStackTrace();
					System.out.println("消息发送失败！");
				}
			} else {
				System.out.println("客户端连接失败！");
			}
		}
	}

	// 文本调整函数，根据窗口大小调整。被异步调用
	private synchronized void textPaneLine(String text) {
		// 得到文本显示窗口的文档格式操作对象
		Document doc = getTextManifest().getStyledDocument();
		// 得到文本显示窗口的字体规格操作对象
		FontMetrics fm = getTextManifest().getFontMetrics(getTextManifest().getFont());
		int paneWidth = getTextManifest().getWidth();// 得到文本显示窗口的宽度

		try {
			// 遍历整条消息的每个字符，加入适当的换行符实现适应窗口大小的调整
			for (int i = 0, cnt = 0; i < text.length(); ++i) {
				// 将当前遍历到的字符宽度累加起来与窗口大小相比较，如果过长了，则应该加上换行
				if ((cnt += fm.charWidth(text.charAt(i))) >= paneWidth - 50) {
					cnt = 0; // 字符宽度累加器重置
					// 在当前窗口显示的文本后面拼接换行符 ，设置拼接的字符串为"normal"格式
					doc.insertString(doc.getLength(), "\n", getTextManifest().getStyle("normal"));
				}
				// 在当前窗口显示的文本后面拼接当前遍历到的字符 ，设置拼接的字符串为"normal"格式
				doc.insertString(doc.getLength(), String.valueOf(text.charAt(i)), getTextManifest().getStyle("normal"));
			}
			// 在当前窗口显示的文本后面拼接 换行符 ，设置拼接的字符串为"normal"格式
			doc.insertString(doc.getLength(), "\n", getTextManifest().getStyle("normal"));

			// 滚动条到滚动到文本最底端
			getTextManifest().setCaretPosition(doc.getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 主窗口动作事件
	@Override
	public void actionPerformed(ActionEvent arg0) {
		new Thread() { // 另创建一个线程
			public void run() { // 具体的执行函数
				sendMsg(); // 要发送消息
			};
		}.start(); // 线程启动
	}

	// 发送关闭消息函数
	private void sendClose() throws IOException {
		System.out.println("客户端发送关闭命令中...");
		// 将得到的socket输出流转换为封装后的打印输出流并赋值给pw
		setPw(new PrintWriter(new BufferedWriter(new OutputStreamWriter(getSocket().getOutputStream(), "UTF-8")),
				true));
		// 给消息加上首部和尾部信息后放进输出流
		// 关闭通知消息没有消息内容，只有首尾部
		getPw().println("!!@@##$$" + "\n@#end#@");
		getPw().flush(); // 输出流刷新缓冲区，将里面的消息发送出去
		System.out.println("关闭命令发送成功！\n");
	}

	// 连接关闭函数
	public void close() {
		System.out.println("\n窗口关闭");
		try {
			sendClose(); // 发送关闭消息，通知服务端要断开连接了
		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("关闭命令发送失败！\n");
		}
		new Thread() { // 创建一个线程
			public void run() { // 具体的执行函数
				try {
					sleep(500); // 先等待500毫秒，等关闭消息回送完毕
					getPw().close(); // 关闭输出流
					getBr().close(); // 关闭输入流
					getSocket().close(); // 关闭socket，断开客户端这边的连接
					System.out.println("连接关闭成功！\n");
					System.exit(0); // 正常退出
				} catch (Exception e) {
					// e.printStackTrace();
					System.out.println("连接关闭失败！");
				}
			};
		}.start(); // 线程启动
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public JScrollPane getScrollPaneManifest() {
		return scrollPaneManifest;
	}

	public void setScrollPaneManifest(JScrollPane scrollPaneManifest) {
		this.scrollPaneManifest = scrollPaneManifest;
	}

	public JTextPane getTextManifest() {
		return textManifest;
	}

	public void setTextManifest(JTextPane textManifest) {
		this.textManifest = textManifest;
	}

	public JScrollPane getScrollPaneInput() {
		return scrollPaneInput;
	}

	public void setScrollPaneInput(JScrollPane scrollPaneInput) {
		this.scrollPaneInput = scrollPaneInput;
	}

	public JTextArea getTextInput() {
		return textInput;
	}

	public void setTextInput(JTextArea textInput) {
		this.textInput = textInput;
	}

	public JButton getSenButton() {
		return senButton;
	}

	public void setSenButton(JButton button) {
		this.senButton = button;
	}

	public JLabel getLabPerson() {
		return labPerson;
	}

	public void setLabPerson(JLabel labPerson) {
		this.labPerson = labPerson;
	}

	public JComboBox<String> getComPerson() {
		return comPerson;
	}

	public void setComPerson(JComboBox<String> comPerson) {
		this.comPerson = comPerson;
	}

	public ButtonGroup getbGroup() {
		return bGroup;
	}

	public void setbGroup(ButtonGroup bGroup) {
		this.bGroup = bGroup;
	}

	public JRadioButton getrSButton() {
		return rSButton;
	}

	public void setrSButton(JRadioButton rSButton) {
		this.rSButton = rSButton;
	}

	public JRadioButton getrMButton() {
		return rMButton;
	}

	public void setrMButton(JRadioButton rMButton) {
		this.rMButton = rMButton;
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
}
