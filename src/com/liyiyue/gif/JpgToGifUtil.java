package com.liyiyue.gif;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.liyiyue.ScreenCatcherWindow;

/**
 * 多张jpg图片合成一个gif
 * 
 * @author colorbin
 *         创建时间: 2017年4月8日 上午11:01:19
 */
public class JpgToGifUtil {

	public static void jpgToGif(String fromPics[], String toGif, int frameMillis) {
		try {
			AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
			gifEncoder.setRepeat(0);
			gifEncoder.start(toGif);
			BufferedImage src[] = new BufferedImage[fromPics.length];
			for (int i = 0; i < src.length; i++) {
				gifEncoder.setDelay(frameMillis);
				src[i] = ImageIO.read(new File(fromPics[i]));
				gifEncoder.addFrame(src[i]);
			}
			gifEncoder.finish();
		} catch (Exception e) {
			System.out.println("jpgToGif Failed:");
		}
	}

	public static void bufferToGif(BufferedImage fromBuffers[], String toGif, int frameMillis) {
		try {
			AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
			gifEncoder.setRepeat(0);
			gifEncoder.start(toGif);
			// 设置质量参数
			
			ScreenCatcherWindow.progressWindow.progressBar.setMaximum(fromBuffers.length);
			ScreenCatcherWindow.progressWindow.progressBar.setValue(0);
			ScreenCatcherWindow.progressWindow.setVisible(true);
			for (int i = 0; i < fromBuffers.length; i++) {
				gifEncoder.setDelay(frameMillis);
				gifEncoder.addFrame(fromBuffers[i]);
				ScreenCatcherWindow.progressWindow.progressBar.setValue(i + 1);
			}
			ScreenCatcherWindow.progressWindow.setVisible(false);
			gifEncoder.finish();
		} catch (Exception e) {
			System.out.println("bufferToGif Failed:");
		}
	}

}
