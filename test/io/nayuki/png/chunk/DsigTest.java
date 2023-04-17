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


public final class DsigTest {
	
	@Test public void testWriteChunk() {
		// All examples are random data and not real digital signatures
		TestUtil.assertChunkBytesEqual("0000000A 64534947 F260CC3E25F94EFEF936 E3ACEA20", new Dsig(TestUtil.hexToBytes("F260CC3E25F94EFEF936")));
	}
	
	
	@Test public void testWriteChunkData() {
		// All examples are random data and not real digital signatures
		TestUtil.assertDataEquals("18B8894CCF0C0271B989B47C769BEE210C89F08FA2", new Dsig(TestUtil.hexToBytes("18B8894CCF0C0271B989B47C769BEE210C89F08FA2")));
	}
	
}
