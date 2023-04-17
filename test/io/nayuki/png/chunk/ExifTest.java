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


public final class ExifTest {
	
	@Test public void testWriteChunkData() {
		// All examples are random data and not real Exif profiles
		TestUtil.assertDataEquals("19C4EAEC2AA8CC7BB5B17DFDCDF665EECE5B", new Exif(TestUtil.hexToBytes("19C4EAEC2AA8CC7BB5B17DFDCDF665EECE5B")));
	}
	
	
	@Test public void testWriteChunk() {
		// All examples are random data and not real Exif profiles
		TestUtil.assertChunkBytesEqual("00000009 65584966 C2ABF509891AACCB00 5C420B5B", new Exif(TestUtil.hexToBytes("C2ABF509891AACCB00")));
	}
	
}
