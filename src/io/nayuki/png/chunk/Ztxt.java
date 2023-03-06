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
import java.io.OutputStream;
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
	public static Ztxt read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		byte[][] parts = Util.readAndSplitByNul(dataLen, in, 2);
		if (parts[1].length < 1)
			throw new IllegalArgumentException("Missing compression method");
		return new Ztxt(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			Util.indexInto(CompressionMethod.values(), parts[1][0]),
			Arrays.copyOfRange(parts[1], 1, parts[1].length));
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
	
	
	@Override public void writeChunk(OutputStream out0) throws IOException {
		int dataLen = Util.checkedLengthSum(keyword, 2 * Byte.BYTES, compressedText);
		var out = new ChunkWriter(dataLen, getType(), out0);
		out.write(keyword.getBytes(StandardCharsets.ISO_8859_1));
		out.writeUint8(0);
		out.writeUint8(compressionMethod.ordinal());
		out.write(compressedText);
		out.finish();
	}
	
}
