/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import org.junit.Assert;
import org.junit.Test;


public final class ChunkTest {
	
	@Test public void testCheckType() {
		String[] CASES = {
			//"abcd",
			//"hgfE",
			"ikLj",
			"moNP",
			//"sQrt",
			//"wVuX",
			"yZZy",
			"oOOO",
			//"Test",
			//"TinG",
			//"FoUr",
			"FiVE",
			//"NIne",
			//"THrE",
			//"TWOo",
			"ZYXW",
		};
		
		for (String cs : CASES)
			Chunk.checkType(cs);
	}
	
	
	@Test public void testCheckTypeBad() {
		String[] CASES = {
			// Wrong length
			"",
			"a",
			"bc",
			"deF",
			"GHIJK",
			"ZYXWVU",
			// Wrong characters
			"\0",
			"B&",
			"0123",
			"\r\n\r\n",
			"ABCD\0",
			"u--v",
			"_G_F",
			"(!*)",
			// Reserved type
			"abcd",
			"hgfE",
			"sQrt",
			"wVuX",
			"Test",
			"TinG",
			"NIne",
			"THrE",
			// Critical but safe to copy
			"FoUr",
			"TWOo",
		};
		
		for (String cs : CASES) {
			try {
				Chunk.checkType(cs);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
}
