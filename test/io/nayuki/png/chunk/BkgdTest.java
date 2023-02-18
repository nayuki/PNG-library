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


public final class BkgdTest {
	
	@Test public void testCreateBytesLengthBad() {
		int[] CASES = {
			0,
			3,
			4,
			5,
			7,
			8,
			9,
			10,
			1024,
		};
		
		for (int cs : CASES) {
			try {
				new Bkgd(new byte[cs]);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testCreateShortsLengthBad() {
		int[] CASES = {
			0,
			2,
			4,
			5,
			8,
			13,
			256,
		};
		
		for (int cs : CASES) {
			try {
				new Bkgd(new short[cs]);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testBytesGetData() {
		String[] CASES = {
			"00",
			"0102",
			"FFFE CAFE BABE",
		};
		
		for (String cs : CASES) {
			byte[] b = hexToBytes(cs);
			assertArrayEquals(b,  new Bkgd(b).getData());
		}
	}
	
	
	@Test public void testShortsGetData() {
		assertArrayEquals(hexToBytes("FEED"), new Bkgd(new short[]{(short)0xFEED}).getData());
		assertArrayEquals(hexToBytes("4321 FACE B00C"), new Bkgd(new short[]{(short)0x4321, (short)0xFACE, (short)0xB00C}).getData());
	}
	
}
