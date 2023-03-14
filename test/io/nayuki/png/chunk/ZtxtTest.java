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
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class ZtxtTest {
	
	@Test public void testCreateKeywordBad() {
		String[] CASES = {
			"",
			" ",
			" a",
			"b ",
			" c ",
			"d  e",
			"今",
			"a".repeat(80),
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Ztxt(cs, ZLIB_DEFLATE, hexToBytes("789C010100FEFF7800790079")));
		}
	}
	
	
	@Test public void testCreateDecompressionBad() {
		String[] CASES = {
			"0123456789ABCDEF",
			"789C010100FEFF7800790078",
			"789C0600000000",
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Ztxt("kw", ZLIB_DEFLATE, hexToBytes(cs)));
		}
	}
	
	
	@Test public void testCreateHuge() throws IOException {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		{
			var bout = new ByteArrayOutputStream(1_000_009_331);
			bout.write(hexToBytes("789C"));
			var blockData = new byte[0xFFFF];
			Arrays.fill(blockData, (byte)'.');
			for (int i = 0; i < 15258; i++) {
				bout.write(hexToBytes("00FFFF0000"));
				bout.write(blockData);
			}
			bout.write(hexToBytes("010000FFFF"));
			bout.write(hexToBytes("4C67F824"));
			new Ztxt("01".repeat(25), ZLIB_DEFLATE, bout.toByteArray());
		}
		{
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
			new Ztxt("x".repeat(79), ZLIB_DEFLATE, bout.toByteArray());
		}
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		
		TestUtil.runExpect(IllegalArgumentException.class, () -> {
			try {
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
				new Ztxt("x".repeat(79), ZLIB_DEFLATE, bout.toByteArray());
			} catch (IOException e) {
				throw new AssertionError("Unreachable exception", e);
			}
		});
	}
	
	
	@Test public void testGetText() {
		assertEquals("t3St!ng", new Ztxt("a", ZLIB_DEFLATE, hexToBytes("789C010700F8FF74335374216E67097A0265")).getText());
		assertEquals("ça", new Ztxt("a", ZLIB_DEFLATE, hexToBytes("789C010200FDFFE76102310149")).getText());
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("6B6579 00 00 789C53284BCC5150284D05000AD3027E",
			new Ztxt("key", ZLIB_DEFLATE, hexToBytes("789C 53284BCC5150284D0500 0AD3027E")));
		
		TestUtil.assertDataEquals("57686F27732074686174 00 00 789C010700F8FF506F6BE96D6F6E0D1F035E",
			new Ztxt("Who's that", ZLIB_DEFLATE, hexToBytes("789C 010700F8FF506F6BE96D6F6E 0D1F035E")));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000002A 7A545874 74686520515549434B2062726F776E 00 00 789C010E00F1FF466F58204A756D7073206F76457223EA04F9 DB2EAE8A",
			new Ztxt("the QUICK brown", ZLIB_DEFLATE, hexToBytes("789C 010E00F1FF466F58204A756D7073206F764572 23EA04F9")));
	}
	
}
