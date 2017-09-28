package com.liyiyue.util;

/**
 * @author liyiyue
 * @date 2017年9月28日下午7:30:53
 * @desc 系统相关工具类
 */
public class SystemUtil {

	/**
	 * 是否是64位系统
	 */
	public static boolean is64Bit() {
		boolean is64bit = false;
		if (System.getProperty("os.name").contains("Windows")) {
			is64bit = (System.getenv("ProgramFiles(x86)") != null);
		} else {
			is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
		}
		return is64bit;
	}
}
