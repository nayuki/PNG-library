package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Hist(short[] frequencies) implements Chunk {
	
	/*---- Constructor ----*/
	
	public Hist {
		Objects.requireNonNull(frequencies);
		if (!(1 <= frequencies.length && frequencies.length <= 256))
			throw new IllegalArgumentException();
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "hIST";
	}
	
	
	@Override public int getDataLength() {
		return Math.multiplyExact(frequencies.length, Short.BYTES);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		for (short freq : frequencies)
			out.writeShort(freq);
	}
	
}
