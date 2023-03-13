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
 * An image header (IHDR) chunk. This specifies the image dimensions,
 * color type, and various encoding methods. Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11IHDR
 */
public record Ihdr(
		int width,
		int height,
		int bitDepth,
		ColorType colorType,
		CompressionMethod compressionMethod,
		FilterMethod filterMethod,
		InterlaceMethod interlaceMethod)
	implements SmallDataChunk {
	
	
	static final String TYPE = "IHDR";
	
	
	/*---- Constructor and factory ----*/
	
	public Ihdr {
		if (width <= 0)
			throw new IllegalArgumentException("Non-positive width");
		if (height <= 0)
			throw new IllegalArgumentException("Non-positive height");
		Objects.requireNonNull(colorType);
		if (!(colorType.minimumBitDepth <= bitDepth && bitDepth <= colorType.maximumBitDepth
				&& Integer.bitCount(bitDepth) == 1))
			throw new IllegalArgumentException("Invalid bit depth");
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(filterMethod);
		Objects.requireNonNull(interlaceMethod);
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
	public static Ihdr read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		int width = in.readInt();
		int height = in.readInt();
		int bitDepth = in.readUnsignedByte();
		
		ColorType colorType = null;
		int colorTypeInt = in.readUnsignedByte();
		for (ColorType val : ColorType.values()) {
			if (val.value == colorTypeInt)
				colorType = val;
		}
		if (colorType == null)
			throw new IllegalArgumentException("Unrecognized value for enumeration");
		
		CompressionMethod compressionMethod = Util.indexInto(CompressionMethod.values(), in.readUnsignedByte());
		FilterMethod filterMethod = Util.indexInto(FilterMethod.values(), in.readUnsignedByte());
		InterlaceMethod interlaceMethod = Util.indexInto(InterlaceMethod.values(), in.readUnsignedByte());
		return new Ihdr(width, height, bitDepth, colorType, compressionMethod, filterMethod, interlaceMethod);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.writeInt32(width);
		out.writeInt32(height);
		out.writeUint8(bitDepth);
		out.writeUint8(colorType.value);
		out.writeUint8(compressionMethod);
		out.writeUint8(filterMethod);
		out.writeUint8(interlaceMethod);
	}
	
	
	
	/*---- Enumerations ----*/
	
	public enum ColorType {
		GRAYSCALE            (0, 1, 16),
		TRUE_COLOR           (2, 8, 16),
		INDEXED_COLOR        (3, 1,  8),
		GRAYSCALE_WITH_ALPHA (4, 8, 16),
		TRUE_COLOR_WITH_ALPHA(6, 8, 16);
		
		public final int value;
		public final int minimumBitDepth;
		public final int maximumBitDepth;
		
		private ColorType(int val, int minBitDepth, int maxBitDepth) {
			value = val;
			minimumBitDepth = minBitDepth;
			maximumBitDepth = maxBitDepth;
		}
	}
	
	
	public enum FilterMethod {
		ADAPTIVE,
	}
	
	
	public enum InterlaceMethod {
		NONE,
		ADAM7,
	}
	
}
