/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Assert;
import org.junit.Test;


public final class HistTest {
	
	@Test public void testCreateLengthBad() {
		int[] CASES = {
			0,
			257,
			384,
			1024,
		};
		
		for (int cs : CASES) {
			try {
				new Hist(new short[cs]);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
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
	
}
