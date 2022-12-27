package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Text(String keyword, String text) implements Chunk {
	
	/*---- Constructor ----*/
	
	public Text {
		checkString(keyword);
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
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "tEXt";
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
