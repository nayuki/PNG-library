/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.nayuki.png.Chunk;


/**
 * Utility functions for chunks. Not instantiable.
 */
public final class Util {
	
	/**
	 * Reads from the specified chunk reader and returns a chunk object representing the
	 * data that is read. If the chunk reader holds a type that corresponds to a known class
	 * in this package ({@link io.nayuki.png.chunk}), then this function calls the {@code
	 * read()} function of that class to parse and return a chunk of that type. For example,
	 * calling this function with the chunk type "IHDR" will return an object of the class {@link
	 * Ihdr} or throw an {@code IllegalArgumentException} if the data is invalid. Otherwise
	 * if the chunk type is not of a known type, then a {@link Custom} chunk is returned.
	 * @param in the chunk reader to read the chunk's data from (not {@code null})
	 * @return a chunk object representing the data parsed from the chunk reader (not {@code null})
	 * @throws NullPointerException if the chunk reader is {@code null}
	 * @throws IllegalArgumentException if the chunk contains invalid data
	 * @throws IOException if an I/O exception occurs
	 */
	public static Chunk readChunk(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		Chunk result = switch (in.getType()) {
			case Actl.TYPE -> Actl.read(in);
			case Bkgd.TYPE -> Bkgd.read(in);
			case Chrm.TYPE -> Chrm.read(in);
			case Dsig.TYPE -> Dsig.read(in);
			case Exif.TYPE -> Exif.read(in);
			case Fctl.TYPE -> Fctl.read(in);
			case Fdat.TYPE -> Fdat.read(in);
			case Gama.TYPE -> Gama.read(in);
			case Gifg.TYPE -> Gifg.read(in);
			case Gift.TYPE -> Gift.read(in);
			case Gifx.TYPE -> Gifx.read(in);
			case Hist.TYPE -> Hist.read(in);
			case Iccp.TYPE -> Iccp.read(in);
			case Idat.TYPE -> Idat.read(in);
			case Iend.TYPE -> Iend.SINGLETON;
			case Ihdr.TYPE -> Ihdr.read(in);
			case Itxt.TYPE -> Itxt.read(in);
			case Offs.TYPE -> Offs.read(in);
			case Pcal.TYPE -> Pcal.read(in);
			case Phys.TYPE -> Phys.read(in);
			case Plte.TYPE -> Plte.read(in);
			case Sbit.TYPE -> Sbit.read(in);
			case Scal.TYPE -> Scal.read(in);
			case Splt.TYPE -> Splt.read(in);
			case Srgb.TYPE -> Srgb.read(in);
			case Ster.TYPE -> Ster.read(in);
			case Text.TYPE -> Text.read(in);
			case Time.TYPE -> Time.read(in);
			case Trns.TYPE -> Trns.read(in);
			case Ztxt.TYPE -> Ztxt.read(in);
			default -> Custom.read(in);
		};
		in.finish();
		return result;
	}
	
	
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
	
	
	// Returns a new array of fully reading the given number of bytes.
	static byte[] readBytes(DataInput in, int len) throws IOException {
		var result = new byte[len];
		in.readFully(result);
		return result;
	}
	
	
	// Returns the given array element or throws a specific exception.
	static <E> E indexInto(E[] array, int index) {
		if (0 <= index && index < array.length)
			return array[index];
		else
			throw new IllegalArgumentException("Unrecognized value for enumeration");
	}
	
	
	private Util() {}
	
}
