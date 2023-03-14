/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;


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
	
	
	/*---- Constructor and factory ----*/
	
	public Itxt {
		Util.checkKeyword(keyword, true);
		
		checkLanguageTag(languageTag);
		
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
		
		Util.checkedLengthSum(keyword, 3 * Byte.BYTES, languageTag, Byte.BYTES,
			text, Byte.BYTES, translatedKeyword.getBytes(StandardCharsets.UTF_8));
	}
	
	
	static void checkLanguageTag(String s) {
		Objects.requireNonNull(s);
		if (!s.matches("(?:[A-Za-z0-9]{1,8}(?:-[A-Za-z0-9]{1,8})*+)?"))
			throw new IllegalArgumentException("Invalid language tag syntax");
	}
	
	
	static Itxt read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String keyword = in.readString(true, StandardCharsets.ISO_8859_1);
		int compFlag = in.readUint8();
		int compMethod = in.readUint8();
		if (compFlag == 0) {
			if (compMethod != 0)
				throw new IllegalArgumentException("Invalid compression method");
		} else if (compFlag == 1) {
			if (compMethod > CompressionMethod.values().length)
				throw new IllegalArgumentException("Unrecognized value for enumeration");
		} else
			throw new IllegalArgumentException("Compression flag out of range");
		String language = in.readString(true, StandardCharsets.ISO_8859_1);
		String transKeyword = in.readString(true, StandardCharsets.UTF_8);
		byte[] text = in.readRemainingBytes();
		return new Itxt(keyword, language, transKeyword,
			compFlag == 0 ? Optional.empty() : Optional.of(CompressionMethod.values()[compMethod]),
			text);
	}
	
	
	/*---- Methods ----*/
	
	/**
	 * Decompresses the text field, interprets the bytes as UTF-8, and returns the data as a string object.
	 * @return a string representing the text conveyed by the text field (not {@code null})
	 */
	public String getText() {
		byte[] decompText = compressionMethod.map(cm -> cm.decompress(text)).orElse(text);
		return new String(decompText, StandardCharsets.UTF_8);
	}
	
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(keyword, 3 * Byte.BYTES, languageTag, Byte.BYTES,
			text, Byte.BYTES, translatedKeyword.getBytes(StandardCharsets.UTF_8));
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeString(keyword, StandardCharsets.ISO_8859_1, true);
		cout.writeUint8(compressionMethod.isPresent() ? 1 : 0);
		cout.writeUint8(compressionMethod.map(cm -> cm.ordinal()).orElse(0));
		cout.writeString(languageTag, StandardCharsets.ISO_8859_1, true);
		cout.writeString(translatedKeyword, StandardCharsets.UTF_8, true);
		cout.write(text);
		cout.finish();
	}
	
}
