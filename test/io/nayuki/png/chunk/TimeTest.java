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
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class TimeTest {
	
	@Test public void testCreateBad() {
		int[][] CASES = {
			// Year
			{-32769, 1, 1, 0, 0, 0},
			{ 65536, 1, 1, 0, 0, 0},
			// Month
			{2000, -129, 1, 0, 0, 0},
			{2000, -128, 1, 0, 0, 0},
			{2000,   -1, 1, 0, 0, 0},
			{2000,    0, 1, 0, 0, 0},
			{2000,   13, 1, 0, 0, 0},
			{2000,  128, 1, 0, 0, 0},
			{2000,  256, 1, 0, 0, 0},
			// Day
			{2000, 1, -129, 0, 0, 0},
			{2000, 1, -128, 0, 0, 0},
			{2000, 1,   -1, 0, 0, 0},
			{2000, 1,    0, 0, 0, 0},
			{2000, 1,   32, 0, 0, 0},
			{2000, 1,  128, 0, 0, 0},
			{2000, 1,  256, 0, 0, 0},
			// Hour
			{2000, 1, 1, -129, 0, 0},
			{2000, 1, 1, -128, 0, 0},
			{2000, 1, 1,   -1, 0, 0},
			{2000, 1, 1,   24, 0, 0},
			{2000, 1, 1,  128, 0, 0},
			{2000, 1, 1,  256, 0, 0},
			// Minute
			{2000, 1, 1, 0, -129, 0},
			{2000, 1, 1, 0, -128, 0},
			{2000, 1, 1, 0,   -1, 0},
			{2000, 1, 1, 0,   60, 0},
			{2000, 1, 1, 0,  128, 0},
			{2000, 1, 1, 0,  256, 0},
			// Second
			{2000, 1, 1, 0, 0, -129},
			{2000, 1, 1, 0, 0, -128},
			{2000, 1, 1, 0, 0,   -1},
			{2000, 1, 1, 0, 0,   61},
			{2000, 1, 1, 0, 0,  128},
			{2000, 1, 1, 0, 0,  256},
			// Multiple
			{-40000, -5, 1, 0, 0, 0},
			{1999, 4, 64, 13, 557, 64},
			{2050, 9, 30, -2, 88, 35},
			{99999, 13, 32, 24, 60, 61},
			{0, 0, 0, 0, 0, 0},
			{-1, -1, -1, -1, -1, -1},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Time(cs[0], cs[1], cs[2], cs[3], cs[4], cs[5]));
		}
	}
	
	
	@Test public void testGetData() {
		assertArrayEquals(hexToBytes("07CF 0C 1F 17 3B 3B"), new Time(1999, 12, 31, 23, 59, 59).getData());
		assertArrayEquals(hexToBytes("07D0 01 01 00 00 00"), new Time(2000,  1,  1,  0,  0,  0).getData());
		assertArrayEquals(hexToBytes("07F6 01 13 03 0E 07"), new Time(2038,  1, 19,  3, 14,  7).getData());
	}
	
}
