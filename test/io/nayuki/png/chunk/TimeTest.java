/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static org.junit.Assert.assertEquals;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
			{1900,  2,   29, 0, 0, 0},
			{2000,  1, -129, 0, 0, 0},
			{2000,  1, -128, 0, 0, 0},
			{2000,  1,   -1, 0, 0, 0},
			{2000,  1,    0, 0, 0, 0},
			{2000,  1,   32, 0, 0, 0},
			{2000,  1,  128, 0, 0, 0},
			{2000,  1,  256, 0, 0, 0},
			{2000,  2,   30, 0, 0, 0},
			{2000,  3,   32, 0, 0, 0},
			{2000,  4,   31, 0, 0, 0},
			{2000,  5,   32, 0, 0, 0},
			{2001,  2,   29, 0, 0, 0},
			{2004,  2,   30, 0, 0, 0},
			{2005, 11,   31, 0, 0, 0},
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
	
	
	@Test public void testCreateInstant() {
		{
			var time = new Time(OffsetDateTime.of(2000, 1, 2, 3, 4, 5, 0, ZoneOffset.UTC).toInstant());
			assertEquals(2000, time.year());
			assertEquals(1, time.month());
			assertEquals(2, time.day());
			assertEquals(3, time.hour());
			assertEquals(4, time.minute());
			assertEquals(5, time.second());
		}
		{
			var time = new Time(OffsetDateTime.of(1999, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC).toInstant());
			assertEquals(1999, time.year());
			assertEquals(12, time.month());
			assertEquals(31, time.day());
			assertEquals(23, time.hour());
			assertEquals(59, time.minute());
			assertEquals(59, time.second());
		}
	}
	
	
	@Test public void testToInstant() {
		{
			OffsetDateTime dt = new Time(2000, 1, 2, 3, 4, 5).toInstant().atOffset(ZoneOffset.UTC);
			assertEquals(2000, dt.getYear());
			assertEquals(1, dt.getMonthValue());
			assertEquals(2, dt.getDayOfMonth());
			assertEquals(3, dt.getHour());
			assertEquals(4, dt.getMinute());
			assertEquals(5, dt.getSecond());
		}
		{
			OffsetDateTime dt = new Time(1999, 12, 31, 23, 59, 60).toInstant().atOffset(ZoneOffset.UTC);
			assertEquals(1999, dt.getYear());
			assertEquals(12, dt.getMonthValue());
			assertEquals(31, dt.getDayOfMonth());
			assertEquals(23, dt.getHour());
			assertEquals(59, dt.getMinute());
			assertEquals(59, dt.getSecond());
		}
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000007 74494D45 07D4 07 0F 10 04 1D B4743272", new Time(2004, 7, 15, 16, 4, 29));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("07CF 0C 1F 17 3B 3B", new Time(1999, 12, 31, 23, 59, 59));
		TestUtil.assertDataEquals("07D0 01 01 00 00 00", new Time(2000,  1,  1,  0,  0,  0));
		TestUtil.assertDataEquals("07F6 01 13 03 0E 07", new Time(2038,  1, 19,  3, 14,  7));
	}
	
}
