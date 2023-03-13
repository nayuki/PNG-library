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
	 * Reads from the specified chunk reader, parses the
	 * fields, and returns a new chunk object of this type.
	 * @param in the chunk reader to read the chunk's data from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Text read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String keyword = in.readString(ChunkReader.Until.NUL, StandardCharsets.ISO_8859_1);
		String text = in.readString(ChunkReader.Until.END, StandardCharsets.ISO_8859_1);
		return new Text(keyword, text);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(keyword, Byte.BYTES, text);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeIso8859_1(keyword);
		cout.writeUint8(0);
		cout.writeIso8859_1(text);
		cout.finish();
	}
	
}
