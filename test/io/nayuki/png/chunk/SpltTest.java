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


public final class SpltTest {
	
	@Test public void testCreateNameBad() {
		String[] CASES = {
			"",
			" ",
			" a",
			"b ",
			" c ",
			"d  e",
			"今",
			"a".repeat(80),
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Splt(cs, 8, new byte[0]));
		}
	}
	
	
	@Test public void testCreateDepthLengthBad() {
		int[][] CASES = {
			{Integer.MIN_VALUE, 0},
			{-1,  0},
			{ 0,  0},
			{ 1,  0},
			{ 1,  1},
			{ 5,  0},
			{ 5,  2},
			{ 5,  6},
			{ 8,  1},
			{ 8,  2},
			{ 8,  3},
			{ 8,  4},
			{ 8,  5},
			{ 8,  7},
			{ 8, 10},
			{12,  0},
			{12,  3},
			{12,  8},
			{16,  1},
			{16,  2},
			{16,  3},
			{16,  4},
			{16,  5},
			{16,  6},
			{16,  7},
			{16,  8},
			{16,  9},
			{16, 11},
			{16, 16},
			{Integer.MAX_VALUE, 0},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Splt("a", cs[0], new byte[cs[1]]));
		}
	}
	
	
	@Test public void testCreateHuge() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		new Splt("a".repeat(77), 8, new byte[357_913_928 * 6]);
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		TestUtil.runExpect(IllegalArgumentException.class, () ->
			new Splt("a".repeat(78), 8, new byte[357_913_928 * 6]));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("666F6F626172 00 08 001234556677 89ABCDEF1011",
			new Splt("foobar", 8, TestUtil.hexToBytes("001234556677 89ABCDEF1011")));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000010 73504C54 51555834 00 10 FFEEEE3D00547A818256 E6D23504",
			new Splt("QUX4", 16, TestUtil.hexToBytes("FFEEEE3D00547A818256")));
	}
	
}
