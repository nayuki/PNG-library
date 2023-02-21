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
import io.nayuki.png.chunk.Phys.UnitSpecifier;


public final class PhysTest {
	
	@Test public void testCreateBad() {
		int[][] CASES = {
			{0, 0},
			{0, 1},
			{1, 0},
			{2, Integer.MIN_VALUE},
			{Integer.MIN_VALUE, 3},
			{Integer.MIN_VALUE, Integer.MIN_VALUE},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Phys(cs[0], cs[1], UnitSpecifier.METRE));
		}
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("00000002 00000001 00", new Phys(         2,          1, UnitSpecifier.UNKNOWN));
		TestUtil.assertDataEquals("40000000 000FFFFF 01", new Phys(1073741824,    1048575, UnitSpecifier.METRE  ));
		TestUtil.assertDataEquals("0004CB2F 00287D95 00", new Phys(    314159,    2653589, UnitSpecifier.UNKNOWN));
		TestUtil.assertDataEquals("7FFFFFFF 7FFFFFFF 01", new Phys(2147483647, 2147483647, UnitSpecifier.METRE  ));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000009 70485973 638F0B6D 3B5446BD 01 041F4428", new Phys(1670318957, 995378877, UnitSpecifier.METRE));
	}
	
}
