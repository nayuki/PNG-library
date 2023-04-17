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


public final class GiftTest {
	
	@Test public void testCreateNumbersBad() {
		int[][] CASES = {
			{Integer.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0},
			{0, Integer.MIN_VALUE, 0, 0, 0, 0, 0, 0},
			{0, 0, Integer.MIN_VALUE, 0, 0, 0, 0, 0},
			{0, 0, 0, Integer.MIN_VALUE, 0, 0, 0, 0},
			{0, 0, 0, 0, Integer.MIN_VALUE, 0, 0, 0},
			{0, 0, 0, 0, 0, Integer.MIN_VALUE, 0, 0},
			{0, 0, 0, 0, 0, 0, Integer.MIN_VALUE, 0},
			{0, 0, 0, 0, 0, 0, 0, Integer.MIN_VALUE},
			{0, 0, 0, 0, -1, 0, 0, 0},
			{0, 0, 0, 0, 1 << 8, 0, 0, 0},
			{0, 0, 0, 0, Integer.MAX_VALUE, 0, 0, 0},
			{0, 0, 0, 0, 0, -1, 0, 0},
			{0, 0, 0, 0, 0, 1 << 8, 0, 0},
			{0, 0, 0, 0, 0, Integer.MAX_VALUE, 0, 0},
			{0, 0, 0, 0, 0, 0, -1, 0},
			{0, 0, 0, 0, 0, 0, 1 << 24, 0},
			{0, 0, 0, 0, 0, 0, Integer.MAX_VALUE, 0},
			{0, 0, 0, 0, 0, 0, 0, -1},
			{0, 0, 0, 0, 0, 0, 0, 1 << 24},
			{0, 0, 0, 0, 0, 0, 0, Integer.MAX_VALUE},
			{-1, -1, -1, -1, -1, -1, -1, -1},
			{0, 0, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE},
			{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class, () ->
				new Gift(cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6], cs[7], ""));
		}
	}
	
	
	@Test public void testCreateTextBad() {
		String[] CASES = {
			"\u0080",
			"ciné",
			"Aめ",
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Gift(0, 0, 0, 0, 0, 0, 0, 0, cs));
		}
	}
	
	
	@Test public void testCreateHuge() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		new Gift(0, 0, 0, 0, 0, 0, 0, 0, "-".repeat(2_147_483_623));
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		TestUtil.runExpect(IllegalArgumentException.class, () ->
			new Gift(0, 0, 0, 0, 0, 0, 0, 0, "-".repeat(2_147_483_624)));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("00000000 00000003 00000002 00000004 01 05 000006 000007",
			new Gift(0, 3, 2, 4, 1, 5, 6, 7, ""));
		
		TestUtil.assertDataEquals("0000004F 0000002A 00000035 00000044 FF FE 000300 000035 616263",
			new Gift(79, 42, 53, 68, 0xFF, 0xFE, 768, 53, "abc"));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000002B 67494674 00007AB7 0000039E 0331B721 00000950 3E 40 000D37 006D2E 54686520515549434B2062526F576E20666F58 C4051A76",
			new Gift(31415, 926, 53589793, 2384, 62, 64, 3383, 27950, "The QUICK bRoWn foX"));
	}
	
}
