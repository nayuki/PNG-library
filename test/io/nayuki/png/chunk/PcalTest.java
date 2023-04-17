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
import io.nayuki.png.chunk.Pcal.EquationType;


public final class PcalTest {
	
	@Test public void testCreateBad() {
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("", "b", 0, 1, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal(" a", "b", 0, 1, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a".repeat(80), "b", 0, 1, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "米", 0, 1, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", 0, 0, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", Integer.MIN_VALUE, 0, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", 0, Integer.MIN_VALUE, EquationType.LINEAR, "0", "1"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", 0, 1, EquationType.LINEAR, "0"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", 0, 1, EquationType.LINEAR, "2", "3", "4"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", 1, 0, EquationType.LINEAR, "a", "+"));
		TestUtil.runExpect(IllegalArgumentException.class, () -> new Pcal("a", "b", 11, -22, EquationType.BASE_E_EXPONENTIAL, "98", "765"));
	}
	
	
	@Test public void testCreateHuge() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		new Pcal("X", "Y", 0, 1, EquationType.LINEAR, "2".repeat(1_073_741_816), "3".repeat(1_073_741_816));
	}
	
	
	@Test public void testCreateHugeBad() {
		if (!TestUtil.ENABLE_LARGE_MEMORY_TEST_CASES)
			return;
		TestUtil.runExpect(IllegalArgumentException.class, () ->
		new Pcal("X", "Y", 0, 1, EquationType.LINEAR, "2".repeat(1_073_741_816), "3".repeat(1_073_741_817)));
	}
	
	
	@Test public void testWriteChunkData() {
		TestUtil.assertDataEquals("616C6566 00 00000000 0000FFFF 00 02 6D65747265 00 31 00 32",
			new Pcal("alef", "metre", 0, 65535, EquationType.LINEAR, "1", "2"));
		
		TestUtil.assertDataEquals("626574 00 FFFFFFB3 01E97401 01 03 7365636F6E64 00 332E396534 00 2D2E356530 00 316530",
			new Pcal("bet", "second", -77, 32076801, EquationType.BASE_E_EXPONENTIAL, "3.9e4", "-.5e0", "1e0"));
		
		TestUtil.assertDataEquals("67696D656C 00 00083E4E FFFFFE00 02 03 6B696C6F6772616D 00 2E303030 00 38652B38 00 362E652D31",
			new Pcal("gimel", "kilogram", 540238, -512, EquationType.ARBITRARY_BASE_EXPONENTIAL, ".000", "8e+8", "6.e-1"));
		
		TestUtil.assertDataEquals("64616C6574 00 80000001 7FFFFFFF 03 04 616D70657265 00 31652D3939 00 322E 00 352E3737 00 2B342E36652B32",
			new Pcal("dalet", "ampere", Integer.MIN_VALUE + 1, Integer.MAX_VALUE, EquationType.HYPERBOLIC, "1e-99", "2.", "5.77", "+4.6e+2"));
	}
	
	
	@Test public void testWriteChunk() {
		TestUtil.assertChunkBytesEqual("0000002E 7043414C 416E676C65 00 1004868A FFFFE68F 01 03 415243204D494E555445 00 2B322E31652D34 00 38 00 2D2E3937652B303536 9C40D6D9",
			new Pcal("Angle", "ARC MINUTE", 268732042, -6513, EquationType.BASE_E_EXPONENTIAL, "+2.1e-4", "8", "-.97e+056"));
	}
	
}
