/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utility functions for chunks. Not instantiable.
 */
final class Util {
	
	// Throws an exception if the string is invalid.
	static void checkKeyword(String s, boolean checkSpaces) {
		Objects.requireNonNull(s);
		if (!(1 <= s.length() && s.length() <= 79))
			throw new IllegalArgumentException("Invalid string length");
		if (checkSpaces && (s.startsWith(" ") || s.endsWith(" ") || s.contains("  ")))
			throw new IllegalArgumentException("String contains invalid spaces");
		checkIso8859_1(s, false);
	}
	
	
	// Throws an exception if the string is invalid.
	static void checkIso8859_1(String s, boolean allowNewline) {
		Objects.requireNonNull(s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (32 <= c && c <= 126 || 161 <= c && c <= 255);
			else if (allowNewline && c == '\n');
			else throw new IllegalArgumentException("Invalid byte in ISO 8859-1 text");
		}
	}
	
	
	// Adds the given integers / array lengths / string lengths,
	// ensuring the sum doesn't exceed Integer.MAX_VALUE.
	static int checkedLengthSum(Object... componentLengths) {
		Objects.requireNonNull(componentLengths);
		long result = 0;
		for (Object obj : componentLengths) {
			int n;
			if (obj instanceof Integer i)
				n = i.intValue();
			else if (obj instanceof byte[] b)
				n = b.length;
			else if (obj instanceof String s)
				n = s.length();
			else
				throw new IllegalArgumentException("Value has unrecognized type");
			
			if (n < 0)
				throw new AssertionError("Negative length");
			result += n;
			if (result > Integer.MAX_VALUE)
				throw new IllegalArgumentException("Data too long");
		}
		return Math.toIntExact(result);
	}
	
	
	// Classifies the given string which should be in scientific notation.
	static int testAsciiFloat(String s) {
		Objects.requireNonNull(s);
		Matcher m = ASCII_FLOAT.matcher(s);
		if (!m.matches())
			return -1;  // Invalid syntax
		else if (m.group(1).equals("-") || !NONZERO.matcher(m.group(2)).find())
			return 0;  // Negative or zero
		else
			return 1;  // Positive
	}
	
	private static final Pattern ASCII_FLOAT = Pattern.compile(
		"([+-]?)(\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?");
	
	private static final Pattern NONZERO = Pattern.compile("[1-9]");
	
	
	private Util() {}
	
}
