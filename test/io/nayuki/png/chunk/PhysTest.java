/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.chunk.TestUtil.hexToBytes;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Assert;
import org.junit.Test;
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
			try {
				new Phys(cs[0], cs[1], UnitSpecifier.METRE);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testGetData() {
		assertArrayEquals(hexToBytes("00000002 00000001 00"), new Phys(         2,          1, UnitSpecifier.UNKNOWN).getData());
		assertArrayEquals(hexToBytes("40000000 000FFFFF 01"), new Phys(1073741824,    1048575, UnitSpecifier.METRE  ).getData());
		assertArrayEquals(hexToBytes("0004CB2F 00287D95 00"), new Phys(    314159,    2653589, UnitSpecifier.UNKNOWN).getData());
		assertArrayEquals(hexToBytes("7FFFFFFF 7FFFFFFF 01"), new Phys(2147483647, 2147483647, UnitSpecifier.METRE  ).getData());
	}
	
}
