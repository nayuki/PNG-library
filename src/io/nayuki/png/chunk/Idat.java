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
 * An image data (IDAT) chunk. This contains pixel data that is filtered and compressed.
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * The interpretation of this chunk depends extensively on the IHDR chunk.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11IDAT
 */
public record Idat(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "IDAT";
	
	
	/*---- Constructor and factory ----*/
	
	public Idat {
		Objects.requireNonNull(data);
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
	public static Idat read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		return new Idat(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
