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


public final class ActlTest {
	
	@Test public void testCreateBad() {
		int[][] CASES = {
			{0, 0},
			{1, -1},
			{0, -1},
			{-1, -1},
			{1, Integer.MIN_VALUE},
			{Integer.MIN_VALUE, 0},
			{Integer.MIN_VALUE, Integer.MIN_VALUE},
		};
		
		for (int[] cs : CASES)
			TestUtil.runExpect(IllegalArgumentException.class, () -> new Actl(cs[0], cs[1]));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("00000001 00000000", new Actl(1, 0));
		TestUtil.assertDataEquals("00000005 00000002", new Actl(5, 2));
		TestUtil.assertDataEquals("00000652 7FFFFFFF", new Actl(1618, Integer.MAX_VALUE));
		TestUtil.assertDataEquals("7FFFFFFF 01DF5E76", new Actl(Integer.MAX_VALUE, 31415926));
		TestUtil.assertDataEquals("7FFFFFFF 7FFFFFFF", new Actl(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000008 6163544C 00000001 00000000 B42DE9A0", new Actl(1, 0));
	}
	
}
