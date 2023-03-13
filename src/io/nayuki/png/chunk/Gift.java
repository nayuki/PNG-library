/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.io.OutputStream;
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
	
	
	/*---- Constructor and factory ----*/
	
	public Gift {
		if (textGridLeft == Integer.MIN_VALUE || textGridTop == Integer.MIN_VALUE ||
				textGridWidth == Integer.MIN_VALUE || textGridHeight == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Invalid int32 value");
		if (characterCellWidth >>> 8 != 0 || characterCellHeight >>> 8 != 0)
			throw new IllegalArgumentException("Character cell dimension out of range");
		if (textForegroundColor >>> 24 != 0 || textBackgroundColor >>> 24 != 0)
			throw new IllegalArgumentException("Invalid color");
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) >= 0x80)
				throw new IllegalArgumentException("Invalid byte in ASCII text");
		}
		Util.checkedLengthSum(4 * Integer.BYTES, 2 * Byte.BYTES, 2 * 3 * Byte.BYTES, text);
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
	public static Gift read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		
		int textGridLeft   = in.readInt32();
		int textGridTop    = in.readInt32();
		int textGridWidth  = in.readInt32();
		int textGridHeight = in.readInt32();
		int charCellWidth  = in.readUint8();
		int charCellHeight = in.readUint8();
		
		int textForegroundColor = 0;
		for (int i = 0; i < 3; i++)
			textForegroundColor = (textForegroundColor << 8) | in.readUint8();
		int textBackgroundColor = 0;
		for (int i = 0; i < 3; i++)
			textBackgroundColor = (textBackgroundColor << 8) | in.readUint8();
		
		return new Gift(
			textGridLeft, textGridTop, textGridWidth, textGridHeight,
			charCellWidth, charCellHeight, textForegroundColor, textBackgroundColor,
			new String(in.readRemainingBytes(), StandardCharsets.US_ASCII));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(4 * Integer.BYTES, 2 * Byte.BYTES, 2 * 3 * Byte.BYTES, text);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeInt32(textGridLeft  );
		cout.writeInt32(textGridTop   );
		cout.writeInt32(textGridWidth );
		cout.writeInt32(textGridHeight);
		cout.writeUint8(characterCellWidth );
		cout.writeUint8(characterCellHeight);
		for (int i = 16; i >= 0; i -= 8)
			cout.writeUint8((textForegroundColor >>> i) & 0xFF);
		for (int i = 16; i >= 0; i -= 8)
			cout.writeUint8((textBackgroundColor >>> i) & 0xFF);
		cout.writeAscii(text);
		cout.finish();
	}
	
}
