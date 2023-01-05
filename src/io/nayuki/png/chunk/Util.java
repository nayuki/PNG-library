/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * Utility functions for chunks. Not instantiable.
 */
public final class Util {
	
	public static Chunk readChunk(String type, int dataLen, DataInput in) throws IOException {
		return switch (type) {
			case Bkgd.TYPE -> Bkgd.read(dataLen, in);
			case Chrm.TYPE -> Chrm.read(         in);
			case Dsig.TYPE -> Dsig.read(dataLen, in);
			case Exif.TYPE -> Exif.read(dataLen, in);
			case Gama.TYPE -> Gama.read(         in);
			case Gifg.TYPE -> Gifg.read(         in);
			case Gifx.TYPE -> Gifx.read(dataLen, in);
			case Hist.TYPE -> Hist.read(dataLen, in);
			case Iccp.TYPE -> Iccp.read(dataLen, in);
			case Idat.TYPE -> Idat.read(dataLen, in);
			case Iend.TYPE -> Iend.SINGLETON;
			case Ihdr.TYPE -> Ihdr.read(         in);
			case Itxt.TYPE -> Itxt.read(dataLen, in);
			case Offs.TYPE -> Offs.read(         in);
			case Phys.TYPE -> Phys.read(         in);
			case Plte.TYPE -> Plte.read(dataLen, in);
			case Sbit.TYPE -> Sbit.read(dataLen, in);
			case Scal.TYPE -> Scal.read(dataLen, in);
			case Splt.TYPE -> Splt.read(dataLen, in);
			case Srgb.TYPE -> Srgb.read(         in);
			case Ster.TYPE -> Ster.read(         in);
			case Text.TYPE -> Text.read(dataLen, in);
			case Time.TYPE -> Time.read(         in);
			case Trns.TYPE -> Trns.read(dataLen, in);
			case Ztxt.TYPE -> Ztxt.read(dataLen, in);
			default -> Custom.read(type, dataLen, in);
		};
	}
	
	
	static byte[][] readAndSplitByNull(int dataLen, DataInput in, int numParts) throws IOException {
		var data = readBytes(in, dataLen);
		
		var result = new byte[numParts][];
		int start = 0;
		for (int i = 0; i < result.length - 1; i++) {
			int end = start;
			while (true) {
				if (end >= data.length)
					throw new IllegalArgumentException("Missing expected NUL byte");
				else if (data[end] == 0)
					break;
				else
					end++;
			}
			result[i] = Arrays.copyOfRange(data, start, end);
			start = end + 1;
		}
		result[result.length - 1] = Arrays.copyOfRange(data, start, data.length);
		return result;
	}
	
	
	static void checkKeyword(String s, boolean checkSpaces) {
		Objects.requireNonNull(s);
		if (!(1 <= s.length() && s.length() <= 79))
			throw new IllegalArgumentException("Invalid string length");
		if (checkSpaces && (s.startsWith(" ") || s.endsWith(" ") || s.contains("  ")))
			throw new IllegalArgumentException("String contains invalid spaces");
		checkIso8859_1(s, false);
	}
	
	
	static void checkIso8859_1(String s, boolean allowNewline) {
		Objects.requireNonNull(s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (32 <= c && c <= 126 || 161 <= c && c <= 255);
			else if (allowNewline && c == '\n');
			else throw new IllegalArgumentException("Invalid byte in ISO 8859-1 text");
		}
	}
	
	
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
	
	
	static byte[] readBytes(DataInput in, int len) throws IOException {
		var result = new byte[len];
		in.readFully(result);
		return result;
	}
	
	
	static <E> E indexInto(E[] array, int index) {
		if (0 <= index && index < array.length)
			return array[index];
		else
			throw new IllegalArgumentException("Unrecognized value for enumeration");
	}
	
	
	private Util() {}
	
}
