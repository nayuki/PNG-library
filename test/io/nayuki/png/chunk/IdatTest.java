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


public final class IdatTest {
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("789C010200FDFF010000040002", new Idat(TestUtil.hexToBytes("789C010200FDFF010000040002")));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000000D 49444154 789C010200FDFF010000040002 DAAD708D", new Idat(TestUtil.hexToBytes("789C010200FDFF010000040002")));
	}
	
}
