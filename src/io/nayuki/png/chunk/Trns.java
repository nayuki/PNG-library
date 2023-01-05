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
 * A transparency (tRNS) chunk. This specifies a single transparent color
 * or associates alpha values with palette entries. Instances should be
 * treated as immutable, but arrays are not copied defensively. The
 * interpretation of this chunk depends on the color type in the IHDR chunk.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11tRNS
 */
public record Trns(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "tRNS";
	
	
	/*---- Constructors ----*/
	
	public Trns {
		Objects.requireNonNull(data);
		if (data.length > 256)
			throw new IllegalArgumentException("Data length out of range");
	}
	
	
	public Trns(short[] channelValues) {
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
	
	
	public static Trns read(int dataLen, DataInput in) throws IOException {
		return new Trns(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
