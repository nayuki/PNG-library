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
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A significant bits (sBIT) chunk. This defines the original number of significant bits per
 * channel. Instances should be treated as immutable, but arrays are not copied defensively.
 * The interpretation of this chunk depends on the color type in the IHDR chunk.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11sBIT
 */
public record Sbit(byte[] significantBits) implements Chunk {
	
	static final String TYPE = "sBIT";
	
	
	/*---- Constructor ----*/
	
	public Sbit {
		Objects.requireNonNull(significantBits);
		if (!(1 <= significantBits.length && significantBits.length <= 4))
			throw new IllegalArgumentException("Array length out of range");
		for (int bits : significantBits) {
			if (!(1 <= bits && bits <= 16))
				throw new IllegalArgumentException("Number of significant bits out of range");
		}
	}
	
	
	public static Sbit read(int dataLen, DataInput in) throws IOException {
		return new Sbit(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return significantBits.length;
	}
	
	
	@Override public byte[] getData() {
		return significantBits;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(significantBits);
	}
	
}
