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
import java.util.Objects;


/**
 * A significant bits (sBIT) chunk. This defines the original number of significant bits per
 * channel. Instances should be treated as immutable, but arrays are not copied defensively.
 * The interpretation of this chunk depends on the color type in the IHDR chunk.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11sBIT
 */
public record Sbit(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "sBIT";
	
	
	/*---- Constructor and factory ----*/
	
	public Sbit {
		Objects.requireNonNull(data);
		if (!(1 <= data.length && data.length <= 4))
			throw new IllegalArgumentException("Array length out of range");
		for (int bits : data) {
			if (!(1 <= bits && bits <= 16))
				throw new IllegalArgumentException("Number of significant bits out of range");
		}
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
	public static Sbit read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		return new Sbit(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
