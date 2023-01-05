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
import io.nayuki.png.Chunk;


/**
 * A textual data (tEXt) chunk. This contains a keyword and text
 * string in the ISO 8859-1 character set. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11tEXt
 */
public record Text(String keyword, String text) implements Chunk {
	
	static final String TYPE = "tEXt";
	
	
	/*---- Constructor ----*/
	
	public Text {
		Util.checkKeyword(keyword, true);
		Util.checkIso8859_1(text, true);
		if (1L + keyword.length() + text.length() > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Data too long");
	}
	
	
	public static Text read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 2);
		return new Text(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			new String(parts[1], StandardCharsets.ISO_8859_1));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return keyword.length() + 1 + text.length();
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(keyword.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.write(text.getBytes(StandardCharsets.ISO_8859_1));
	}
	
}
