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
import io.nayuki.png.chunk.Offs.UnitSpecifier;


public final class OffsTest {
	
	@Test public void testCreateBad() {
		int[][] CASES = {
			{0, Integer.MIN_VALUE},
			{Integer.MIN_VALUE, 0},
			{Integer.MIN_VALUE, Integer.MIN_VALUE},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Offs(cs[0], cs[1], UnitSpecifier.MICROMETRE));
		}
	}
	
	
	@Test public void testGetData() {
		assertArrayEquals(hexToBytes("00000000 00000000 00"), new Offs(          0,           0, UnitSpecifier.PIXEL     ).getData());
		assertArrayEquals(hexToBytes("00000002 FFFFFFFF 01"), new Offs(          2,          -1, UnitSpecifier.MICROMETRE).getData());
		assertArrayEquals(hexToBytes("FFFB34D1 00287D95 00"), new Offs(    -314159,     2653589, UnitSpecifier.PIXEL     ).getData());
		assertArrayEquals(hexToBytes("7FFFFFFF 7FFFFFFF 01"), new Offs( 2147483647,  2147483647, UnitSpecifier.MICROMETRE).getData());
		assertArrayEquals(hexToBytes("80000001 80000001 00"), new Offs(-2147483647, -2147483647, UnitSpecifier.PIXEL     ).getData());
	}
	
}
