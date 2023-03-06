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
 * A physical pixel dimensions (pHYs) chunk. This specifies the intended pixel
 * size or aspect ratio for displaying the image. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11pHYs
 */
public record Phys(
		int pixelsPerUnitX,
		int pixelsPerUnitY,
		UnitSpecifier unitSpecifier)
	implements SmallDataChunk {
	
	
	static final String TYPE = "pHYs";
	
	
	/*---- Constructor and factory ----*/
	
	public Phys {
		if (pixelsPerUnitX <= 0 || pixelsPerUnitY <= 0)
			throw new IllegalArgumentException("Non-positive physical density");
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
	public static Phys read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		int pixelsPerUnitX = in.readInt();
		int pixelsPerUnitY = in.readInt();
		UnitSpecifier unitSpecifier = Util.indexInto(UnitSpecifier.values(), in.readUnsignedByte());
		return new Phys(pixelsPerUnitX, pixelsPerUnitY, unitSpecifier);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeInt32(pixelsPerUnitX);
		out.writeInt32(pixelsPerUnitY);
		out.writeUint8(unitSpecifier.ordinal());
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		UNKNOWN,
		METRE,
	}
	
}
