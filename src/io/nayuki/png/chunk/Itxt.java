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
 * A international textual data (iTXt) chunk. This contains a keyword (ISO 8859-1), compression parameters,
 * language tag (ISO 8859-1), translated keyword (UTF-8), and text (UTF-8 with optional compression).
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11iTXt
 */
public record Itxt(
		String keyword,
		boolean compressionFlag,
		CompressionMethod compressionMethod,
		String languageTag,
		String translatedKeyword,
		byte[] text)
	implements Chunk {
	
	
	static final String TYPE = "iTXt";
	
	
	/*---- Constructor ----*/
	
	public Itxt {
		checkString(keyword);
		if (!(1 <= keyword.length() && keyword.length() <= 79) ||
				keyword.startsWith(" ") || keyword.endsWith(" ") || keyword.contains("  ") || keyword.contains("\n"))
			throw new IllegalArgumentException();
		
		checkString(languageTag);
		
		Objects.requireNonNull(translatedKeyword);
		for (int i = 0; i < translatedKeyword.length(); i++) {
			if (translatedKeyword.charAt(i) == '\0')
				throw new IllegalArgumentException();
		}
		
		if (compressionFlag) {
			switch (compressionMethod) {
				case DEFLATE:
					try {
						Util.decompressZlibDeflate(text);
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else if (compressionMethod != CompressionMethod.DEFLATE)
			throw new IllegalArgumentException();
		
		if (5L + keyword.length() + languageTag.length() + text.length +
				translatedKeyword.getBytes(StandardCharsets.UTF_8).length > Integer.MAX_VALUE)
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
	
	
	public static Itxt read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 4);
		if (parts[1].length < 2)
			throw new IllegalArgumentException();
		return new Itxt(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			switch (parts[1][0]) {
				case 0 -> false;
				case 1 -> true;
				default -> throw new IllegalArgumentException();
			},
			Util.indexInto(CompressionMethod.values(), parts[1][1]),
			new String(Arrays.copyOfRange(parts[1], 2, parts[1].length), StandardCharsets.ISO_8859_1),
			new String(parts[2], StandardCharsets.UTF_8),
			parts[3]);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 5 + keyword.length() + languageTag.length() + text.length
			+ translatedKeyword.getBytes(StandardCharsets.UTF_8).length;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(keyword.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(compressionFlag ? 1 : 0);
		out.writeByte(compressionMethod.ordinal());
		out.write(languageTag.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.write(translatedKeyword.getBytes(StandardCharsets.UTF_8));
		out.writeByte(0);
		out.write(text);
	}
	
}
