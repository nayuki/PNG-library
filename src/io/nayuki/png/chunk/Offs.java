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
import io.nayuki.png.Chunk;


/**
 * An image offset (oFFs) chunk. This specifies the position where
 * the image should be printed on a page alone, or defines the image's
 * location with respect to a larger screen. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.oFFs
 */
public record Offs(
		int xPosition,
		int yPosition,
		UnitSpecifier unitSpecifier)
	implements Chunk {
	
	
	static final String TYPE = "oFFs";
	
	
	/*---- Constructor and factory ----*/
	
	public Offs {
		if (xPosition == Integer.MIN_VALUE || yPosition == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Invalid int32 value");
		Objects.requireNonNull(unitSpecifier);
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
	public static Offs read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		int xPosition = in.readInt();
		int yPosition = in.readInt();
		UnitSpecifier unitSpecifier = Util.indexInto(UnitSpecifier.values(), in.readUnsignedByte());
		return new Offs(xPosition, yPosition, unitSpecifier);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeInt(xPosition);
		out.writeInt(yPosition);
		out.writeByte(unitSpecifier.ordinal());
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		PIXEL,
		MICROMETRE,
	}
	
}
