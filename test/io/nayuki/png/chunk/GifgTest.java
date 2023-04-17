/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class GifgTest {
	
	@Test public void testCreateBad() {
		int[][] CASES = {
			{Integer.MIN_VALUE, 1},
			{-1, 1},
			{8, 1},
			{256, 1},
			{Integer.MAX_VALUE, 1},
			{0, Integer.MIN_VALUE},
			{0, -1},
			{0, 0x10000},
			{0, 31415926},
			{0, Integer.MAX_VALUE},
		};
		
		for (int[] cs : CASES)
			TestUtil.runExpect(IllegalArgumentException.class, () -> new Gifg(cs[0], false, cs[1]));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("00 00 0001", new Gifg(0, false,      1));
		TestUtil.assertDataEquals("01 01 8421", new Gifg(1, true , 0x8421));
		TestUtil.assertDataEquals("02 00 0147", new Gifg(2, false,    327));
		TestUtil.assertDataEquals("03 01 FFFF", new Gifg(3, true , 0xFFFF));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000004 67494667 01 00 0002 CA3E0204", new Gifg(1, false, 2));
	}
	
}
