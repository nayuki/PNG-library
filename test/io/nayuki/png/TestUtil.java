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
import java.util.Arrays;
import java.util.Random;
import org.junit.Assert;
import io.nayuki.png.chunk.ChunkWriter;


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
	
	
	public static void assertDataEquals(String expectHex, Chunk chk) {
		assertDataEquals(hexToBytes(expectHex), chk);
	}
	
	
	public static void assertDataEquals(byte[] expect, Chunk chk) {
		var out = new ByteArrayOutputStream();
		try {
			chk.writeData(new ChunkWriter(Integer.MAX_VALUE, "AAAA", out));
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		byte[] b = out.toByteArray();
		Assert.assertArrayEquals(expect, Arrays.copyOfRange(b, 8, b.length));
	}
	
	
	public static void assertChunkBytesEqual(String expectHex, Chunk chk) {
		var out = new ByteArrayOutputStream();
		try {
			chk.writeChunk(out);
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		Assert.assertArrayEquals(hexToBytes(expectHex), out.toByteArray());
	}
	
	
	public static final boolean ENABLE_LARGE_MEMORY_TEST_CASES = true;
	
	
	public static Random rand = new Random();
	
	
	private TestUtil() {}
	
}
