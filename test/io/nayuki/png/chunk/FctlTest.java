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


public final class FctlTest {
	
	@Test public void testCreateBad() {
		int[][] CASES = {
			{Integer.MIN_VALUE, 1, 1, 0, 0, 1, 1},
			{-1, 1, 1, 0, 0, 1, 1},
			
			{0, Integer.MIN_VALUE, 1, 0, 0, 1, 1},
			{0, -1, 1, 0, 0, 1, 1},
			{0, 0, 1, 0, 0, 1, 1},
			
			{0, 1, Integer.MIN_VALUE, 0, 0, 1, 1},
			{0, 1, -1, 0, 0, 1, 1},
			{0, 1, 0, 0, 0, 1, 1},
			
			{0, 1, 1, Integer.MIN_VALUE, 0, 1, 1},
			{0, 1, 1, -1, 0, 1, 1},
			
			{0, 1, 1, 0, Integer.MIN_VALUE, 1, 1},
			{0, 1, 1, 0, -1, 1, 1},
			
			{0, 1, 1, 0, 0, Integer.MIN_VALUE, 1},
			{0, 1, 1, 0, 0, -1, 1},
			{0, 1, 1, 0, 0, 0x10000, 1},
			{0, 1, 1, 0, 0, Integer.MAX_VALUE, 1},
			
			{0, 1, 1, 0, 0, 1, Integer.MIN_VALUE},
			{0, 1, 1, 0, 0, 1, -1},
			{0, 1, 1, 0, 0, 1, 0x10000},
			{0, 1, 1, 0, 0, 1, Integer.MAX_VALUE},
			
			{-2, 1, -5, 0, 0, 1, 1},
			{0, -3, 1, -10, 0, 1, 65536},
			{-1000, 0, 0, -2000000, -50000, -365, 24252425},
			{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE},
			{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class, () ->
				new Fctl(cs[0], cs[1], cs[2], cs[3], cs[4], cs[5], cs[6],
					Fctl.DisposeOperation.NONE, Fctl.BlendOperation.SOURCE));
		}
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000001A 6663544C 00007AB7 0000039E 0331B721 00000950 00001878 8428 031B 01 00 AB886FB9",
			new Fctl(31415, 926, 53589793, 2384, 6264, 33832, 795, Fctl.DisposeOperation.BACKGROUND, Fctl.BlendOperation.SOURCE));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("00000000 00000003 00000002 00000004 00000001 0005 0006 00 00",
			new Fctl(0, 3, 2, 4, 1, 5, 6, Fctl.DisposeOperation.NONE, Fctl.BlendOperation.SOURCE));
		
		TestUtil.assertDataEquals("04BBEF58 0000004F 0000002A 00000035 00000044 FFFE FFFF 02 01",
			new Fctl(79425368, 79, 42, 53, 68, 0xFFFE, 0xFFFF, Fctl.DisposeOperation.PREVIOUS, Fctl.BlendOperation.OVER));
	}
	
}
