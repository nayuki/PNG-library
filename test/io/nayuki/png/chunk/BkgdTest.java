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
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Bkgd(new byte[cs]));
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
				() -> new Bkgd(new short[cs]));
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
			assertArrayEquals(b, new Bkgd(b).getData());
		}
	}
	
	
	@Test public void testShortsGetData() {
		TestUtil.assertDataEquals("FEED", new Bkgd(new short[]{(short)0xFEED}));
		TestUtil.assertDataEquals("4321 FACE B00C", new Bkgd(new short[]{(short)0x4321, (short)0xFACE, (short)0xB00C}));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000001 624B4744 85 15D76AE7", new Bkgd(new byte[]{(byte)0x85}));
		TestUtil.assertChunkBytesEqual("00000002 624B4744 049D 405F09CF", new Bkgd(new short[]{(short)0x049D}));
	}
	
}
