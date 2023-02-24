/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
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
 * sample depth, and color samples with alpha and frequency. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11sPLT
 */
public record Splt(
		String paletteName,
		int sampleDepth,
		byte[] data)
	implements Chunk {
	
	
	static final String TYPE = "sPLT";
	
	
	/*---- Constructor and factory ----*/
	
	public Splt {
		Util.checkKeyword(paletteName, true);
		
		int bytesPerEntry = switch (sampleDepth) {
			case 8 -> 6;
			case 16 -> 10;
			default -> throw new IllegalArgumentException("Invalid sample depth");
		};
		if (data.length % bytesPerEntry != 0)
			throw new IllegalArgumentException("Invalid data length");
		
		Util.checkedLengthSum(paletteName, 2 * Byte.BYTES, data);
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
	public static Splt read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		byte[][] parts = Util.readAndSplitByNul(dataLen, in, 2);
		if (parts[1].length < 1)
			throw new IllegalArgumentException("Missing sample depth");
		return new Splt(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			parts[1][0],
			Arrays.copyOfRange(parts[1], 1, parts[1].length));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(paletteName, 2 * Byte.BYTES, data);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(paletteName.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(sampleDepth);
		out.write(data);
	}
	
}
