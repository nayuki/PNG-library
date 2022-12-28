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


public record Hist(short[] frequencies) implements Chunk {
	
	static final String TYPE = "hIST";
	
	
	/*---- Constructor ----*/
	
	public Hist {
		Objects.requireNonNull(frequencies);
		if (!(1 <= frequencies.length && frequencies.length <= 256))
			throw new IllegalArgumentException();
	}
	
	
	public static Hist read(int dataLen, DataInput in) throws IOException {
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
