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


public final class TextTest {
	
	@Test public void testCreateBad() {
		String[][] CASES = {
			{"", "x"},
			{" ", "x"},
			{" a", "x"},
			{"b ", "x"},
			{" c ", "x"},
			{"d  e", "x"},
			{"今", "x"},
			{"x", "楜"},
		};
		
		for (String[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Text(cs[0], cs[1]));
		}
	}
	
	
	@Test public void testCreateHuge() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		new Text(TestUtil.repeatString("01", 25), TestUtil.repeatString(".", 1_000_000_000));
		new Text(TestUtil.repeatString("x", 79), TestUtil.repeatString("X", 2_147_483_567));
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		TestUtil.runExpect(IllegalArgumentException.class,
			() -> new Text(TestUtil.repeatString("x", 79), TestUtil.repeatString("X", 2_147_483_568)));
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("410062E9", new Text("A", "bé"));
		TestUtil.assertDataEquals("6B6579 00 2076616C20207565", new Text("key", " val  ue"));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000001E 74455874 74686520515549434B2062726F776E 00 466F58204A756D7073206F764572 690D26EF", new Text("the QUICK brown", "FoX Jumps ovEr"));
	}
	
}
