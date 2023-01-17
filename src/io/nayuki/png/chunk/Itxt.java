/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
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
		String languageTag,
		String translatedKeyword,
		Optional<CompressionMethod> compressionMethod,
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
		
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(text);
		byte[] decompText = compressionMethod.map(cm -> cm.decompress(text)).orElse(text);
		var textStr = new String(decompText, StandardCharsets.UTF_8);
		for (int i = 0; i < textStr.length(); i++) {
			if (textStr.charAt(i) == '\0')
				throw new IllegalArgumentException("NUL character in text");
		}
		
		Util.checkedLengthSum(keyword, languageTag, text,
			translatedKeyword.getBytes(StandardCharsets.UTF_8), 5);
	}
	
	
	/**
	 * Reads the specified number of bytes from the specified input stream,
	 * parses the fields, and returns a new chunk object of this type.
	 * @param dataLen the expected number of bytes of chunk data (non-negative)
	 * @param in the input stream to read from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if {@code dataLen} is negative
	 * or the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Itxt read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		byte[][] parts0 = Util.readAndSplitByNul(dataLen, in, 2);
		if (parts0[1].length < 2)
			throw new IllegalArgumentException("Missing compression flag or compression method");
		byte[] rest = Arrays.copyOfRange(parts0[1], 2, parts0[1].length);
		byte[][] parts1 = Util.splitByNul(rest, 3);
		
		int compFlag = parts0[1][0];
		int compMethod = parts0[1][1];
		if (compFlag >>> 1 != 0)
			throw new IllegalArgumentException("Compression flag out of range");
		if (compFlag == 0 && compMethod != 0)
			throw new IllegalArgumentException("Invalid compression method");
		
		return new Itxt(
			new String(parts0[0], StandardCharsets.ISO_8859_1),
			new String(parts1[0], StandardCharsets.ISO_8859_1),
			new String(parts1[1], StandardCharsets.UTF_8),
			compFlag == 0 ? Optional.empty() : Optional.of(Util.indexInto(CompressionMethod.values(), compMethod)),
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
		out.writeByte(compressionMethod.isPresent() ? 1 : 0);
		out.writeByte(compressionMethod.map(cm -> cm.ordinal()).orElse(0));
		out.write(languageTag.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.write(translatedKeyword.getBytes(StandardCharsets.UTF_8));
		out.writeByte(0);
		out.write(text);
	}
	
}
