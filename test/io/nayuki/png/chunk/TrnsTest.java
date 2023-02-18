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


public final class TrnsTest {
	
	@Test public void testCreateBytesLengthBad() {
		int[] CASES = {
			257,
			384,
			1024,
		};
		
		for (int cs : CASES) {
			try {
				new Trns(new byte[cs]);
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
				new Trns(new short[cs]);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testBytesGetData() {
		for (int len = 0; len <= 256; len++) {
			var b = new byte[len];
			TestUtil.rand.nextBytes(b);
			assertArrayEquals(b, new Trns(b).getData());
		}
	}
	
	
	@Test public void testShortsGetData() {
		assertArrayEquals(hexToBytes("FEED"), new Trns(new short[]{(short)0xFEED}).getData());
		assertArrayEquals(hexToBytes("4321 FACE B00C"), new Trns(new short[]{(short)0x4321, (short)0xFACE, (short)0xB00C}).getData());
	}
	
}