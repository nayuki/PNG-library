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
 * A physical scale of image subject (sCAL) chunk. This specifies the physical size
 * of the image from scanning or for printing, or the physical size of the image's
 * subject such as in maps and astronomical surveys. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.sCAL
 */
public record Scal(
		UnitSpecifier unitSpecifier,
		String pixelWidth,
		String pixelHeight)
	implements Chunk {
	
	
	static final String TYPE = "sCAL";
	
	
	/*---- Constructor ----*/
	
	public Scal {
		Objects.requireNonNull(unitSpecifier);
		if (Util.testAsciiFloat(pixelWidth) != 1 || Util.testAsciiFloat(pixelHeight) != 1)
			throw new IllegalArgumentException("Invalid number string");
		Util.checkedLengthSum(pixelWidth, pixelHeight, 2);
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
	public static Scal read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		UnitSpecifier unitSpecifier = Util.indexInto(UnitSpecifier.values(), in.readUnsignedByte() - 1);
		byte[][] parts = Util.readAndSplitByNul(dataLen - 1, in, 2);
		return new Scal(
			unitSpecifier,
			new String(parts[0], StandardCharsets.US_ASCII),
			new String(parts[1], StandardCharsets.US_ASCII));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(pixelWidth, pixelHeight, 2);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeByte(unitSpecifier.ordinal() + 1);
		out.write(pixelWidth.getBytes(StandardCharsets.US_ASCII));
		out.writeByte(0);
		out.write(pixelHeight.getBytes(StandardCharsets.US_ASCII));
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		METRE,  // Value 1
		RADIAN,  // Value 2
	}
	
}
