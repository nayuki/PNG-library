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
 * An image gamma (gAMA) chunk. This specifies the relationship between the image
 * samples and the desired display output intensity. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11gAMA
 */
public record Gama(int gamma) implements SmallDataChunk {
	
	static final String TYPE = "gAMA";
	
	
	/*---- Constructors and factory ----*/
	
	public Gama {
		if (gamma <= 0)
			throw new IllegalArgumentException("Gamma value out of range");
	}
	
	
	public Gama(double gamma) {
		this(convert(gamma));
	}
	
	
	private static int convert(double gamma) {
		gamma *= 100_000;
		if (!(Double.isFinite(gamma) && 0 < gamma && gamma <= Integer.MAX_VALUE + 1.0))
			throw new IllegalArgumentException("Gamma value out of range");
		long result = Math.round(gamma);
		if (!(0 < result && result <= Integer.MAX_VALUE))
			throw new IllegalArgumentException("Gamma value out of range");
		return Math.toIntExact(result);
	}
	
	
	static Gama read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		return new Gama(in.readInt32());
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeInt32(gamma);
	}
	
}
