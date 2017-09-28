package com.liyiyue;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import com.liyiyue.util.FontUtil;

/**
 * @author liyiyue
 * @date 2017年9月6日下午5:19:28
 * @desc 导出进度面板
 */
public class ProgressWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	public JProgressBar progressBar;

	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// public void run() {
	// try {
	// // 初始化进度条
	// ProgressWindow progressWindow = new ProgressWindow();
	// progressWindow.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// }

	public ProgressWindow() throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			FontUtil.setGlobalFonts(new Font("微软雅黑", Font.PLAIN, 12));
		} catch (Exception e) {
			e.printStackTrace();
		}
		getContentPane().setLayout(null);
		setTitle("进度");
		setBounds(700, 400, 300, 89);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);

		progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		progressBar.setBounds(10, 36, 274, 16);
		getContentPane().add(progressBar);
		
		JLabel lblNewLabel = new JLabel("保存中...");
		lblNewLabel.setBounds(10, 11, 122, 15);
		getContentPane().add(lblNewLabel);

	}
}