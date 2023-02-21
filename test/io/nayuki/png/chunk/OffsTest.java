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
		TestUtil.assertDataEquals("00000000 00000000 00", new Offs(          0,           0, UnitSpecifier.PIXEL     ));
		TestUtil.assertDataEquals("00000002 FFFFFFFF 01", new Offs(          2,          -1, UnitSpecifier.MICROMETRE));
		TestUtil.assertDataEquals("FFFB34D1 00287D95 00", new Offs(    -314159,     2653589, UnitSpecifier.PIXEL     ));
		TestUtil.assertDataEquals("7FFFFFFF 7FFFFFFF 01", new Offs( 2147483647,  2147483647, UnitSpecifier.MICROMETRE));
		TestUtil.assertDataEquals("80000001 80000001 00", new Offs(-2147483647, -2147483647, UnitSpecifier.PIXEL     ));
	}
	
	
	@Test public void testWriteChunk() {
		assertArrayEquals(hexToBytes("00000009 6F464673 B404CBCE 2E884CC4 00 2E1AF44F"), TestUtil.writeChunkToBytes(new Offs(-1274754098, 780684484, UnitSpecifier.PIXEL)));
	}
	
}
