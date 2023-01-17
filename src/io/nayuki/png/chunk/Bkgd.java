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
import java.nio.ByteBuffer;
import java.util.Objects;


/**
 * A background color (bKGD) chunk. This specifies a default background color to present
 * the image against. Instances should be treated as immutable, but arrays are not copied
 * defensively. The interpretation of this chunk depends on the color type in the IHDR chunk.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11bKGD
 */
public record Bkgd(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "bKGD";
	
	
	/*---- Constructors ----*/
	
	public Bkgd {
		Objects.requireNonNull(data);
		if (data.length != 1 && data.length != 2 && data.length != 6)
			throw new IllegalArgumentException("Invalid data length");
	}
	
	
	public Bkgd(short[] channelValues) {
		this(convert(channelValues));
	}
	
	
	private static byte[] convert(short[] channelValues) {
		Objects.requireNonNull(channelValues);
		if (channelValues.length != 1 && channelValues.length != 3)
			throw new IllegalArgumentException("Invalid array length");
		ByteBuffer bb = ByteBuffer.allocate(Math.multiplyExact(channelValues.length, Short.BYTES));
		bb.asShortBuffer().put(channelValues);
		if (!bb.hasArray())
			throw new AssertionError("Non-direct ByteBuffer must have array");
		return bb.array();
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
	public static Bkgd read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		return new Bkgd(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
