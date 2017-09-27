package com.liyiyue;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.liyiyue.gif.JpgToGifUtil;
import com.liyiyue.util.FontUtil;
import com.sun.awt.AWTUtilities;

/**
 * @author liyiyue
 * @date 2017年9月25日下午7:52:39
 * @desc GIF屏幕录像宗师
 */
@SuppressWarnings("restriction")
public class ScreenCatcherWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// 常量
	private static final int MAX_MILLIS = 10 * 1000; // 可录制最大毫秒数
	private static final int RECORD_SLEEP = 2;       // 录制每帧休眠
	private static final int DV_MAX_LEN = 20;        // DV框线最大长度
	private static final int DV_DIV = 4;             // DV框边长与框线的比值

	// 全局参数
	private int area_x;         // 选框左上角x
	private int area_y;         // 选框左上角y
	private int mouse_x_r;      // 鼠标按下的相对x
	private int mouse_y_r;      // 鼠标按下的相对y
	private byte cursor;        // 鼠标指针样式，0默认、1十字、2拖动
	private boolean mouse_move; // 鼠标拖动中
	private int screen_w = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int screen_h = Toolkit.getDefaultToolkit().getScreenSize().height;

	// 成员变量
	private static Executor executor = Executors.newSingleThreadExecutor();
	private Robot robot;                 // 用以模拟截屏
	private Rectangle rectangle;         // 截屏区域
	private BufferedImage bImage;        // 原始背景图像
	private BufferedImage bImageD;       // 绘制背景图像
	private JDialog bgDialog;            // 用以选择截屏区域的组件
	private BackgroundImage bgImg;       // 用以选择截屏区域，并绘制选框的背景
	private List<BufferedImage> buffers; // 录屏序列
	private boolean starting;            // 是否正在录制
	private JDialog dvDialog;            // DV框组件
	private JLabel dvImage;              // DV框背景
	private JLabel dvRecImage;           // DV rec背景
	private BufferedImage recImage;      // DV rec资源
//	private JDialog glassDialog;         // 放大镜组件
//	private JLabel glassImage;           // 放大镜背景

	// 组件
	public static ProgressWindow progressWindow;
	private JLabel lb_1;
	private JTextField tf_setPath;
	private JButton btn_setPath;
	private JButton btn_setAera;
	private JTextField tf_areaW;
	private JTextField tf_areaH;
	private JLabel lb_4;
	private JComboBox<FPSItem> cb_cutFrames;
	private JComboBox<FPSItem> cb_playFrames;
	private JLabel lb_5;
	private JButton btn_start;
	private JButton btn_end;
	private JScrollPane sp_1;
	private JTextArea ta_help;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ScreenCatcherWindow window = new ScreenCatcherWindow();
					window.setVisible(true);

					progressWindow = new ProgressWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void init() throws Exception {
		robot = new Robot();
		rectangle = new Rectangle();
		buffers = new ArrayList<BufferedImage>();

		// 初始化画布，监听鼠标按下、松开，以及移动
		bgImg = new BackgroundImage();
		bgImg.addMouseListener(new MouseAdapter() {
			// 鼠标按下
			@Override
			public void mousePressed(MouseEvent e) {
				if (bgImg.inArea(e.getX(), e.getY())) {
					mouse_move = true;
					area_x = bgImg.x;
					area_y = bgImg.y;
					mouse_x_r = e.getX() - area_x;
					mouse_y_r = e.getY() - area_y;
				} else {
					area_x = e.getX();
					area_y = e.getY();
				}
			}

			// 鼠标释放
			@Override
			public void mouseReleased(MouseEvent e) {
				mouse_move = false;
			}

			// 鼠标点击
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					bgDialog.setVisible(false);
				}
			}
		});
		bgImg.addMouseMotionListener(new MouseMotionAdapter() {
			// 鼠标按下移动
			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouse_move) {
					int tmp_x = e.getX() - mouse_x_r;
					int tmp_y = e.getY() - mouse_y_r;
					if (tmp_x < 0) tmp_x = 0;
					if (tmp_y < 0) tmp_y = 0;
					if (tmp_x > screen_w - bgImg.w) tmp_x = screen_w - bgImg.w;
					if (tmp_y > screen_h - bgImg.h) tmp_y = screen_h - bgImg.h;
					area_x = tmp_x;
					area_y = tmp_y;
					bgImg.drawRectangle(area_x, area_y, area_x + bgImg.w, area_y + bgImg.h);
				} else {
					bgImg.drawRectangle(area_x, area_y, e.getX(), e.getY());
				}
			}

			// 鼠标松开移动
			@Override
			public void mouseMoved(MouseEvent e) {
				if (bgImg.inArea(e.getX(), e.getY())) {
					if (cursor != 2) {
						cursor = 2;
						bgDialog.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					}
				} else {
					if (cursor != 1) {
						cursor = 1;
						bgDialog.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
					}
				}
			}
		});

		// 区域选择
		bgDialog = new JDialog();
		bgDialog.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		cursor = 1;
		bgDialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		bgDialog.setUndecorated(true);
		bgDialog.setAlwaysOnTop(true);
		bgDialog.setModal(true);
		bgDialog.setResizable(false);
		bgDialog.getContentPane().setLayout(new BorderLayout());
		bgDialog.getContentPane().add(BorderLayout.CENTER, bgImg);
		bgDialog.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
					bgDialog.setVisible(false);
				}
			}
		});

		// 录制DV框
		dvDialog = new JDialog();
		dvImage = new JLabel();
		dvRecImage = new JLabel();
		dvDialog.setUndecorated(true);
		dvDialog.setAlwaysOnTop(true);
		dvDialog.setResizable(false);
		dvDialog.setFocusable(false);
		dvDialog.getContentPane().setLayout(null);
		dvDialog.getContentPane().add(dvRecImage);
		dvDialog.getContentPane().add(dvImage);
		AWTUtilities.setWindowOpaque(dvDialog, false);
		recImage = ImageIO.read(getClass().getResourceAsStream("/pic/pic_rec.png"));

		// 放大镜
//		glassDialog = new JDialog();
//		glassImage = new JLabel();
	}

	public ScreenCatcherWindow() throws Exception {
		init();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			FontUtil.setGlobalFonts(new Font("微软雅黑", Font.PLAIN, 12));
		} catch (Exception e) {
			e.printStackTrace();
		}
		getContentPane().setLayout(null);
		setTitle("GIF屏幕录像宗师");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 300, 422, 274);
		setAlwaysOnTop(true);
		setResizable(false);

		lb_1 = new JLabel("保存路径：");
		lb_1.setBounds(12, 9, 72, 23);
		getContentPane().add(lb_1);

		tf_setPath = new JTextField();
		tf_setPath.setText("E:\\");
		tf_setPath.setEditable(false);
		tf_setPath.setBounds(82, 9, 241, 23);
		getContentPane().add(tf_setPath);

		btn_setPath = new JButton("选择");
		btn_setPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});
		btn_setPath.setBounds(335, 9, 65, 23);
		getContentPane().add(btn_setPath);

		btn_setAera = new JButton("设置捕捉区域");
		btn_setAera.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setAera();
			}
		});
		btn_setAera.setBounds(264, 43, 136, 23);
		getContentPane().add(btn_setAera);

		JLabel lb_2 = new JLabel("区域大小：");
		lb_2.setBounds(12, 42, 72, 23);
		getContentPane().add(lb_2);

		tf_areaW = new JTextField();
		tf_areaW.setEditable(false);
		tf_areaW.setBounds(82, 43, 72, 23);
		getContentPane().add(tf_areaW);

		tf_areaH = new JTextField();
		tf_areaH.setEditable(false);
		tf_areaH.setBounds(182, 43, 72, 23);
		getContentPane().add(tf_areaH);

		JLabel lb_3 = new JLabel("×");
		lb_3.setBounds(164, 43, 12, 23);
		getContentPane().add(lb_3);

		lb_4 = new JLabel("截取帧率：");
		lb_4.setBounds(12, 76, 65, 23);
		getContentPane().add(lb_4);

		FPSItem[] fpsArr = new FPSItem[3];
		fpsArr[0] = new FPSItem(5);
		fpsArr[1] = new FPSItem(10);
		fpsArr[2] = new FPSItem(15);

		cb_cutFrames = new JComboBox<FPSItem>();
		cb_cutFrames.setBounds(82, 75, 115, 23);
		cb_cutFrames.setModel(new DefaultComboBoxModel<FPSItem>(fpsArr));
		cb_cutFrames.setSelectedIndex(1);
		getContentPane().add(cb_cutFrames);

		cb_playFrames = new JComboBox<FPSItem>();
		cb_playFrames.setBounds(285, 75, 115, 23);
		cb_playFrames.setModel(new DefaultComboBoxModel<FPSItem>(fpsArr));
		cb_playFrames.setSelectedIndex(1);
		getContentPane().add(cb_playFrames);

		lb_5 = new JLabel("播放帧率：");
		lb_5.setBounds(215, 76, 65, 23);
		getContentPane().add(lb_5);

		btn_start = new JButton("开 始");
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						start();
					}
				});
			}
		});
		btn_start.setBounds(12, 109, 185, 23);
		getContentPane().add(btn_start);

		btn_end = new JButton("结 束");
		btn_end.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				end();
			}
		});
		btn_end.setBounds(215, 109, 185, 23);
		getContentPane().add(btn_end);

		sp_1 = new JScrollPane();
		sp_1.setBounds(12, 142, 388, 89);
		getContentPane().add(sp_1);

		ta_help = new JTextArea();
		ta_help.setEditable(false);
		ta_help.setText("说明：\r\n1.选择gif文件的“保存路径“\r\n2.设置gif“捕捉区域“，双击、Esc、回车可返回\r\n3.选择“帧率“，截取频率建议不要太大\r\n4.点击“开始“，开始录像（最长10秒，10秒后自动结束）\r\n5.点击“结束“，保存gif");
		ta_help.setCaretPosition(0);
		sp_1.setViewportView(ta_help);
	}

	/**
	 * 开始录屏
	 * 
	 * @throws Exception
	 */
	private void start() {
		if (isEmptyStr(tf_setPath.getText())) {
			JOptionPane.showMessageDialog(null, "未选择路径");
			return;
		}
		if (isEmptyStr(tf_areaW.getText()) || isEmptyStr(tf_areaH.getText())) {
			JOptionPane.showMessageDialog(null, "未设置区域");
			return;
		}
		buffers.clear();
		starting = true;
		btn_start.setText("录制中...");

		// 设置DV框
		dvDialog.setBounds(rectangle.x - 1, rectangle.y - 1, rectangle.width + 2, rectangle.height + 2);
		dvImage.setBounds(0, 0, dvDialog.getWidth(), dvDialog.getHeight());
		dvRecImage.setBounds(0, 0, dvDialog.getWidth(), dvDialog.getHeight());
		dvRecImage.setVisible(false);
		BufferedImage b = new BufferedImage(dvDialog.getWidth(), dvDialog.getHeight(), BufferedImage.TYPE_INT_ARGB);
		setDVInterface(b, true);
		dvImage.setIcon(new ImageIcon(b));
		BufferedImage b2 = new BufferedImage(dvDialog.getWidth(), dvDialog.getHeight(), BufferedImage.TYPE_INT_ARGB);
		setDVInterface(b2, false);
		dvRecImage.setIcon(new ImageIcon(b2));
		dvDialog.setVisible(true);

		int num = 0;
		int frames = ((FPSItem) cb_cutFrames.getSelectedItem()).getFrames();
		long begin = System.currentTimeMillis();
		int delay = 1000 / frames;
		long lastTime = 0;
		while (starting) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - begin) >= MAX_MILLIS) {
				end();
				continue;
			}
			if ((currentTime - lastTime) >= delay) {
				lastTime = currentTime;
				// REC闪烁，每秒的截屏间隔中，只有最后一个间隔显示
				num++;
				boolean setVisable = false;
				if (num >= (frames - 1)) {
					num = 0;
					setVisable = true;
				} else {
					if (dvRecImage.isVisible()) {
						dvRecImage.setVisible(false);
					}
				}
				// 截屏
				buffers.add(robot.createScreenCapture(rectangle));
				if (setVisable) {
					dvRecImage.setVisible(true);
				}
			}
			try {
				Thread.sleep(RECORD_SLEEP);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		outPutGif();
		buffers.clear();
	}

	/**
	 * 输出gif
	 */
	private void outPutGif() {
		String savePath = tf_setPath.getText() + File.separator + "sc_" + System.currentTimeMillis() / 1000 + ".gif";
		int playDelay = 1000 / ((FPSItem) cb_playFrames.getSelectedItem()).getFrames();
		JpgToGifUtil.bufferToGif(buffers.toArray(new BufferedImage[0]), savePath, playDelay);
	}

	/**
	 * 结束录屏
	 */
	private void end() {
		starting = false;
		btn_start.setText("开 始");
		dvDialog.setVisible(false);
	}

	/**
	 * 设置捕捉区域
	 */
	private void setAera() {
		cleanAera();
		bImage = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		bImageD = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int i = bImage.getMinX(); i <= bImage.getWidth() - 1; i++) {
			for (int j = bImage.getMinY(); j <= bImage.getHeight() - 1; j++) {
				bImageD.setRGB(i, j, rgbToDark(bImage.getRGB(i, j)));
			}
		}
		bgImg.setIcon(new ImageIcon(bImageD));
		bgDialog.setVisible(true);
	}

	/**
	 * 设置gif保存路径
	 */
	private void setPath() {
		JFileChooser fc = new JFileChooser(tf_setPath.getText());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int op = fc.showDialog(this, "选择");
		if (op == JFileChooser.APPROVE_OPTION) {
			File selectedFiles = fc.getSelectedFile();
			tf_setPath.setText(selectedFiles.getPath());
		}
	}

	/**
	 * 设置DV框
	 * 
	 * @param b
	 */
	private void setDVInterface(BufferedImage b, boolean frame) {
		int x1 = b.getMinX();
		int y1 = b.getMinY();
		int w = b.getWidth();
		int h = b.getHeight();
		int x2 = b.getMinX() + w - 1;
		int y2 = b.getMinY() + h - 1;

		int len = Math.min(w, h) / DV_DIV;
		len = len > DV_MAX_LEN ? DV_MAX_LEN : len;

		if (frame) {
			for (int x = x1; x <= x2; x++) {
				for (int y = y1; y <= y2; y++) {
					if (inDVLine(x, y, len, x1, y1, x2, y2)) {
						b.setRGB(x, y, 0xffb0b0b0);
					} else {
						b.setRGB(x, y, 0x00ff0000);
					}
				}
			}
		} else {
			for (int i = recImage.getMinX(); i < recImage.getMinX() + recImage.getWidth(); i++) {
				for (int j = recImage.getMinY(); j < recImage.getMinY() + recImage.getHeight(); j++) {
					b.setRGB(i + len / 2, j + len / 2, recImage.getRGB(i, j));
				}
			}
		}
	}

	/**
	 * 在框线上
	 */
	private boolean inDVLine(int x, int y, int len, int x1, int y1, int x2, int y2) {
		if (x == x1 || x == x2 || y == y1 || y == y2) {
			if (!((x >= x1 + len && x <= x2 - len) || (y >= y1 + len && y <= y2 - len))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 初始化选区
	 */
	private void cleanAera() {
		area_x = 0;
		area_y = 0;
		bgImg.clean();
	}

	public static boolean isEmptyStr(String s) {
		return s == null || "".equals(s);
	}

	/**
	 * 颜色变暗
	 */
	private int rgbToDark(int rgb) {
		float f = 0.65f;
		rgb = rgb & 0xffffff;
		int r = rgb >> 16;
		int g = (rgb >> 8) & 0xff;
		int b = rgb & 0xff;
		r = (int) (r * f);
		g = (int) (g * f);
		b = (int) (b * f);
		return (r << 16) | (g << 8) | b;
	}

	/**
	 * @author liyiyue
	 * @date 2017年9月26日上午10:11:12
	 * @desc 用以选择截屏区域，并绘制选框的背景
	 */
	private class BackgroundImage extends JLabel {
		private static final long serialVersionUID = 1L;

		private int x;
		private int y;
		private int w;
		private int h;

		private int x1_l;
		private int y1_l;
		private int x2_l;
		private int y2_l;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (w != 0 && h != 0) {
				int x1 = x;
				int y1 = y;
				int x2 = x + w - 1;
				int y2 = y + h - 1;
				// 绘制选框
				g.drawRect(x, y, w, h);
				// 绘制文字
				String str = Integer.toString(w) + " × " + Integer.toString(h);
				g.setFont(new Font("Consola", Font.BOLD, 16));
				g.drawString(str, x + (int) w / 2 - 28, y + (int) h / 2);
				tf_areaW.setText(w + "");
				tf_areaH.setText(h + "");
				// 绘制背景
				for (int i = bImage.getMinX(); i <= bImage.getWidth() - 1; i++) {
					for (int j = bImage.getMinY(); j <= bImage.getHeight() - 1; j++) {
						if (inANotInB(i, j, x1, y1, x2, y2, x1_l, y1_l, x2_l, y2_l)) {
							bImageD.setRGB(i, j, bImage.getRGB(i, j));
						} else if (inANotInB(i, j, x1_l, y1_l, x2_l, y2_l, x1, y1, x2, y2)) {
							bImageD.setRGB(i, j, rgbToDark(bImage.getRGB(i, j)));
						}
					}
				}
				bgImg.setIcon(new ImageIcon(bImageD));
				// 保存上次
				x1_l = x1;
				y1_l = y1;
				x2_l = x2;
				y2_l = y2;
			}
			rectangle.setBounds(x, y, w, h);
		}

		public boolean inANotInB(int x, int y, int x1a, int y1a, int x2a, int y2a, int x1b, int y1b, int x2b, int y2b) {
			return ((x >= x1a && x <= x2a && y >= y1a && y <= y2a) && (!(x >= x1b && x <= x2b && y >= y1b && y <= y2b)));
		}

		public boolean inArea(int x, int y) {
			if (x1_l == 0 && x2_l == 0 && y1_l == 0 && y2_l == 0) return false;
			return (x >= x1_l && x <= x2_l && y >= y1_l && y <= y2_l);
		}

		/**
		 * 画选框
		 */
		public void drawRectangle(int x1, int y1, int x2, int y2) {
			x = Math.min(x1, x2);
			y = Math.min(y1, y2);
			w = Math.abs(x1 - x2);
			h = Math.abs(y1 - y2);
			repaint();
		}

		/**
		 * 清除选框
		 */
		public void clean() {
			x = 0;
			y = 0;
			w = 0;
			h = 0;
			x1_l = 0;
			y1_l = 0;
			x2_l = 0;
			y2_l = 0;
			repaint();
		}
	}

	/**
	 * @author liyiyue
	 * @date 2017年9月26日上午11:21:12
	 * @desc 帧率
	 */
	private class FPSItem {
		/** 每秒帧数 */
		private int frames;

		public int getFrames() {
			return frames;
		}

		public FPSItem(int frames) {
			this.frames = frames;
		}

		@Override
		public String toString() {
			return frames + " 帧/秒";
		}
	}
}