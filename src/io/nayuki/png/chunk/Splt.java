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
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A suggested palette (sPLT) chunk. This contains a palette name,
 * sample depth, and color samples with alpha and frequency.
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11sPLT
 */
public record Splt(String paletteName, int sampleDepth, byte[] data) implements Chunk {
	
	static final String TYPE = "sPLT";
	
	
	/*---- Constructor ----*/
	
	public Splt {
		checkString(paletteName);
		if (!(1 <= paletteName.length() && paletteName.length() <= 79) ||
				paletteName.startsWith(" ") || paletteName.endsWith(" ") || paletteName.contains("  ") || paletteName.contains("\n"))
			throw new IllegalArgumentException();
		
		int bytesPerEntry = switch (sampleDepth) {
			case 8 -> 6;
			case 16 -> 10;
			default -> throw new IllegalArgumentException();
		};
		if (data.length % bytesPerEntry != 0)
			throw new IllegalArgumentException();
		
		if (2L + paletteName.length() + data.length > Integer.MAX_VALUE)
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
	
	
	public static Splt read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 2);
		
		String paletteName = new String(parts[0], StandardCharsets.ISO_8859_1);
		if (parts[1].length < 1)
			throw new IllegalArgumentException();
		int sampleDepth = parts[1][0];
		byte[] data = Arrays.copyOfRange(parts[1], 1, parts[1].length);
		
		return new Splt(paletteName, sampleDepth, data);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return paletteName.length() + 2 + data.length;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(paletteName.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(sampleDepth);
		out.write(data);
	}
	
}
