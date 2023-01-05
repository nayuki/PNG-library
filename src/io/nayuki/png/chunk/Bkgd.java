/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;


/**
 * A background color (bKGD) chunk. This specifies a default
 * background color to present the image against. Instances should
 * be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11bKGD
 */
public record Bkgd(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "bKGD";
	
	
	/*---- Constructors ----*/
	
	public Bkgd {
		Objects.requireNonNull(data);
		if (!(1 <= data.length && data.length <= 6))
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
	
	
	public static Bkgd read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		return new Bkgd(data);
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
