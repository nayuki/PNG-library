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


public final class CustomTest {
	
	@Test public void testCreateTypeBad() {
		String[] CASES = {
			"",
			"deF",
			"0123",
			"GHIJK",
			"一二三四",
			"Test",
			"TWOo",
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Custom(cs, new byte[0]));
		}
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("AABC10", new Custom("asDf", TestUtil.hexToBytes("AABC10")));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000008 68654C4F 5D46746BE197D8B9 0BCFAED3", new Custom("heLO", TestUtil.hexToBytes("5D46746BE197D8B9")));
		TestUtil.assertChunkBytesEqual("00000003 49484452 000000 63835E75", new Custom("IHDR", TestUtil.hexToBytes("000000")));
		TestUtil.assertChunkBytesEqual("00000009 49454E44 66614B652144617461 73EAEA86", new Custom("IEND", TestUtil.hexToBytes("66614B652144617461")));
	}
	
}
