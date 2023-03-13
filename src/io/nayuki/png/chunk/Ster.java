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
 * An indicator of stereo image (sTER) chunk. This indicates the image
 * contains a stereo pair of subimages. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.sTER
 */
public record Ster(Mode mode) implements SmallDataChunk {
	
	static final String TYPE = "sTER";
	
	
	/*---- Constructor and factory ----*/
	
	public Ster {
		Objects.requireNonNull(mode);
	}
	
	
	/**
	 * Reads from the specified chunk reader, parses the
	 * fields, and returns a new chunk object of this type.
	 * @param in the chunk reader to read the chunk's data from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Ster read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		Mode mode = in.readEnum(Mode.values());
		return new Ster(mode);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeUint8(mode);
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum Mode {
		CROSS_FUSE_LAYOUT,
		DIVERGING_FUSE_LAYOUT,
	}
	
}
