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


public final class IendTest {
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("", Iend.SINGLETON);
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000000 49454E44 AE426082", Iend.SINGLETON);
	}
	
}
