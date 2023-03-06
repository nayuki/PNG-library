/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;


/**
 * A GIF graphic control extension (gIFg) chunk. This provides
 * backward compatibility for GIF images. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.gIFg
 */
public record Gifg(
		int disposalMethod,
		boolean userInputFlag,
		int delayTime)
	implements SmallDataChunk {
	
	
	static final String TYPE = "gIFg";
	
	
	/*---- Constructor and factory ----*/
	
	public Gifg {
		if (disposalMethod >>> 3 != 0)
			throw new IllegalArgumentException("Disposal method out of range");
		if (delayTime >>> 16 != 0)
			throw new IllegalArgumentException("Delay time out of range");
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
	public static Gifg read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		int disposalMethod = in.readUnsignedByte();
		int userInputFlag = in.readUnsignedByte();
		int delayTime = in.readUnsignedShort();
		if (userInputFlag >>> 1 != 0)
			throw new IllegalArgumentException("User input flag out of range");
		return new Gifg(disposalMethod, userInputFlag != 0, delayTime);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeByte(disposalMethod);
		out.writeByte(userInputFlag ? 1 : 0);
		out.writeShort(delayTime);
	}
	
}
