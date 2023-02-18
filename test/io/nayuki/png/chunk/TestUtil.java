/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.util.Random;


final class TestUtil {
	
	public static byte[] hexToBytes(String s) {
		s = s.replace(" ", "");
		var result = new byte[s.length() / 2];
		for (int i = 0; i < result.length; i++)
			result[i] = (byte)Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
		return result;
	}
	
	
	public static Random rand = new Random();
	
	
	private TestUtil() {}
	
}
