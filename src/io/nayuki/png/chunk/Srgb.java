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
 * A standard RGB color space (sRGB) chunk. This indicates the image
 * samples conform to the sRGB color space and should be displayed
 * using the specified rendering intent. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11sRGB
 */
public record Srgb(RenderingIntent renderingIntent) implements Chunk {
	
	static final String TYPE = "sRGB";
	
	
	/*---- Constructor and factory ----*/
	
	public Srgb {
		Objects.requireNonNull(renderingIntent);
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
	public static Srgb read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		RenderingIntent renderingIntent = Util.indexInto(RenderingIntent.values(), in.readUnsignedByte());
		return new Srgb(renderingIntent);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeByte(renderingIntent.ordinal());
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum RenderingIntent {
		PERCEPTUAL,
		RELITAVIE_COLORIMETRIC,
		SATURATION,
		ABSOLUTE_COLORIMETRIC,
	}
	
}
