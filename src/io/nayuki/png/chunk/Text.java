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
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A textual data (tEXt) chunk. This contains a keyword and text
 * string in the ISO 8859-1 character set. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11tEXt
 */
public record Text(String keyword, String text) implements Chunk {
	
	static final String TYPE = "tEXt";
	
	
	/*---- Constructor and factory ----*/
	
	public Text {
		Util.checkKeyword(keyword, true);
		Util.checkIso8859_1(text, true);
		Util.checkedLengthSum(keyword, Byte.BYTES, text);
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
	public static Text read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		byte[][] parts = Util.readAndSplitByNul(dataLen, in, 2);
		return new Text(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			new String(parts[1], StandardCharsets.ISO_8859_1));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out0) throws IOException {
		int dataLen = Util.checkedLengthSum(keyword, Byte.BYTES, text);
		var out = new ChunkWriter(dataLen, getType(), out0);
		out.writeIso8859_1(keyword);
		out.writeUint8(0);
		out.writeIso8859_1(text);
		out.finish();
	}
	
}
