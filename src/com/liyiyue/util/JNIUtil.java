package com.liyiyue.util;

import java.io.FileOutputStream;
import java.io.InputStream;

public class JNIUtil {

	private static boolean loaded;

	public static native boolean setGifToClipBoard(String path);

	public static void init() {
		loaded = true;
		String jarPath = "/com/jni/";
		String tmpDir = System.getProperty("java.io.tmpdir");
		try {
			String dll = "JNI_DLL_GIF.dll";
			fromJarToFs(jarPath + dll, tmpDir + dll);
			System.load(tmpDir + dll);
		} catch (Throwable e) {
			try {
				String dll = "JNI_DLL_GIF64.dll";
				fromJarToFs(jarPath + dll, tmpDir + dll);
				System.load(tmpDir + dll);
			} catch (Throwable e2) {
				loaded = false;
			}
		}
	}

	/**
	 * 将jar中的文件临时缓存出来
	 */
	public static void fromJarToFs(String jarFile, String tmpFile) {
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = JNIUtil.class.getResourceAsStream(jarFile);
			fos = new FileOutputStream(tmpFile);
			byte[] buffer = new byte[1024];
			int read = -1;
			while ((read = is.read(buffer)) != -1) {
				fos.write(buffer, 0, read);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
