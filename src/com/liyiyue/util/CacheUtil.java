package com.liyiyue.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author liyiyue
 * @date 2017年10月9日下午2:31:30
 * @desc 用于持久化一些缓存数据的
 */
public class CacheUtil {
	// 常量
	public static final String LOCAL_APP_DATA_PATH = System.getenv("LOCALAPPDATA") + File.separator + "GifScreenCatcher" + File.separator;
	public static final String LOCAL_APP_DATA_FILE = "path";
	// 变量
	public static String path;

	/**
	 * 启动时初始化路径
	 */
	public static void init() {
		String path = readString();
		if (path == null || "".equals(path)) {
			CacheUtil.path = "";
		} else {
			File file = new File(path);
			if (file.exists()) {
				CacheUtil.path = path;
			} else {
				CacheUtil.path = "";
			}
		}
	}

	/**
	 * 保存路径
	 * 
	 * @param path
	 */
	public static void savePath(String path) {
		CacheUtil.path = path;
		saveString(path);
	}

	/**
	 * String存储至文件，文件不存在则创建
	 * 
	 * @param str
	 */
	private static void saveString(String str) {
		FileOutputStream fos = null;
		File file = new File(LOCAL_APP_DATA_PATH + LOCAL_APP_DATA_FILE);
		try {
			// 文件不存在，则创建
			if (!file.exists()) {
				new File(LOCAL_APP_DATA_PATH).mkdirs();
				file.createNewFile();
			}
			fos = new FileOutputStream(file);
			byte[] strBytes = str.getBytes("utf-8");
			fos.write(strBytes);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从文件读取第一行String
	 * 
	 * @return
	 */
	private static String readString() {
		String str = null;
		File file = new File(LOCAL_APP_DATA_PATH + LOCAL_APP_DATA_FILE);
		if (!file.exists()) {
			return str;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			str = reader.readLine();
			reader.close();
			return str;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return str;
	}
}
