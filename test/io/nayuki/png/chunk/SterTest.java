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
import io.nayuki.png.chunk.Ster.Mode;


public final class SterTest {
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("00", new Ster(Mode.CROSS_FUSE_LAYOUT    ));
		TestUtil.assertDataEquals("01", new Ster(Mode.DIVERGING_FUSE_LAYOUT));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000001 73544552 01 B5E4B59C", new Ster(Mode.DIVERGING_FUSE_LAYOUT));
	}
	
}
