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
			try {
				new Plte(new byte[cs]);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testGetData() {
		String[] CASES = {
			"000000",
			"010203",
			"FFFECA FEBABE",
		};
		
		for (String cs : CASES) {
			byte[] b = hexToBytes(cs);
			assertArrayEquals(b, new Plte(b).getData());
		}
	}
	
}
