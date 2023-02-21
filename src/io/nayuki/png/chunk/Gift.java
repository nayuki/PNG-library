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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A GIF plain text extension (gIFt) chunk. This type is deprecated but
 * provides backward compatibility for GIF images. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#DC.gIFt
 */
public record Gift(
		int textGridLeft,
		int textGridTop,
		int textGridWidth,
		int textGridHeight,
		int characterCellWidth,
		int characterCellHeight,
		int textForegroundColor,
		int textBackgroundColor,
		String text)
	implements Chunk {
	
	
	static final String TYPE = "gIFt";
	
	
	/*---- Constructor ----*/
	
	public Gift {
		if (textGridLeft == Integer.MIN_VALUE || textGridTop == Integer.MIN_VALUE ||
				textGridWidth == Integer.MIN_VALUE || textGridHeight == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Invalid int32 value");
		if (characterCellWidth >>> 8 != 0 || characterCellHeight >>> 8 != 0)
			throw new IllegalArgumentException("Character cell dimension out of range");
		if (textForegroundColor >>> 24 != 0 || textBackgroundColor >>> 24 != 0)
			throw new IllegalArgumentException("Invalid color");
		Util.checkedLengthSum(4 * Integer.BYTES, 2 * Byte.BYTES, 2 * 3 * Byte.BYTES, text);
	}
	
	
	/**
	 * Reads the specified number of bytes from the specified input stream,
	 * parses the fields, and returns a new chunk object of this type.
	 * @param dataLen the expected number of bytes of chunk data (non-negative)
	 * @param in the input stream to read from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if {@code dataLen} is negative
	 * or the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Gift read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		int textGridLeft   = in.readInt();
		int textGridTop    = in.readInt();
		int textGridWidth  = in.readInt();
		int textGridHeight = in.readInt();
		int charCellWidth  = in.readUnsignedByte();
		int charCellHeight = in.readUnsignedByte();
		
		int textForegroundColor = 0;
		for (int i = 0; i < 3; i++)
			textForegroundColor = (textForegroundColor << 8) | in.readUnsignedByte();
		int textBackgroundColor = 0;
		for (int i = 0; i < 3; i++)
			textBackgroundColor = (textBackgroundColor << 8) | in.readUnsignedByte();
		
		return new Gift(
			textGridLeft, textGridTop, textGridWidth, textGridHeight,
			charCellWidth, charCellHeight, textForegroundColor, textBackgroundColor,
			new String(Util.readBytes(in, dataLen - 24), StandardCharsets.US_ASCII));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(4 * Integer.BYTES, 2 * Byte.BYTES, 2 * 3 * Byte.BYTES, text);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(textGridLeft  );
		out.writeInt(textGridTop   );
		out.writeInt(textGridWidth );
		out.writeInt(textGridHeight);
		out.writeByte(characterCellWidth );
		out.writeByte(characterCellHeight);
		for (int i = 16; i >= 0; i -= 8)
			out.writeByte(textForegroundColor >>> i);
		for (int i = 16; i >= 0; i -= 8)
			out.writeByte(textBackgroundColor >>> i);
		out.write(text.getBytes(StandardCharsets.US_ASCII));
	}
	
}
