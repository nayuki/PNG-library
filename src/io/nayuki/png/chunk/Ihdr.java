package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Ihdr(
		int width,
		int height,
		int bitDepth,
		ColorType colorType,
		CompressionMethod compressionMethod,
		FilterMethod filterMethod,
		InterlaceMethod interlaceMethod)
	implements Chunk {
	
	
	/*---- Constructor ----*/
	
	public Ihdr {
		if (width <= 0)
			throw new IllegalArgumentException();
		if (height <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(colorType);
		if (!(colorType.minimumBitDepth <= bitDepth && bitDepth <= colorType.maximumBitDepth && Integer.bitCount(bitDepth) == 1))
			throw new IllegalArgumentException();
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(filterMethod);
		Objects.requireNonNull(interlaceMethod);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "IHDR";
	}
	
	
	@Override public int getDataLength() {
		return 13;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(width);
		out.writeInt(height);
		out.writeByte(bitDepth);
		out.writeByte(colorType.value);
		out.writeByte(compressionMethod.ordinal());
		out.writeByte(filterMethod.ordinal());
		out.writeByte(interlaceMethod.ordinal());
	}
	
	
	
	/*---- Enumerations ----*/
	
	public enum ColorType {
		GRAYSCALE(0, 1, 16),
		TRUE_COLOR(2, 8, 16),
		INDEXED_COLOR(3, 1, 8),
		GRAYSCALE_WITH_ALPHA(4, 8, 16),
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
	
	
	public enum CompressionMethod {
		DEFLATE,
	}
	
	
	public enum FilterMethod {
		ADAPTIVE,
	}
	
	
	public enum InterlaceMethod {
		NONE,
		ADAM7,
	}
	
}
