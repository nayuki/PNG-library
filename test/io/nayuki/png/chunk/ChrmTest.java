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
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class ChrmTest {
	
	@Test public void testCreateIntBad() {
		int[] CASES = {
			-1,
			-200,
			Integer.MIN_VALUE + 1,
			Integer.MIN_VALUE,
		};
		
		for (int cs : CASES) {
			for (int i = 0; i < 8; i++) {
				var a = new int[8];
				Arrays.fill(a, 50000);
				a[i] = cs;
				TestUtil.runExpect(IllegalArgumentException.class,
					() -> new Chrm(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]));
			}
		}
	}
	
	
	@Test public void testCreateDoubleBad() {
		double[] CASES = {
			Double.NaN,
			Double.POSITIVE_INFINITY,
			Double.NEGATIVE_INFINITY,
			184467440737096.51616,
			184467440737095.51616,
			42950.67296,
			42949.67296,
			21474.836476,
			-0.000006,
			-0.1,
			-1.0,
			-200.0,
			-21474.83648,
			-42948.67296,
			-42949.67296,
		};
		
		for (double cs : CASES) {
			for (int i = 0; i < 8; i++) {
				var a = new double[8];
				Arrays.fill(a, 0.5);
				a[i] = cs;
				TestUtil.runExpect(IllegalArgumentException.class,
					() -> new Chrm(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7]));
			}
		}
	}
	
	
	@Test public void testCreateDouble() {
		var chrm = new Chrm(
			    0.000006,
			    0.00001 ,
			    0.000023,
			    0.000027,
			    0.45455 ,
			    1.00000 ,
			21474.83647 ,
			21474.836474);
		assertEquals(         1, chrm.whitePointX());
		assertEquals(         1, chrm.whitePointY());
		assertEquals(         2, chrm.redX       ());
		assertEquals(         3, chrm.redY       ());
		assertEquals(     45455, chrm.greenX     ());
		assertEquals(    100000, chrm.greenY     ());
		assertEquals(2147483647, chrm.blueX      ());
		assertEquals(2147483647, chrm.blueY      ());
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("00000001 0000B18F 000186A0 7FFFFFFF 002FEFD8 00006A2F 00000000 00035B60",
			new Chrm(1, 45455, 100000, 2147483647, 3141592, 27183, 0, 220000));
	}
	
	
	@Test public void testWriteChunk() {
		assertArrayEquals(hexToBytes("00000020 6348524D 00000009 0000001B 0000276D 0EB9EA98 0006ED4D 7608BC41 0000000F 0000000E 5B866230"),
			TestUtil.writeChunkToBytes(new Chrm(9, 27, 10093, 247065240, 453965, 1980283969, 15, 14)));
	}
	
}
