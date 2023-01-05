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
	
	
	/*---- Constructor ----*/
	
	public Ztxt {
		Util.checkKeyword(keyword, true);
		
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(compressedText);
		byte[] decompText = compressionMethod.decompress(compressedText);
		
		String text = new String(decompText, StandardCharsets.ISO_8859_1);
		Util.checkIso8859_1(text, true);
		if (2L + keyword.length() + compressedText.length > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Data too long");
	}
	
	
	public static Ztxt read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 2);
		if (parts[1].length < 1)
			throw new IllegalArgumentException("Missing compression method");
		return new Ztxt(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			Util.indexInto(CompressionMethod.values(), parts[1][0]),
			Arrays.copyOfRange(parts[1], 1, parts[1].length));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return keyword.length() + 2 + compressedText.length;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(keyword.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(compressionMethod.ordinal());
		out.write(compressedText);
	}
	
}
