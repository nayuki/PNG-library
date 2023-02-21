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
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class ItxtTest {
	
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
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("617574686F72 00 00 00 454E 00 417574486F52 00 4E6179756B69",
			new Itxt("author", "EN", "AutHoR", Optional.empty(), "Nayuki".getBytes(StandardCharsets.UTF_8)));
	}
	
	
	@Test public void testWriteChunk() {
		assertArrayEquals(hexToBytes("00000040 69545874 5768617427732061206B6579776F72643F 00 00 00 66722D4652414E43452D30 00 5175276573742D636520717527756E206D6F742D636CC3A9203F 00 4E6FC3AB6C 1B3F8554"),
			TestUtil.writeChunkToBytes(new Itxt("What's a keyword?", "fr-FRANCE-0", "Qu'est-ce qu'un mot-clé ?", Optional.empty(), "Noël".getBytes(StandardCharsets.UTF_8))));
	}
	
}
