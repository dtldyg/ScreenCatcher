package com.liyiyue.util;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.liyiyue.gif.JpgToGifUtil;

public class MainTest {
	private Robot robot;
	private List<BufferedImage> buffer;
	private Rectangle rectangle;
	private int delay = 100;
	private int time = 3 * 1000;

	public static void main(String[] args) throws Exception {
		MainTest main = new MainTest();
		main.run();
	}

	private void run() throws Exception {
		robot = new Robot();
		buffer = new ArrayList<BufferedImage>();
		rectangle = new Rectangle(400, 200, 400, 200);

		long begin = System.currentTimeMillis();
		long lastTime = 0;
		while (true) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - begin) >= time) break;
			if ((currentTime - lastTime) >= delay) {
				lastTime = currentTime;
				logic();
			}
			Thread.sleep(5);
		}
		outPutGif();
	}

	/**
	 * 捕获屏幕指定区域
	 */
	public void logic() {
		buffer.add(robot.createScreenCapture(rectangle));
	}

	/**
	 * 输出gif
	 */
	public void outPutGif() throws Exception {
		JpgToGifUtil.bufferToGif(buffer.toArray(new BufferedImage[0]), "E://1.gif", delay);
	}
}
