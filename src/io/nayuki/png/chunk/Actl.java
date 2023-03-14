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
 * An animation control (acTL) chunk. This specifies the
 * number of frames and loops. Instances are immutable.
 * @see https://wiki.mozilla.org/APNG_Specification#.60acTL.60:_The_Animation_Control_Chunk
 */
public record Actl(
		int numFrames,
		int numPlays)
	implements SmallDataChunk {
	
	
	static final String TYPE = "acTL";
	
	
	/*---- Constructor and factory ----*/
	
	public Actl {
		if (numFrames <= 0)
			throw new IllegalArgumentException("Invalid number of frames");
		if (numPlays < 0)
			throw new IllegalArgumentException("Invalid number of plays");
	}
	
	
	static Actl read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		int numFrames = in.readInt32();
		int numPlays  = in.readInt32();
		return new Actl(numFrames, numPlays);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeInt32(numFrames);
		out.writeInt32(numPlays );
	}
	
}
