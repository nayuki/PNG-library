/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A compressed textual data (zTXt) chunk. This contains a keyword and
 * compressed text string in the ISO 8859-1 character set. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11zTXt
 */
public record Ztxt(
		String keyword,
		CompressionMethod compressionMethod,
		byte[] compressedText)
	implements Chunk {
	
	
	static final String TYPE = "zTXt";
	
	
	/*---- Constructor ----*/
	
	public Ztxt {
		checkString(keyword);
		if (!(1 <= keyword.length() && keyword.length() <= 79) ||
				keyword.startsWith(" ") || keyword.endsWith(" ") || keyword.contains("  ") || keyword.contains("\n"))
			throw new IllegalArgumentException();
		
		Objects.requireNonNull(compressionMethod);
		byte[] decompText = switch (compressionMethod) {
			case DEFLATE -> {
				try {
					yield Util.decompressZlibDeflate(compressedText);
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}
			default -> throw new IllegalArgumentException();
		};
		
		String text = new String(decompText, StandardCharsets.ISO_8859_1);
		checkString(text);
		if (2L + keyword.length() + compressedText.length > Integer.MAX_VALUE)
			throw new IllegalArgumentException();
	}
	
	
	private static void checkString(String s) {
		Objects.requireNonNull(s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!(c == '\n' || 32 <= c && c <= 126 || 161 <= c && c <= 255))
				throw new IllegalArgumentException();
		}
	}
	
	
	public static Ztxt read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 2);
		
		String keyword = new String(parts[0], StandardCharsets.ISO_8859_1);
		if (parts[1].length < 1)
			throw new IllegalArgumentException();
		CompressionMethod compMethod = Util.indexInto(CompressionMethod.values(), parts[1][0]);
		byte[] compText = Arrays.copyOfRange(parts[1], 1, parts[1].length);
		
		return new Ztxt(keyword, compMethod, compText);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return keyword.length() + 2 + compressedText.length;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(keyword.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(compressionMethod.ordinal());
		out.write(compressedText);
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum CompressionMethod {
		DEFLATE,
	}
	
}
