/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class GamaTest {
	
	@Test public void testCreateIntBad() {
		int[] CASES = {
			0,
			-1,
			-200,
			Integer.MIN_VALUE + 1,
			Integer.MIN_VALUE,
		};
		
		for (int cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Gama(cs));
		}
	}
	
	
	@Test public void testCreateDouble() {
		Object[][] CASES = {
			{    0.000006,          1},
			{    0.00001 ,          1},
			{    0.000023,          2},
			{    0.000027,          3},
			{    0.45455 ,      45455},
			{    1.00000 ,     100000},
			{21474.83647 , 2147483647},
			{21474.836474, 2147483647},
		};
		
		for (Object[] cs : CASES)
			assertEquals((int)cs[1], new Gama((double)cs[0]).gamma());
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
			0.000004,
			0.0,
			-0.0,
			-0.1,
			-1.0,
			-200.0,
			-21474.83648,
			-42948.67296,
			-42949.67296,
		};
		
		for (double cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Gama(cs));
		}
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("00000001", new Gama(         1));
		TestUtil.assertDataEquals("0000B18F", new Gama(     45455));
		TestUtil.assertDataEquals("000186A0", new Gama(    100000));
		TestUtil.assertDataEquals("7FFFFFFF", new Gama(2147483647));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000004 67414D41 000004E8 4198CF03", new Gama(1256));
	}
	
}
