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
 * An image gamma (gAMA) chunk. This specifies the relationship between the image
 * samples and the desired display output intensity. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11gAMA
 */
public record Gama(int gamma) implements Chunk {
	
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
	
	
	/**
	 * Reads a constant number of bytes from the specified input stream,
	 * parses the fields, and returns a new chunk object of this type.
	 * @param in the input stream to read from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Gama read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		return new Gama(in.readInt());
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(gamma);
	}
	
}
