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


public final class FdatTest {
	
	@Test public void testCreateSequenceBad() {
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Fdat(-1, new byte[0]));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Fdat(Integer.MIN_VALUE, new byte[0]));
	}
	
	
	@Test public void testGetData() {
		TestUtil.assertDataEquals("00000000 1337C0DE9876", new Fdat(0, TestUtil.hexToBytes("1337C0DE9876")));
		TestUtil.assertDataEquals("0000000A", new Fdat(10, TestUtil.hexToBytes("")));
		TestUtil.assertDataEquals("7FFFFFFF 0000000000000000", new Fdat(Integer.MAX_VALUE, new byte[8]));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("00000011 66644154 00000141 789C010200FDFF010000040002 4D82D736",
			new Fdat(321, TestUtil.hexToBytes("789C010200FDFF010000040002")));
	}
	
}
