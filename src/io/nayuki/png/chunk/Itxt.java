/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A international textual data (iTXt) chunk. This contains a keyword (ISO
 * 8859-1), compression parameters, language tag (ISO 8859-1), translated
 * keyword (UTF-8), and text (UTF-8 with optional compression). Instances
 * should be treated as immutable, but arrays are not copied defensively.
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
		Util.checkKeyword(keyword, true);
		
		Objects.requireNonNull(languageTag);
		if (!languageTag.matches("(?:[A-Za-z0-9]{1,8}(?:-[A-Za-z0-9]{1,8})*)?"))
			throw new IllegalArgumentException("Invalid language tag syntax");
		
		Objects.requireNonNull(translatedKeyword);
		for (int i = 0; i < translatedKeyword.length(); i++) {
			if (translatedKeyword.charAt(i) == '\0')
				throw new IllegalArgumentException("NUL character in translated keyword");
		}
		
		byte[] decompText;
		if (compressionFlag)
			decompText = compressionMethod.decompress(text);
		else {
			if (compressionMethod != CompressionMethod.ZLIB_DEFLATE)
				throw new IllegalArgumentException("Invalid compression method");
			decompText = text;
		}
		String textStr = new String(decompText, StandardCharsets.UTF_8);
		for (int i = 0; i < textStr.length(); i++) {
			if (textStr.charAt(i) == '\0')
				throw new IllegalArgumentException("NUL character in text");
		}
		
		Util.checkedLengthSum(keyword, languageTag, text,
			translatedKeyword.getBytes(StandardCharsets.UTF_8), 5);
	}
	
	
	public static Itxt read(int dataLen, DataInput in) throws IOException {
		byte[][] parts0 = Util.readAndSplitByNull(dataLen, in, 2);
		if (parts0[1].length < 2)
			throw new IllegalArgumentException("Missing compression flag or compression method");
		byte[] rest = Arrays.copyOfRange(parts0[1], 2, parts0[1].length);
		byte[][] parts1 = Util.readAndSplitByNull(rest.length, new DataInputStream(new ByteArrayInputStream(rest)), 3);
		
		return new Itxt(
			new String(parts0[0], StandardCharsets.ISO_8859_1),
			switch (parts0[1][0]) {
				case 0 -> false;
				case 1 -> true;
				default -> throw new IllegalArgumentException("Compression flag out of range");
			},
			Util.indexInto(CompressionMethod.values(), parts0[1][1]),
			new String(parts1[0], StandardCharsets.ISO_8859_1),
			new String(parts1[1], StandardCharsets.UTF_8),
			parts1[2]);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(keyword, languageTag, text,
			translatedKeyword.getBytes(StandardCharsets.UTF_8), 5);
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
