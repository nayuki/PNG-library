/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.TestUtil.hexToBytes;
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class GifxTest {
	
	@Test public void testCreateLengthBad() {
		int[][] CASES = {
			{0, 0, 0},
			{8, 2, 7},
			{9, 3, 1},
			{10, 10, 5},
		};
		
		for (int[] cs : CASES)
			TestUtil.runExpect(IllegalArgumentException.class, () -> new Gifx(new byte[cs[0]], new byte[cs[1]], new byte[cs[2]]));
	}
	
	
	@Test public void testGetData() {
		// All examples are random data and not real application extensions
		TestUtil.assertDataEquals("EA03D5DF6B0554D3 E96F24", new Gifx(hexToBytes("EA03D5DF6B0554D3"), hexToBytes("E96F24"), hexToBytes("")));
		TestUtil.assertDataEquals("DB18421B804105AF 2A4827 DDEB522337B8C5E23DA3CA8A", new Gifx(hexToBytes("DB18421B804105AF"), hexToBytes("2A4827"), hexToBytes("DDEB522337B8C5E23DA3CA8A")));
	}
	
	
	@Test public void testWriteChunk() {
		// All examples are random data and not real application extensions
		TestUtil.assertChunkBytesEqual("00000010 67494678 6D4BF433D588EFB2 D50E4A FB34A6BE4D 7CB82263", new Gifx(hexToBytes("6D4BF433D588EFB2"), hexToBytes("D50E4A"), hexToBytes("FB34A6BE4D")));
	}
	
}
