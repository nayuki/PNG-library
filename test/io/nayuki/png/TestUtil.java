/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import org.junit.Assert;


public final class TestUtil {
	
	public static byte[] hexToBytes(String s) {
		s = s.replace(" ", "");
		var result = new byte[s.length() / 2];
		for (int i = 0; i < result.length; i++)
			result[i] = (byte)Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
		return result;
	}
	
	
	public static void runExpect(Class<? extends Throwable> exception, Runnable func) {
		try {
			func.run();
			Assert.fail("Expected exception");
		} catch (Throwable e) {
			if (exception.isInstance(e));  // Pass
			else  throw e;
		}
	}
	
	
	public static byte[] writeChunkToBytes(Chunk chk) {
		var out = new ByteArrayOutputStream();
		try {
			chk.writeChunk(out);
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		return out.toByteArray();
	}
	
	
	public static Random rand = new Random();
	
	
	private TestUtil() {}
	
}
