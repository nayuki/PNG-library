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
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A textual data (tEXt) chunk. This contains a keyword and text string in the ISO 8859-1 character set.
 * Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11tEXt
 */
public record Text(String keyword, String text) implements Chunk {
	
	static final String TYPE = "tEXt";
	
	
	/*---- Constructor ----*/
	
	public Text {
		checkString(keyword);
		if (!(1 <= keyword.length() && keyword.length() <= 79) ||
				keyword.startsWith(" ") || keyword.endsWith(" ") || keyword.contains("  ") || keyword.contains("\n"))
			throw new IllegalArgumentException();
		checkString(text);
		if (1L + keyword.length() + text.length() > Integer.MAX_VALUE)
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
	
	
	public static Text read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		byte[][] parts = Util.splitByNull(data, 2);
		
		String keyword = new String(parts[0], StandardCharsets.ISO_8859_1);
		String text = new String(parts[1], StandardCharsets.ISO_8859_1);
		return new Text(keyword, text);
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
