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
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * An image histogram (hIST) chunk. This gives the approximate
 * usage frequency of each color in the palette. Instances should
 * be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11hIST
 */
public record Hist(short[] frequencies) implements Chunk {
	
	static final String TYPE = "hIST";
	
	
	/*---- Constructor ----*/
	
	public Hist {
		Objects.requireNonNull(frequencies);
		if (!(1 <= frequencies.length && frequencies.length <= 256))
			throw new IllegalArgumentException("Data length out of range");
	}
	
	
	public static Hist read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		var freqs = new short[dataLen / Short.BYTES];
		for (int i = 0; i < freqs.length; i++)
			freqs[i] = in.readShort();
		return new Hist(freqs);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Math.multiplyExact(frequencies.length, Short.BYTES);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		for (short freq : frequencies)
			out.writeShort(freq);
	}
	
}
