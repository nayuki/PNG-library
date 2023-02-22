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
import io.nayuki.png.chunk.Scal.UnitSpecifier;


public final class ScalTest {
	
	@Test public void testCreateBad() {
		String[][] CASES = {
			{"", ""},
			{"0", "0"},
			{"0.0001", ".00"},
			{"0", "1"},
			{"1", "0"},
			{"3", "0e5"},
			{"000.e-2", "4"},
			{"-0", "87"},
			{"-1", "-2"},
			{"6", "-0.00200e6"},
		};
		
		for (String[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Scal(UnitSpecifier.METRE, cs[0], cs[1]));
		}
	}
	
	
	@Test public void testCreateHuge() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		new Scal(UnitSpecifier.RADIAN, TestUtil.repeatString("1", 1_146_741_823), TestUtil.repeatString("2", 1_000_741_822));
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		TestUtil.runExpect(IllegalArgumentException.class, () ->
			new Scal(UnitSpecifier.RADIAN, TestUtil.repeatString("1", 1_146_741_823), TestUtil.repeatString("2", 1_000_741_823)));
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("01 37 00 2B2E3031652B35", new Scal(UnitSpecifier.METRE, "7", "+.01e+5"));
		TestUtil.assertDataEquals("02 39392E6530 00 322E3433652D3037", new Scal(UnitSpecifier.RADIAN, "99.e0", "2.43e-07"));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000015 7343414C 01 2B323335372E3131 00 2E3836343230652B3031 3934AF8043",
			new Scal(UnitSpecifier.METRE, "+2357.11", ".86420e+019"));
	}
	
}
