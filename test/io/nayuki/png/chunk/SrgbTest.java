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
import io.nayuki.png.chunk.Srgb.RenderingIntent;


public final class SrgbTest {
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("00", new Srgb(RenderingIntent.PERCEPTUAL            ));
		TestUtil.assertDataEquals("01", new Srgb(RenderingIntent.RELITAVIE_COLORIMETRIC));
		TestUtil.assertDataEquals("02", new Srgb(RenderingIntent.SATURATION            ));
		TestUtil.assertDataEquals("03", new Srgb(RenderingIntent.ABSOLUTE_COLORIMETRIC ));
	}
	
	
	@Test public void testWriteChunk() {
		assertArrayEquals(hexToBytes("00000001 73524742 03 37C74D53"), TestUtil.writeChunkToBytes(new Srgb(RenderingIntent.ABSOLUTE_COLORIMETRIC)));
	}
	
}
