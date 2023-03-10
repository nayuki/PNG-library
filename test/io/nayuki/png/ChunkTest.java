/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import org.junit.Test;
import io.nayuki.png.chunk.Chunk;


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
			"一二三四",
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
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> Chunk.checkType(cs));
		}
	}
	
}
