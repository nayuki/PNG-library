/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.TestUtil.hexToBytes;
import static org.junit.Assert.assertArrayEquals;
import java.util.Arrays;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class SbitTest {
	
	@Test public void testCreateLengthBad() {
		int[] CASES = {
			0,
			5,
			6,
			7,
			8,
			9,
			10,
			1024,
		};
		
		for (int cs : CASES) {
			var b = new byte[cs];
			Arrays.fill(b, (byte)8);
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Sbit(b));
		}
	}
	
	
	@Test public void testCreateValueBad() {
		byte[][] CASES = {
			{0},
			{0, 0},
			{0, 0, 0},
			{0, 0, 0, 0},
			{-1},
			{-1, -1},
			{-1, -1, -1},
			{-1, -1, -1, -1},
			{17},
			{17, 17},
			{17, 17, 17},
			{17, 17, 17, 17},
		};
		
		for (byte[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Sbit(cs));
		}
	}
	
	
	@Test public void testGetData() {
		String[] CASES = {
			"01",
			"02",
			"03",
			"08",
			"10",
			"02 03",
			"08 08",
			"10 10",
			"06 05 04",
			"08 08 08",
			"04 0D 07",
			"10 10 10",
			"08 08 08 08",
			"0D 0E 0A 0D",
			"10 10 10 10",
		};
		
		for (String cs : CASES) {
			byte[] b = hexToBytes(cs);
			assertArrayEquals(b, new Sbit(b).getData());
		}
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000004 73424954 0C 06 0D 01 FD5F9234", new Sbit(new byte[]{12, 6, 13, 1}));
	}
	
}
