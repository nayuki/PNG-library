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
	
	
	static Text read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String keyword = in.readString(true, StandardCharsets.ISO_8859_1);
		String text = in.readString(false, StandardCharsets.ISO_8859_1);
		return new Text(keyword, text);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(keyword, Byte.BYTES, text);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeString(keyword, StandardCharsets.ISO_8859_1, true);
		cout.writeString(text, StandardCharsets.ISO_8859_1, false);
		cout.finish();
	}
	
}
