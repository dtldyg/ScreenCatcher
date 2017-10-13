package com.liyiyue;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.liyiyue.gif.JpgToGifUtil;
import com.liyiyue.util.CacheUtil;
import com.liyiyue.util.FontUtil;
import com.liyiyue.util.JNIUtil;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
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
	private static final int MAX_MILLIS = 15 * 1000; // 可录制最大毫秒数
	private static final int MAX_RECORD_SEC = 5;     // 记录仪记录的最大秒数
	private static final int RECORD_SLEEP = 2;       // 录制每帧休眠
	private static final int DV_MAX_LEN = 20;        // DV框线最大长度
	private static final int DV_DIV = 4;             // DV框边长与框线的比值
	private static final int GLASS_LEN = 161;        // 放大镜边长
	private static final int GLASS_INFO_W = 110;     // 放大镜信息长
	private static final int GLASS_INFO_H = 20;      // 放大镜信息宽
	private static final int MOUSE_SLOW = 4;         // 鼠标慢速移动

	// 全局参数
	private int area_x;          // 选框左上角x
	private int area_y;          // 选框左上角y
	private int mouse_x_r;       // 鼠标拖动选框时的相对x
	private int mouse_y_r;       // 鼠标拖动选框时的相对y
	private byte cursor;         // 鼠标指针样式，0默认、1十字、2拖动
	private boolean mouse_move;  // 鼠标拖动选框中
	private float mouse_x_f;     // 鼠标当前所在x_float
	private float mouse_y_f;     // 鼠标当前所在y_float
	private int mouse_x_i;       // 鼠标当前所在x_int
	private int mouse_y_i;       // 鼠标当前所在y_int
	private boolean mouse_shift; // 鼠标shift慢速移动中
	private byte model;          // 录制模式
	private boolean outPut;      // 是否保存gif
	private int screen_w = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int screen_h = Toolkit.getDefaultToolkit().getScreenSize().height;

	// 成员变量
	private static Executor executor = Executors.newSingleThreadExecutor();
	private Robot robot;                     // 用以模拟截屏
	private Rectangle rectangle;             // 截屏区域
	private BufferedImage bImage;            // 原始背景图像
	private BufferedImage bImageD;           // 绘制背景图像
	private JDialog bgDialog;                // 用以选择截屏区域的组件
	private BackgroundImage bgImg;           // 用以选择截屏区域，并绘制选框的背景
	private List<BufferedImageInfo> buffers; // 录屏序列
	private boolean starting;                // 是否正在录制
	private JDialog dvDialog;                // DV框组件
	private JLabel dvImage;                  // DV框背景
	private JLabel dvRecImage;               // DV rec背景
	private BufferedImage recImage;          // DV rec资源
	private JLabel glassImage;               // 放大镜
	private BufferedImage gImage;            // 放大镜背景图像
	private JLabel glassInfo;                // 放大镜说明文字
	private JLabel glassInfoBg;              // 放大镜说明背景
	private Map<Integer, Long> sizeMap;      // 计算体积的map，字节/1920*1080/帧
	private Map<Byte, Key> keyPress;        // 按键图片资源
	private Map<Byte, Key> keyUnPress;      // 按键图片资源
	private byte[] keyCodes;                 // 按键ascii码

	// 组件
	public static ProgressWindow progressWindow;
	private JLabel lb_1;
	private JTextField tf_setPath;
	private JButton btn_setPath;
	private JButton btn_openPath;
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
	private JLabel lb_6;
	private JSlider sd_qualityBits;
	private JLabel lb_7;
	private JLabel lb_8;
	private JLabel lb_size;
	private JLabel lb_11;
	private JComboBox<ScaleItem> cb_scale;
	private JLabel lb_12;
	private JComboBox<ModelItem> cb_model;

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
		JNIUtil.init();
		robot = new Robot();
		rectangle = new Rectangle();
		buffers = new ArrayList<BufferedImageInfo>();
		mouse_x_f = -1.0f;
		mouse_y_f = -1.0f;
		mouse_x_i = -1;
		mouse_y_i = -1;
		mouse_shift = false;
		starting = false;
		sizeMap = new HashMap<Integer, Long>();
		sizeMap.put(2, 143353l);
		sizeMap.put(3, 209516l);
		sizeMap.put(4, 316155l);
		sizeMap.put(5, 476788l);
		sizeMap.put(6, 740967l);
		sizeMap.put(7, 942649l);
		sizeMap.put(8, 1173570l);

		// 放大镜
		glassImage = new JLabel();
		glassImage.setBounds(0, 0, GLASS_LEN, GLASS_LEN);
		gImage = new BufferedImage(GLASS_LEN, GLASS_LEN, BufferedImage.TYPE_INT_ARGB);
		glassInfo = new JLabel(" 按住shift屏住呼吸");
		glassInfo.setFont(new Font("微软雅黑", Font.BOLD, 12));
		glassInfo.setBounds(0, 0, GLASS_INFO_W, GLASS_INFO_H);
		glassInfo.setForeground(Color.WHITE);
		glassInfoBg = new JLabel();
		glassInfoBg.setBounds(0, 0, GLASS_INFO_W, GLASS_INFO_H);
		BufferedImage gInfo = new BufferedImage(GLASS_INFO_W, GLASS_INFO_H, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < GLASS_INFO_W; i++) {
			for (int j = 0; j < GLASS_INFO_H; j++) {
				gInfo.setRGB(i, j, 0x88000000);
			}
		}
		glassInfoBg.setIcon(new ImageIcon(gInfo));

		// 初始化画布，监听鼠标按下、松开，以及移动
		bgImg = new BackgroundImage();
		bgImg.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		bgImg.setLocation(0, 0);
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
					refreshQualitySize();
				}
			}
		});
		bgImg.addMouseMotionListener(new MouseMotionAdapter() {
			// 鼠标按下移动
			@Override
			public void mouseDragged(MouseEvent e) {
				mouseSlowMove(e);
				if (mouse_move) {
					int tmp_x = mouse_x_i - mouse_x_r;
					int tmp_y = mouse_y_i - mouse_y_r;
					if (tmp_x < 0) tmp_x = 0;
					if (tmp_y < 0) tmp_y = 0;
					if (tmp_x > screen_w - bgImg.w) tmp_x = screen_w - bgImg.w;
					if (tmp_y > screen_h - bgImg.h) tmp_y = screen_h - bgImg.h;
					area_x = tmp_x;
					area_y = tmp_y;
					bgImg.drawRectangle(area_x, area_y, area_x + bgImg.w - 1, area_y + bgImg.h - 1);
				} else {
					bgImg.drawRectangle(area_x, area_y, mouse_x_i, mouse_y_i);
				}
				setGlassLocationAndRepaint();
			}

			// 鼠标松开移动
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseSlowMove(e);
				if (bgImg.inArea(mouse_x_i, mouse_y_i)) {
					if (cursor != 2) {
						cursor = 2;
						bgDialog.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					}
				} else {
					if (cursor != 1) {
						cursor = 1;
						bgDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
				setGlassLocationAndRepaint();
			}
		});
		// 区域选择
		bgDialog = new JDialog();
		cursor = 1;
		bgDialog.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		bgDialog.setUndecorated(true);
		bgDialog.setAlwaysOnTop(true);
		bgDialog.setModal(true);
		bgDialog.setResizable(false);
		bgDialog.getContentPane().setLayout(null);
		bgDialog.getContentPane().add(glassInfo);
		bgDialog.getContentPane().add(glassInfoBg);
		bgDialog.getContentPane().add(glassImage);
		bgDialog.getContentPane().add(bgImg);
		bgDialog.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
					bgDialog.setVisible(false);
					refreshQualitySize();
				}
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					mouse_shift = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
					mouse_shift = true;
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
		dvDialog.setFocusableWindowState(false);
		dvDialog.getContentPane().setLayout(null);
		dvDialog.getContentPane().add(dvRecImage);
		dvDialog.getContentPane().add(dvImage);
		AWTUtilities.setWindowOpaque(dvDialog, false);
		recImage = ImageIO.read(getClass().getResourceAsStream("/res/pic_rec.png"));

		// 按键资源初始化
		int b1 = 5;
		int b2 = 2;
		int b3 = 5;
		int l = 30;
		keyPress = new HashMap<Byte, Key>();
		keyPress.put((byte) 37, new Key((byte) 37, 1 << 0, ImageIO.read(getClass().getResourceAsStream("/res/left_p.png")), 3 * l + 2 * b2 + b1, l + b1));
		keyPress.put((byte) 38, new Key((byte) 38, 1 << 1, ImageIO.read(getClass().getResourceAsStream("/res/up_p.png")), 2 * l + b2 + b1, 2 * l + b2 + b1));
		keyPress.put((byte) 39, new Key((byte) 39, 1 << 2, ImageIO.read(getClass().getResourceAsStream("/res/right_p.png")), l + b1, l + b1));
		keyPress.put((byte) 40, new Key((byte) 40, 1 << 3, ImageIO.read(getClass().getResourceAsStream("/res/down_p.png")), 2 * l + b2 + b1, l + b1));
		keyPress.put((byte) 32, new Key((byte) 32, 1 << 4, ImageIO.read(getClass().getResourceAsStream("/res/space_p.png")), 6 * l + b3 + 2 * b2 + b1, l + b1));
		keyUnPress = new HashMap<Byte, Key>();
		keyUnPress.put((byte) 37, new Key((byte) 37, 1 << 0, ImageIO.read(getClass().getResourceAsStream("/res/left_u.png")), 3 * l + 2 * b2 + b1, l + b1));
		keyUnPress.put((byte) 38, new Key((byte) 38, 1 << 1, ImageIO.read(getClass().getResourceAsStream("/res/up_u.png")), 2 * l + b2 + b1, 2 * l + b2 + b1));
		keyUnPress.put((byte) 39, new Key((byte) 39, 1 << 2, ImageIO.read(getClass().getResourceAsStream("/res/right_u.png")), l + b1, l + b1));
		keyUnPress.put((byte) 40, new Key((byte) 40, 1 << 3, ImageIO.read(getClass().getResourceAsStream("/res/down_u.png")), 2 * l + b2 + b1, l + b1));
		keyUnPress.put((byte) 32, new Key((byte) 32, 1 << 4, ImageIO.read(getClass().getResourceAsStream("/res/space_u.png")), 6 * l + b3 + 2 * b2 + b1, l + b1));
		keyCodes = new byte[] { 37, 38, 39, 40, 32 };

		CacheUtil.init();
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
		setBounds(600, 300, 422, 370);
		setAlwaysOnTop(true);
		setResizable(false);

		lb_1 = new JLabel("保存路径：");
		lb_1.setBounds(12, 9, 72, 23);
		getContentPane().add(lb_1);

		tf_setPath = new JTextField();
		tf_setPath.setText(CacheUtil.path);
		tf_setPath.setEditable(false);
		tf_setPath.setBounds(82, 9, 185, 23);
		getContentPane().add(tf_setPath);

		btn_setPath = new JButton("选择");
		btn_setPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setPath();
			}
		});
		btn_setPath.setBounds(276, 9, 60, 23);
		getContentPane().add(btn_setPath);

		btn_openPath = new JButton("打开");
		btn_openPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String path = tf_setPath.getText();
				if (!isEmptyStr(path)) {
					File file = new File(path);
					if (file.exists()) {
						try {
							Desktop.getDesktop().open(file);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		btn_openPath.setBounds(340, 9, 60, 23);
		getContentPane().add(btn_openPath);

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

		FPSItem[] fpsArr = new FPSItem[6];
		fpsArr[0] = new FPSItem(5);
		fpsArr[1] = new FPSItem(10);
		fpsArr[2] = new FPSItem(15);
		fpsArr[3] = new FPSItem(20);
		fpsArr[4] = new FPSItem(25);
		fpsArr[5] = new FPSItem(30);

		cb_cutFrames = new JComboBox<FPSItem>();
		cb_cutFrames.setBounds(82, 75, 115, 23);
		cb_cutFrames.setModel(new DefaultComboBoxModel<FPSItem>(fpsArr));
		cb_cutFrames.setSelectedIndex(1);
		cb_cutFrames.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refreshQualitySize();
			}
		});
		getContentPane().add(cb_cutFrames);

		lb_5 = new JLabel("播放帧率：");
		lb_5.setBounds(215, 76, 65, 23);
		getContentPane().add(lb_5);

		cb_playFrames = new JComboBox<FPSItem>();
		cb_playFrames.setBounds(285, 75, 115, 23);
		cb_playFrames.setModel(new DefaultComboBoxModel<FPSItem>(fpsArr));
		cb_playFrames.setSelectedIndex(1);
		getContentPane().add(cb_playFrames);

		lb_11 = new JLabel("缩放比例：");
		lb_11.setBounds(12, 111, 65, 23);
		getContentPane().add(lb_11);

		cb_scale = new JComboBox<ScaleItem>();
		cb_scale.setBounds(82, 110, 115, 23);
		ScaleItem[] scaleArr = new ScaleItem[6];
		scaleArr[0] = new ScaleItem(100);
		scaleArr[1] = new ScaleItem(90);
		scaleArr[2] = new ScaleItem(80);
		scaleArr[3] = new ScaleItem(70);
		scaleArr[4] = new ScaleItem(60);
		scaleArr[5] = new ScaleItem(50);
		cb_scale.setModel(new DefaultComboBoxModel<ScaleItem>(scaleArr));
		cb_scale.setSelectedIndex(0);
		cb_scale.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refreshQualitySize();
			}
		});
		getContentPane().add(cb_scale);

		lb_12 = new JLabel("录制模式：");
		lb_12.setBounds(215, 111, 65, 23);
		getContentPane().add(lb_12);

		cb_model = new JComboBox<ModelItem>();
		cb_model.setBounds(285, 110, 115, 23);
		ModelItem[] modelArr = new ModelItem[2];
		modelArr[0] = new ModelItem((byte) 0, "录制模式");
		modelArr[1] = new ModelItem((byte) 1, "记录仪模式");
		cb_model.setModel(new DefaultComboBoxModel<ModelItem>(modelArr));
		cb_model.setSelectedIndex(0);
		getContentPane().add(cb_model);

		lb_6 = new JLabel("图像质量：");
		lb_6.setBounds(12, 144, 65, 23);
		getContentPane().add(lb_6);

		lb_7 = new JLabel("高");
		lb_7.setBounds(85, 144, 17, 23);
		getContentPane().add(lb_7);

		lb_8 = new JLabel("低");
		lb_8.setBounds(222, 144, 17, 23);
		getContentPane().add(lb_8);

		lb_size = new JLabel("");
		lb_size.setHorizontalAlignment(SwingConstants.RIGHT);
		lb_size.setBounds(249, 144, 151, 23);
		getContentPane().add(lb_size);

		sd_qualityBits = new JSlider(2, 8, 8);
		sd_qualityBits.setMajorTickSpacing(3);
		sd_qualityBits.setMinorTickSpacing(1);
		sd_qualityBits.setPaintTicks(true);
		sd_qualityBits.setBounds(99, 143, 121, 30);
		sd_qualityBits.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				refreshQualitySize();
			}
		});
		sd_qualityBits.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				if (arg0.getWheelRotation() == -1) {
					sd_qualityBits.setValue(sd_qualityBits.getValue() - 1);
				} else if (arg0.getWheelRotation() == 1) {
					sd_qualityBits.setValue(sd_qualityBits.getValue() + 1);
				}
			}
		});
		getContentPane().add(sd_qualityBits);
		sd_qualityBits.setValue(2);

		lb_9 = new JLabel("开始延迟：");
		lb_9.setBounds(12, 175, 72, 23);
		getContentPane().add(lb_9);

		tf_startDelay = new JTextField();
		tf_startDelay.setText("0");
		tf_startDelay.setBounds(82, 175, 115, 23);
		tf_startDelay.addKeyListener(new NumKeyListener());
		tf_startDelay.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				super.focusLost(arg0);
				if (isEmptyStr(tf_startDelay.getText())) {
					tf_startDelay.setText("0");
				}
			}
		});
		getContentPane().add(tf_startDelay);

		lb_10 = new JLabel("录制时长：");
		lb_10.setBounds(215, 175, 72, 23);
		getContentPane().add(lb_10);

		tf_recordSecond = new JTextField();
		tf_recordSecond.setText("15");
		tf_recordSecond.setBounds(285, 175, 115, 23);
		tf_recordSecond.addKeyListener(new NumKeyListener(MAX_MILLIS / 1000, tf_recordSecond));
		tf_recordSecond.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				super.focusLost(arg0);
				if (isEmptyStr(tf_recordSecond.getText())) {
					tf_recordSecond.setText("0");
				}
			}
		});
		getContentPane().add(tf_recordSecond);

		btn_start = new JButton("开 始");
		btn_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (starting) {
					if (model == 1) {
						outPut = false;
						end();
					}
				} else {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							start();
						}
					});
				}
			}
		});
		btn_start.setBounds(12, 207, 185, 23);
		getContentPane().add(btn_start);

		btn_end = new JButton("保 存");
		btn_end.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				end();
			}
		});
		btn_end.setBounds(215, 207, 185, 23);
		getContentPane().add(btn_end);

		sp_1 = new JScrollPane();
		sp_1.setBounds(12, 240, 388, 89);
		getContentPane().add(sp_1);

		ta_help = new JTextArea();
		ta_help.setLineWrap(true);
		ta_help.setEditable(false);
		ta_help.setText("***必看***\r\n● 全局快捷键：\r\n1.开始/保存   Alt+L\r\n● 小技巧：\r\n1.录制结束后，会自动将gif复制到系统剪贴板，直接去qq粘贴就行\r\n2.图像质量决定了颜色数量，录制区颜色少的情景下，可降低画质以减少体积\r\n● 录制模式：\r\n1.录制模式：录一段gif，手动结束或根据设定的录制时长结束（最长15秒）\r\n2.记录仪模式：开始后循环录制，当保存时，保存最近5秒的gif\r\n● 使用流程：\r\n1.选定保存路径\r\n2.设置捕捉区域（ESC、回车、双击都可以返回）\r\n3.设定相关参数（尤其是图像质量和缩放比例，决定了文件大小）\r\n4.录制");
		ta_help.setCaretPosition(0);
		sp_1.setViewportView(ta_help);

		// 注册开始快捷键
		JIntellitype.getInstance().registerHotKey(0, JIntellitype.MOD_ALT, 'L');
		JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
			@Override
			public void onHotKey(int keyCode) {
				switch (keyCode) {
				case 0:
					if (!starting) {
						executor.execute(new Runnable() {
							@Override
							public void run() {
								start();
							}
						});
					} else {
						end();
					}
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * 开始录屏
	 * 
	 * @throws Exception
	 */
	private void start() {
		if (isEmptyStr(tf_setPath.getText()) || isEmptyStr(tf_areaW.getText()) || isEmptyStr(tf_areaH.getText())) {
			return;
		}
		starting = true;
		outPut = true;
		buffers.clear();

		// 锁定控件
		lock();

		// 开始延迟
		recordDelay();

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

		// 模式
		model = ((ModelItem) cb_model.getSelectedItem()).getModel();
		// REC闪烁帧
		int num = 0;
		// 截取频率
		int frames = ((FPSItem) cb_cutFrames.getSelectedItem()).getFrames();
		// 开始时间
		long begin = System.currentTimeMillis();
		// 录制时长
		int recordSecond = isEmptyStr(tf_recordSecond.getText()) ? 0 : Integer.parseInt(tf_recordSecond.getText());
		int maxMillis = recordSecond <= 0 ? MAX_MILLIS : recordSecond * 1000;
		// 每帧延时
		int delay = 1000 / frames;
		// 已录时长
		int totalSecond = 0;
		long lastSecondMillis = 0;
		// 上次截屏时间
		long lastTime = 0;
		// 循环录制最大帧数
		int maxFrames = MAX_RECORD_SEC * frames;
		if (model == 0) {
			btn_start.setText("录制中...0秒");
		} else if (model == 1) {
			btn_start.setText("停 止");
		}
		while (starting) {
			long currentTime = System.currentTimeMillis();
			if (model == 0) {
				if (lastSecondMillis == 0) {
					lastSecondMillis = currentTime;
				}
				if (currentTime - lastSecondMillis >= 1000) {
					lastSecondMillis = currentTime;
					totalSecond++;
					btn_start.setText("录制中..." + totalSecond + "秒");
				}
			}
			if (model == 0) {
				if ((currentTime - begin) >= maxMillis) {
					end();
					continue;
				}
			}
			if ((currentTime - lastTime) >= delay) {
				lastTime = currentTime;
				// REC闪烁，每秒的截屏间隔中，只有最后一个间隔显示
				num++;
				boolean setVisable = false;
				if (num >= frames) {
					num = 0;
					setVisable = true;
				} else {
					if (dvRecImage.isVisible()) {
						dvRecImage.setVisible(false);
						// 休眠1秒，以确保UI线程在截屏前，完成了重绘
						try {
							Thread.sleep(1);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				// 截屏
				if (model == 1) {
					if (buffers.size() >= maxFrames) {
						buffers.remove(0);
					}
				}
				BufferedImageInfo info = new BufferedImageInfo();
				info.image = robot.createScreenCapture(rectangle);
				info.key = JNIUtil.getKeyState(keyCodes);
				buffers.add(info);
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
		if (outPut) {
			outPutGif();
		}
		buffers.clear();

		// 解锁控件
		unLock();
	}

	/**
	 * 开始延迟
	 */
	private void recordDelay() {
		if (isEmptyStr(tf_startDelay.getText()) || Integer.parseInt(tf_startDelay.getText()) <= 0) return;
		int delaySecond = Integer.parseInt(tf_startDelay.getText());
		for (int i = delaySecond; i > 0; i--) {
			btn_start.setText("倒计时：" + i + "秒");
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 输出gif
	 */
	private void outPutGif() {
		String savePath = tf_setPath.getText() + File.separator + "sc_" + System.currentTimeMillis() / 1000 + ".gif";
		int playDelay = 1000 / ((FPSItem) cb_playFrames.getSelectedItem()).getFrames();
		JpgToGifUtil.bufferToGif(getResizeBufferedImages(buffers), savePath, playDelay, (byte) bitTValue(sd_qualityBits.getValue()));
		// gif拷贝进系统剪贴板
		JNIUtil.setGifToClipBoard(savePath);
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
	 * 锁定控件
	 */
	private void lock() {
		btn_setPath.setEnabled(false);
		btn_setAera.setEnabled(false);
		cb_cutFrames.setEnabled(false);
		cb_playFrames.setEnabled(false);
		cb_scale.setEnabled(false);
		cb_model.setEnabled(false);
		sd_qualityBits.setEnabled(false);
		tf_startDelay.setEnabled(false);
		tf_recordSecond.setEnabled(false);
	}

	/**
	 * 解锁控件
	 */
	private void unLock() {
		btn_setPath.setEnabled(true);
		btn_setAera.setEnabled(true);
		cb_cutFrames.setEnabled(true);
		cb_playFrames.setEnabled(true);
		cb_scale.setEnabled(true);
		cb_model.setEnabled(true);
		sd_qualityBits.setEnabled(true);
		tf_startDelay.setEnabled(true);
		tf_recordSecond.setEnabled(true);
	}

	/**
	 * 获取某一帧的按键信息
	 * 
	 * @param bii
	 * @return
	 */
	private Set<Byte> getKeyIndexSet(int info) {
		Set<Byte> rt = new HashSet<Byte>();
		if (info > 0) {
			for (Key key : keyPress.values()) {
				if ((info & key.bit) > 0) {
					rt.add(key.index);
				}
			}
		}
		return rt;
	}

	/**
	 * 压缩尺寸、写入按键信息
	 * 
	 * @param buffers
	 * @return
	 */
	private BufferedImage[] getResizeBufferedImages(List<BufferedImageInfo> buffers) {
		int sizePercent = ((ScaleItem) cb_scale.getSelectedItem()).getScale();
		BufferedImage[] rtArr = new BufferedImage[buffers.size()];
		int w = buffers.get(0).image.getWidth() * sizePercent / 100;
		int h = buffers.get(0).image.getHeight() * sizePercent / 100;
		// 显示进度条
		ScreenCatcherWindow.progressWindow.progressBar.setMaximum(rtArr.length);
		ScreenCatcherWindow.progressWindow.progressBar.setValue(0);
		ScreenCatcherWindow.progressWindow.title.setText("预处理中...");
		ScreenCatcherWindow.progressWindow.setVisible(true);
		for (int i = 0; i < rtArr.length; i++) {
			rtArr[i] = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
			rtArr[i].createGraphics().drawImage(buffers.get(i).image.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
			// 写入按键信息
			writeKeyInfo(rtArr[i], buffers.get(i).key);
			ScreenCatcherWindow.progressWindow.progressBar.setValue(i + 1);
		}
		return rtArr;
	}

	/**
	 * 对每一帧，写入按键信息
	 * 
	 * @param bi
	 * @param keyInfo
	 */
	private void writeKeyInfo(BufferedImage bi, int keyInfo) {
		int w = bi.getWidth();
		int h = bi.getHeight();
		if (w < 240 || h < 120) return;
		Set<Byte> set = getKeyIndexSet(keyInfo);
		for (byte index : keyCodes) {
			if (!set.isEmpty() && set.contains(index)) {
				drawKey(bi, keyPress.get(index));
			} else {
				drawKey(bi, keyUnPress.get(index));
			}
		}
	}

	/**
	 * 画一个按键
	 * 
	 * @param bi
	 * @param key
	 */
	private void drawKey(BufferedImage bi, Key key) {
		BufferedImage keyi = key.image;
		for (int i = 0; i < keyi.getWidth(); i++) {
			for (int j = 0; j < keyi.getHeight(); j++) {
				int x = bi.getWidth() - 1 + key.x_off + i;
				int y = bi.getHeight() - 1 + key.y_off + j;
				bi.setRGB(x, y, mixColor(bi.getRGB(x, y), keyi.getRGB(i, j)));
			}
		}
	}

	/**
	 * 混合两种颜色，其中上面的层包含透明通道
	 */
	private int mixColor(int down, int up) {
		int r_d = (down >> 16) & 0xff;
		int g_d = (down >> 8) & 0xff;
		int b_d = down & 0xff;
		int a = (up >> 24) & 0xff;
		int r_u = (up >> 16) & 0xff;
		int g_u = (up >> 8) & 0xff;
		int b_u = up & 0xff;
		int r_n = r_d * (256 - a) / 256 + r_u * a / 256;
		int g_n = g_d * (256 - a) / 256 + g_u * a / 256;
		int b_n = b_d * (256 - a) / 256 + b_u * a / 256;
		return (r_n << 16) | (g_n << 8) | b_n;
	}

	/**
	 * 移动放大镜位置，并且绘制
	 */
	private void setGlassLocationAndRepaint() {
		int x = 0;
		int y = 0;
		if (mouse_x_i + 15 + GLASS_LEN >= screen_w) {
			x = mouse_x_i - 5 - GLASS_LEN;
		} else {
			x = mouse_x_i + 15;
		}
		if (mouse_y_i + 20 + GLASS_LEN + GLASS_INFO_H >= screen_h) {
			y = mouse_y_i - 10 - GLASS_LEN - GLASS_INFO_H;
		} else {
			y = mouse_y_i + 20;
		}
		glassImage.setLocation(x, y);
		glassInfo.setLocation(x, y + GLASS_LEN);
		glassInfoBg.setLocation(x, y + GLASS_LEN);
		repaintGlass(mouse_x_i, mouse_y_i);
	}

	/**
	 * 刷新质量输出的文字
	 */
	private void refreshQualitySize() {
		if (rectangle.width > 0 && rectangle.height > 0) {
			int bits = sd_qualityBits.getValue();
			bits = bitTValue(bits);
			int scale = ((ScaleItem) cb_scale.getSelectedItem()).getScale();
			int w = rectangle.width * scale / 100;
			int h = rectangle.height * scale / 100;
			lb_size.setText("预计：" + getFormatSize(sizeMap.get(bits) * 5 * ((FPSItem) cb_cutFrames.getSelectedItem()).getFrames() * w * h / 1920 / 1080) + " / 5秒");
		}
	}

	/**
	 * slider转换
	 */
	private int bitTValue(int bit) {
		return 10 - bit;
	}

	/**
	 * 绘制放大镜
	 */
	private void repaintGlass(int m_x, int m_y) {
		int off = (GLASS_LEN - 1) / 2;
		for (int i = 0; i < GLASS_LEN; i++) {
			for (int j = 0; j < GLASS_LEN; j++) {
				if (i == off - 1 || i == off || i == off + 1 || j == off - 1 || j == off || j == off + 1) {
					gImage.setRGB(i, j, 0xff33bbbb);
				} else {
					int x = formatGlassPixel(i, off) + m_x;
					int y = formatGlassPixel(j, off) + m_y;
					if (x < 0 || x > screen_w - 1 || y < 0 || y > screen_h - 1) {
						gImage.setRGB(i, j, 0x55000000);
					} else {
						gImage.setRGB(i, j, bImage.getRGB(x, y));
					}
				}
			}
		}
		glassImage.setIcon(new ImageIcon(gImage));
	}

	/**
	 * 放大镜读取像素时，需要偏移一下
	 */
	private int formatGlassPixel(int v, int off) {
		if (v < off) {
			return (v - (off + 1)) / 3;
		} else {
			return (v - (off - 1)) / 3;
		}
	}

	/**
	 * 鼠标缓慢移动
	 */
	private void mouseSlowMove(MouseEvent e) {
		if (mouse_x_f < 0 && mouse_y_f < 0 && mouse_x_i < 0 && mouse_y_i < 0) {
			mouse_x_f = e.getX();
			mouse_y_f = e.getY();
			mouse_x_i = e.getX();
			mouse_y_i = e.getY();
		}
		if (mouse_shift) {
			mouse_x_f = (e.getX() - mouse_x_i) * 1.0f / MOUSE_SLOW + mouse_x_f;
			mouse_y_f = (e.getY() - mouse_y_i) * 1.0f / MOUSE_SLOW + mouse_y_f;
			mouse_x_i = (int) mouse_x_f;
			mouse_y_i = (int) mouse_y_f;
			robot.mouseMove(mouse_x_i, mouse_y_i);
		} else {
			mouse_x_f = e.getX();
			mouse_y_f = e.getY();
			mouse_x_i = e.getX();
			mouse_y_i = e.getY();
		}
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
			CacheUtil.savePath(selectedFiles.getPath());
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
			if ((b.getWidth() - (len / 2)) <= recImage.getWidth() * 2 || (b.getHeight() - (len / 2)) <= recImage.getHeight() * 2) return;
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

	public static DecimalFormat df = new DecimalFormat("#0.0");
	private JLabel lb_9;
	private JLabel lb_10;
	private JTextField tf_startDelay;
	private JTextField tf_recordSecond;

	public static String getFormatSize(long bytes) {
		if (bytes < 1024) return bytes + "B";
		if (bytes >= 1024 && bytes < 1024 * 1024) return df.format(((double) bytes) / 1024) + "KB";
		return df.format(((double) bytes) / 1024 / 1024) + "MB";
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
			w = Math.abs(x1 - x2) + 1;
			h = Math.abs(y1 - y2) + 1;
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

	/**
	 * @author liyiyue
	 * @date 2017年9月26日上午11:21:12
	 * @desc 缩放
	 */
	private class ScaleItem {
		/** 缩放百分数 */
		private int scale;

		public int getScale() {
			return scale;
		}

		public ScaleItem(int scale) {
			this.scale = scale;
		}

		@Override
		public String toString() {
			return scale + "%";
		}
	}

	/**
	 * @author liyiyue
	 * @date 2017年9月26日上午11:21:12
	 * @desc 模式
	 */
	private class ModelItem {
		/** 模式 */
		private byte model;
		private String name;

		public byte getModel() {
			return model;
		}

		public ModelItem(byte model, String name) {
			this.model = model;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * @author liyiyue
	 * @date 2017年10月12日下午12:27:07
	 * @desc 包含额外信息的BufferedImage
	 */
	public class BufferedImageInfo {
		public BufferedImage image;
		public int key;
	}

	/**
	 * @author liyiyue
	 * @date 2017年10月12日下午2:30:03
	 * @desc 按键
	 */
	public class Key {
		byte index;
		int bit;
		BufferedImage image;
		int x_off;
		int y_off;

		public Key(byte index, int bit, BufferedImage image, int x_off, int y_off) {
			this.index = index;
			this.bit = bit;
			this.image = image;
			this.x_off = -x_off;
			this.y_off = -y_off;
		}
	}

	/**
	 * 监听器，只能输入数字
	 */
	public class NumKeyListener implements KeyListener {
		/** 最大值 */
		private int max;
		/** 被监听组件 */
		private JTextField field;

		public NumKeyListener() {
			this.max = -1;
		}

		public NumKeyListener(int max, JTextField field) {
			this.max = max;
			this.field = field;
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
			int keyChar = e.getKeyChar();
			if (keyChar < KeyEvent.VK_0 || keyChar > KeyEvent.VK_9) {
				e.consume();
			} else {
				if (max >= 0 && Integer.parseInt(field.getText() + e.getKeyChar()) > max) {
					e.consume();
					field.setText(max + "");
				}
			}
		}
	}
}