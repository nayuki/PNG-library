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


public final class HistTest {
	
	@Test public void testCreateLengthBad() {
		int[] CASES = {
			0,
			257,
			384,
			1024,
		};
		
		for (int cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Hist(new int[cs]));
		}
	}
	
	
	@Test public void testGetData() {
		for (int len = 1; len <= 256; len++) {
			var bytes = new byte[len * 2];
			var ints = new int[len];
			for (int i = 0; i < len; i++) {
				ints[i] = TestUtil.rand.nextInt(1 << 16);
				bytes[i * 2 + 0] = (byte)(ints[i] >>> 8);
				bytes[i * 2 + 1] = (byte)(ints[i] >>> 0);
			}
			TestUtil.assertDataEquals(bytes, new Hist(ints));
		}
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000000A 68495354 016A 2821 01EF 5E0B 0001 832B5A43",
			new Hist(new int[]{0x016A, 0x2821, 0x01EF, 0x5E0B, 0x0001}));
	}
	
}
