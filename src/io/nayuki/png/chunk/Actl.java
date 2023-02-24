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
 * An animation control (acTL) chunk. This specifies the
 * number of frames and loops. Instances are immutable.
 * @see https://wiki.mozilla.org/APNG_Specification#.60acTL.60:_The_Animation_Control_Chunk
 */
public record Actl(
		int numFrames,
		int numPlays)
	implements Chunk {
	
	
	static final String TYPE = "acTL";
	
	
	/*---- Constructor and factory ----*/
	
	public Actl {
		if (numFrames <= 0)
			throw new IllegalArgumentException("Invalid number of frames");
		if (numPlays < 0)
			throw new IllegalArgumentException("Invalid number of plays");
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
	public static Actl read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		int numFrames = in.readInt();
		int numPlays  = in.readInt();
		return new Actl(numFrames, numPlays);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 2 * Integer.BYTES;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(numFrames);
		out.writeInt(numPlays );
	}
	
}
