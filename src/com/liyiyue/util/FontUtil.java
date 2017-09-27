package com.liyiyue.util;

import java.awt.Font;
import java.util.Enumeration;

import javax.swing.UIManager;

/**
 * @author liyiyue
 * @date 2017年9月25日下午8:08:25
 * @desc 字体工具
 */
public class FontUtil {
	public static void setGlobalFonts(Font font) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		Object key = null;
		Object value = null;
		while (keys.hasMoreElements()) {
			key = keys.nextElement();
			value = UIManager.get(key);
			if (value instanceof Font) {
				UIManager.put(key, font);
			}
		}
	}
}
