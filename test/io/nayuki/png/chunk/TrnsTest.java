/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class TrnsTest {
	
	@Test public void testCreateBytesLengthBad() {
		int[] CASES = {
			257,
			384,
			1024,
		};
		
		for (int cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Trns(new byte[cs]));
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
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Trns(new short[cs]));
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
		TestUtil.assertDataEquals("FEED", new Trns(new short[]{(short)0xFEED}));
		TestUtil.assertDataEquals("4321 FACE B00C", new Trns(new short[]{(short)0x4321, (short)0xFACE, (short)0xB00C}));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000008 74524E53 01 47 CC 0E 3A 23 A1 7E 86C74889",
			new Trns(new byte[]{(byte)0x01, (byte)0x47, (byte)0xCC, (byte)0x0E, (byte)0x3A, (byte)0x23, (byte)0xA1, (byte)0x7E}));
	}
	
}
