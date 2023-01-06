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
 * An indicator of stereo image (sTER) chunk. This indicates the image
 * contains a stereo pair of subimages. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.sTER
 */
public record Ster(Mode mode) implements Chunk {
	
	static final String TYPE = "sTER";
	
	
	/*---- Constructor ----*/
	
	public Ster {
		Objects.requireNonNull(mode);
	}
	
	
	public static Ster read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		Mode mode = Util.indexInto(Mode.values(), in.readUnsignedByte());
		return new Ster(mode);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 1;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeByte(mode.ordinal());
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum Mode {
		CROSS_FUSE_LAYOUT,
		DIVERGING_FUSE_LAYOUT,
	}
	
}
