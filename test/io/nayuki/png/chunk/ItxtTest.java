/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.TestUtil.hexToBytes;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Test;
import io.nayuki.png.Chunk;
import io.nayuki.png.TestUtil;


public final class ItxtTest {
	
	@Test public void testCreateKeywordBad() {
		String[] CASES = {
			"",
			" ",
			" a",
			"b ",
			" c ",
			"d  e",
			"今",
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Itxt(cs, "", "", Optional.empty(), new byte[0]));
		}
	}
	
	
	@Test public void testCheckLanguageTag() {
		String[] CASES = {
			"",
			"cn",
			"EN",
			"en-uk",
			"no-bok",
			"x-klingon",
			"x-KlInGoN",
			"a-b-c-d-e-f-g",
			"7-9-0-6-8",
			"79425368",
			"the-quick-BROWN-f0x-JumPS-ov3r-teh-lazy-DOG-desuDESU",
		};
		
		for (String cs : CASES)
			Itxt.checkLanguageTag(cs);
	}
	
	
	@Test public void testCheckLanguageTagBad() {
		String[] CASES = {
			"-",
			"a-",
			"-b",
			"-c-",
			"def-ghi-",
			"x_y",
			"verylengthy",
			"u.v",
			"alpha beta",
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> Itxt.checkLanguageTag(cs));
		}
	}
	
	
	@Test public void testCreateDecompressBad() {
		String[] CASES = {
			"0123456789ABCDEF",
			"789C010100FEFF7800790078",
			"789C0600000000",
		};
		
		for (String cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Itxt("kw", "", "", Optional.of(Chunk.CompressionMethod.ZLIB_DEFLATE), hexToBytes(cs)));
		}
	}
	
	
	@Test public void testCreateHuge() throws IOException {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		new Itxt(TestUtil.repeatString("a", 79), "bbbbbbbb" + TestUtil.repeatString("-bbbbbbbb", 79_536_427), TestUtil.repeatString("c", 715_827_856),
			Optional.empty(), TestUtil.repeatString("d", 715_827_856).getBytes(StandardCharsets.US_ASCII));
	}
	
	
	@Test public void testCreateHugeBad() throws IOException {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		TestUtil.runExpect(IllegalArgumentException.class, () ->
			new Itxt(TestUtil.repeatString("a", 79), "bbbbbbbb" + TestUtil.repeatString("-bbbbbbbb", 79_536_427), TestUtil.repeatString("c", 715_827_856),
				Optional.empty(), TestUtil.repeatString("d", 715_827_857).getBytes(StandardCharsets.US_ASCII)));
	}
	
	
	@Test public void testGetText() {
		assertEquals("t3St!ng", new Itxt("a", "", "", Optional.empty(), hexToBytes("74335374216E67")).getText());
		assertEquals("ça뉭", new Itxt("a", "", "", Optional.of(Chunk.CompressionMethod.ZLIB_DEFLATE), hexToBytes("789C010600F9FFC3A761EB89AD0DDF03ED")).getText());
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("617574686F72 00 00 00 454E 00 417574486F52 00 4E6179756B69",
			new Itxt("author", "EN", "AutHoR", Optional.empty(), "Nayuki".getBytes(StandardCharsets.UTF_8)));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000040 69545874 5768617427732061206B6579776F72643F 00 00 00 66722D4652414E43452D30 00 5175276573742D636520717527756E206D6F742D636CC3A9203F 00 4E6FC3AB6C 1B3F8554",
			new Itxt("What's a keyword?", "fr-FRANCE-0", "Qu'est-ce qu'un mot-clé ?", Optional.empty(), "Noël".getBytes(StandardCharsets.UTF_8)));
	}
	
}
