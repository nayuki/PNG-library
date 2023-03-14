/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.TestUtil.hexToBytes;
import static io.nayuki.png.chunk.Chunk.CompressionMethod.ZLIB_DEFLATE;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
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
	
	
	@Test public void testCreateHuge() throws IOException {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		
		{  // The decompressed data is not a valid ICCP profile
			var bout = new ByteArrayOutputStream(2_147_483_566);
			bout.write(hexToBytes("789C"));
			var blockData = new byte[0xFFFF];
			Arrays.fill(blockData, (byte)'X');
			for (int i = 0; i < 32765; i++) {
				bout.write(hexToBytes("00FFFF0000"));
				bout.write(blockData);
			}
			bout.write(hexToBytes("01AFFF5000"));
			bout.write(blockData, 0, 0xFFAF);
			bout.write(hexToBytes("0105FEEA"));
			new Iccp("x".repeat(79), ZLIB_DEFLATE, bout.toByteArray());
		}
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		
		TestUtil.runExpect(IllegalArgumentException.class, () -> {
			try {  // The decompressed data is not a valid ICCP profile
				var bout = new ByteArrayOutputStream(2_147_483_567);
				bout.write(hexToBytes("789C"));
				var blockData = new byte[0xFFFF];
				Arrays.fill(blockData, (byte)'X');
				for (int i = 0; i < 32765; i++) {
					bout.write(hexToBytes("00FFFF0000"));
					bout.write(blockData);
				}
				bout.write(hexToBytes("01B0FF4F00"));
				bout.write(blockData, 0, 0xFFB0);
				bout.write(hexToBytes("0056FF42"));
				new Iccp("x".repeat(79), ZLIB_DEFLATE, bout.toByteArray());
			} catch (IOException e) {
				throw new AssertionError("Unreachable exception", e);
			}
		});
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
