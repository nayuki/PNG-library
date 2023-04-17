/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.TestUtil.hexToBytes;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class PlteTest {
	
	@Test public void testCreateUnique() {
		new Plte(new byte[]{9, 0, 4});
		new Plte(new byte[]{0, 1, 2, 5, 4, 3, 8, 6, 7});
	}
	
	
	@Test public void testCreateUniqueAndDuplicate() {
		new Plte(new byte[]{9, 0, 4, 0, 0, 0, 0, 0, 0});
		new Plte(new byte[]{0, 1, 2, 5, 4, 3, 0, 1, 2, 8, 6, 7});
	}
	
	
	@Test public void testCreateDuplicate() {
		int[] CASES = {
			6,
			9,
			90,
			765,
			768,
		};
		
		for (int cs : CASES)
			new Plte(new byte[cs]);
	}
	
	
	@Test public void testCreateLengthBad() {
		int[] CASES = {
			0,
			1,
			2,
			4,
			5,
			7,
			20,
			769,
			771,
			1000,
		};
		
		for (int cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Plte(new byte[cs]));
		}
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000000C 504C5445 DDBC3884973CB4C47C499964 81402C90",
			new Plte(new byte[]{(byte)0xDD, (byte)0xBC, (byte)0x38, (byte)0x84, (byte)0x97, (byte)0x3C, (byte)0xB4, (byte)0xC4, (byte)0x7C, (byte)0x49, (byte)0x99, (byte)0x64}));
	}
	
	
	@Test public void testWriteChunkData() {
		String[] CASES = {
			"000000",
			"010203",
			"FFFECA FEBABE",
		};
		
		for (String cs : CASES) {
			byte[] b = hexToBytes(cs);
			TestUtil.assertDataEquals(b, new Plte(b));
		}
	}
	
}
