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
import org.junit.Test;
import io.nayuki.png.chunk.Ster.Mode;


public final class SterTest {
	
	@Test public void testGetData() {
		assertArrayEquals(hexToBytes("00"), new Ster(Mode.CROSS_FUSE_LAYOUT    ).getData());
		assertArrayEquals(hexToBytes("01"), new Ster(Mode.DIVERGING_FUSE_LAYOUT).getData());
	}
	
}
