/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.util.Objects;


/**
 * An image histogram (hIST) chunk. This gives the approximate
 * usage frequency of each color in the palette. Instances should
 * be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11hIST
 */
public record Hist(int[] frequencies) implements SmallDataChunk {
	
	static final String TYPE = "hIST";
	
	
	/*---- Constructor and factory ----*/
	
	public Hist {
		Objects.requireNonNull(frequencies);
		if (!(1 <= frequencies.length && frequencies.length <= 256))
			throw new IllegalArgumentException("Data length out of range");
		for (int val : frequencies) {
			if (val >>> 16 != 0)
				throw new IllegalArgumentException("Value out of range");
		}
	}
	
	
	static Hist read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		
		var freqs = new int[in.getRemainingCount() / Short.BYTES];
		for (int i = 0; i < freqs.length; i++)
			freqs[i] = in.readUint16();
		return new Hist(freqs);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		for (int freq : frequencies)
			out.writeUint16(freq);
	}
	
}
