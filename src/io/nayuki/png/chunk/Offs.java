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
	
	
	/*---- Constructor ----*/
	
	public Offs {
		if (xPosition == Integer.MIN_VALUE || yPosition == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Invalid int32 value");
		Objects.requireNonNull(unitSpecifier);
	}
	
	
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
	
	
	@Override public int getDataLength() {
		return 9;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
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
