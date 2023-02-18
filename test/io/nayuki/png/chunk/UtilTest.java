/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static org.junit.Assert.assertEquals;
import org.junit.Assert;
import org.junit.Test;


public final class UtilTest {
	
	@Test public void testSplitByNul() {
		record Case(String input, int numParts, String... outputs) {}
		Case[] CASES = {
			new Case("", 1, ""),
			new Case("00", 1, "00"),
			new Case("FF", 1, "FF"),
			new Case("AA00BB", 1, "AA00BB"),
			new Case("AA00BB", 2, "AA", "BB"),
			new Case("00", 2, "", ""),
			new Case("00BB", 2, "", "BB"),
			new Case("AA00", 2, "AA", ""),
			new Case("DEAD00BE00EF", 3, "DEAD", "BE", "EF"),
			new Case("DEAD00BE00EF00", 3, "DEAD", "BE", "EF00"),
			new Case("DEAD00BE00EF000000CAFE", 6, "DEAD", "BE", "EF", "", "", "CAFE"),
		};
		
		for (Case cs : CASES) {
			byte[][] actual = Util.splitByNul(TestUtil.hexToBytes(cs.input), cs.numParts);
			assertEquals(cs.outputs.length, actual.length);
			for (int i = 0; i < actual.length; i++)
				Assert.assertArrayEquals(TestUtil.hexToBytes(cs.outputs[i]), actual[i]);
		}
	}
	
	
	@Test public void testSplitByNulBad() {
		Object[][] CASES = {
			{"", 2},
			{"49", 2},
			{"00", 3},
			{"1A", 3},
			{"110022", 3},
			{"00112233", 3},
			{"33112200", 3},
			{"0123456789", 5},
			{"0100234500006789", 5},
		};
		
		for (Object[] cs : CASES) {
			try {
				Util.splitByNul(TestUtil.hexToBytes((String)cs[0]), (int)cs[1]);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testCheckKeyword() {
		String[] CASES = {
			"a",
			"C B",
			"def ghi_jkl!@#$",
			"the quick brown fox jumps over the lazy dog",
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ`~!@#$%^&*()[]{}',.\"<>;:/=\\?+|-_",
			"0123456789012345678901234567890123456789012345678901234567890123456789012345678",
		};
		
		for (String cs : CASES)
			Util.checkKeyword(cs, true);
	}
	
	
	@Test public void testCheckKeywordBad() {
		String[] CASES = {
			"",
			" ",
			"  ",
			" A",
			"Z ",
			"C  D",
			" G H  I  ",
			"012345678901234567890123456789012345678901234567890123456789012345678901234567 ",
			"01234567890123456789012345678901234567890123456789012345678901234567890123456789",
			"abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz",
		};
		
		for (String cs : CASES) {
			try {
				Util.checkKeyword(cs, true);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testCheckedLengthSum() {
		record Case(int sum, Object... components) {}
		Case[] CASES = {
			new Case(2, 2),
			new Case(1, new byte[1]),
			new Case(3, "abc"),
			new Case(4, 1, "XYZ"),
			new Case(128, new byte[]{8, 2, 4, 1, 0, 5, 3, 7, 6, 9}, "alpha", "beta", "gamma", "delta", 99),
		};
		
		for (Case cs : CASES)
			assertEquals(cs.sum, Util.checkedLengthSum(cs.components));
	}
	
	
	@Test public void testCheckedLengthSumBad() {
		Object[][] CASES = {
			{2147483647, 1},
			{2147483647, 2147483647, 2147483647},
			{"hello", 2147483643},
			{1073741324, new byte[1000], 1073741324},
		};
		
		for (Object[] cs : CASES) {
			try {
				Util.checkedLengthSum(cs);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testTestAsciiFloat() {
		Object[][] CASES = {
			{-1, ""},
			{-1, "a"},
			{-1, "E"},
			{-1, " 5"},
			{-1, "8 "},
			{-1, "1 2"},
			{-1, "4. 3"},
			{-1, "0x0"},
			{-1, "--6"},
			{-1, "7+"},
			{-1, "9+e3"},
			
			{ 0, "0"},
			{ 0, "+0"},
			{ 0, "-0"},
			{ 0, "0.00"},
			{ 0, "0e0"},
			{ 0, ".0e1"},
			{ 0, "+0e-5"},
			
			{ 0, "-0.0e+2"},
			{ 0, "-1"},
			{ 0, "-2e3"},
			{ 0, "-3.1e-4"},
			{ 0, "-.6e99"},
			
			{ 1, "0.001"},
			{ 1, "1"},
			{ 1, "4e8"},
			{ 1, ".5e+10"},
			{ 1, "9.667e-3"},
		};
		
		for (Object[] cs : CASES)
			assertEquals((int)cs[0], Util.testAsciiFloat((String)cs[1]));
	}
	
}
