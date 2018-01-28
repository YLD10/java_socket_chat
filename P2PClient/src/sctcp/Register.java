package sctcp;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Register.java
 *
 * @Author : YLD
 * @EditTime : 2018/01/26 17:25
 * @Mean : 客户端的登录界面，用于接收用户输入的昵称
 *
 */
public class Register extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L; // 确保对象的序列与反序列正确的版本号校验
														// 然而我就没用到这个
	
	private JPanel panel = null; // 主窗口面板，包含所有控件
	private JButton button = null; // 登录按钮
	private JTextField textField = null; // 昵称输入文本框
	private JLabel labName = null; // 显示标签

	// 构造函数，初始化整个显示窗口
	public Register() {
		// 可以获取到显示器屏幕大小的对象
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setTitle("Socket"); // 设置主窗口的标题，标题位于主窗口的左上角
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置主窗口的关闭事件为正常退出
		// 设置主窗口在屏幕的中间位置出现(距屏幕坐标x,y和主窗口大小width:400,height:180)
		this.setBounds((int) (screensize.getWidth() / 2 - 200), (int) (screensize.getHeight() / 2 - 90), 400, 180);
		
		setPanel(new JPanel()); // 初始化主面板
		getPanel().setBorder(new EmptyBorder(5, 5, 5, 5)); // 设置各控件与边界的距离
		this.setContentPane(getPanel()); // 初始化主面板布局
		getPanel().setLayout(null); // 设置主面板为主窗口的内容面板

		setLabName(new JLabel("昵称：")); // 初始化显示标签
		setTextField(new JTextField()); // 初始化文本输入框
		setButton(new JButton("登录")); // 初始化登录按钮

		// 设置显示标签的位置(距主面板坐标x,y)与大小(宽，高)
		getLabName().setBounds(20, 50, 60, 30);
		// 设置文本输入框的位置(距主面板坐标x,y)与大小(宽，高)
		getTextField().setBounds(80, 50, 150, 30);
		// 设置登录按钮的位置(距主面板坐标x,y)与大小(宽，高)
		getButton().setBounds(250, 50, 100, 30);
		getLabName().setFont(new Font("楷体", 1, 18)); // 设置显示标签的字体
		
		getButton().addActionListener(this); // 发送按钮监听主窗口动作事件，这是前提
		
		// 设置主窗口的默认按钮是发送按钮，即点击发送按钮会触发主窗口的动作事件actionPerformed，这是后续
		this.getRootPane().setDefaultButton(getButton());
		
		getPanel().add(getLabName()); // 将显示标签添加到主面板中
		getPanel().add(getTextField()); // 将文本输入框添加到主面板中
		getPanel().add(getButton()); // 将登录按钮添加到主面板中

		this.setResizable(false); // 设置主窗口大小不可调整
		this.setVisible(true); // 设置主窗口可视
		System.out.println("程序启动！");
	}

	// 主窗口动作事件
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// 得到文本输入框的文本，去掉首尾空格
		String name = getTextField().getText().trim();
		
		if (name.isEmpty()) { // 如果没有输入任何内容
			// 弹出提示
			JOptionPane.showMessageDialog(null, "昵称不能为空！", "警告", JOptionPane.ERROR_MESSAGE);
		} else { // 如果有输入内容
			try {
				// 如果昵称的字符长度大于20
				if (name.getBytes("GB2312").length > 20) {
					// 弹出提示
					JOptionPane.showMessageDialog(null, "昵称长度应在20以内，一个中文长度为2！", "警告", JOptionPane.ERROR_MESSAGE);
				} else { // 如果昵称长度小于等于20
					new Thread() { // 另创建一个线程
						public void run() { // 具体的执行函数
							new Client(name); // 打开客户端主界面，并传递用户昵称
						};
					}.start(); // 线程启动
					
					this.dispose(); // 主窗口强制销毁
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public JButton getButton() {
		return button;
	}

	public void setButton(JButton button) {
		this.button = button;
	}

	public JTextField getTextField() {
		return textField;
	}

	public void setTextField(JTextField textField) {
		this.textField = textField;
	}

	public JLabel getLabName() {
		return labName;
	}

	public void setLabName(JLabel labName) {
		this.labName = labName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
