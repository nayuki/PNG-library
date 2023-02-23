/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.Chunk.CompressionMethod.ZLIB_DEFLATE;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class IccpTest {
	
	@Test public void testCreateNameBad() {
		String[] CASES = {
			"",
			"今",
			"a".repeat(80),
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Iccp(cs, ZLIB_DEFLATE, TestUtil.hexToBytes("789C030000000001")));
		}
	}
	
	
	@Test public void testGetData() {
		// The decompressed data is not a valid ICCP profile
		TestUtil.assertDataEquals("4D6F6E69746F72 00 00 789C030000000001",
			new Iccp("Monitor", ZLIB_DEFLATE, TestUtil.hexToBytes("789C030000000001")));
	}
	
	
	@Test public void testWriteChunk() {
		// The decompressed data is not a valid ICCP profile
		TestUtil.assertChunkBytesEqual("0000001D 69434350 7072696E74206572 00 00 789C0B492D2EC9CC4B57303432060018640395 A74D358D",
			new Iccp("print er", ZLIB_DEFLATE, TestUtil.hexToBytes("789C0B492D2EC9CC4B57303432060018640395")));
	}
	
}
