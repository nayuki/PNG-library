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
				() -> new Hist(new short[cs]));
		}
	}
	
	
	@Test public void testGetData() {
		for (int len = 1; len <= 256; len++) {
			var bytes = new byte[len * 2];
			var shorts = new short[len];
			for (int i = 0; i < len; i++) {
				int val = TestUtil.rand.nextInt(1 << 16);
				bytes[i * 2 + 0] = (byte)(val >>> 8);
				bytes[i * 2 + 1] = (byte)(val >>> 0);
				shorts[i] = (short)val;
			}
			assertArrayEquals(bytes, new Hist(shorts).getData());
		}
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000000A 68495354 016A 2821 01EF 5E0B 0001 832B5A43",
			new Hist(new short[]{(short)0x016A, (short)0x2821, (short)0x01EF, (short)0x5E0B, (short)0x0001}));
	}
	
}
