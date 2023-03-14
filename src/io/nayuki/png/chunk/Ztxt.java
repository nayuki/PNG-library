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
	
	
	/*---- Constructor and factory ----*/
	
	public Ztxt {
		Util.checkKeyword(keyword, true);
		
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(compressedText);
		byte[] decompText = compressionMethod.decompress(compressedText);
		
		var text = new String(decompText, StandardCharsets.ISO_8859_1);
		Util.checkIso8859_1(text, true);
		Util.checkedLengthSum(keyword, 2 * Byte.BYTES, compressedText);
	}
	
	
	static Ztxt read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String keyword = in.readString(ChunkReader.Until.NUL, StandardCharsets.ISO_8859_1);
		CompressionMethod compMethod = in.readEnum(CompressionMethod.values());
		byte[] compText = in.readRemainingBytes();
		return new Ztxt(keyword, compMethod, compText);
	}
	
	
	/*---- Methods ----*/
	
	/**
	 * Decompresses the text field, interprets the bytes as ISO 8859-1, and returns the data as a string object.
	 * @return a string representing the text conveyed by the text field (not {@code null})
	 */
	public String getText() {
		byte[] decompText = compressionMethod.decompress(compressedText);
		return new String(decompText, StandardCharsets.ISO_8859_1);
	}
	
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(keyword, 2 * Byte.BYTES, compressedText);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeString(keyword, StandardCharsets.ISO_8859_1);
		cout.writeUint8(0);
		cout.writeUint8(compressionMethod);
		cout.write(compressedText);
		cout.finish();
	}
	
}
