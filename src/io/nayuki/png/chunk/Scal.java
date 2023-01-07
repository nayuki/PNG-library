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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	
	private static final Pattern ASCII_FLOAT =
		Pattern.compile("([+-]?)(\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][+-]?\\d+)?");
	
	private static final Pattern NONZERO = Pattern.compile("[1-9]");
	
	
	/*---- Constructor ----*/
	
	public Scal {
		Objects.requireNonNull(unitSpecifier);
		Objects.requireNonNull(pixelWidth);
		Objects.requireNonNull(pixelHeight);
		
		Matcher m = ASCII_FLOAT.matcher(pixelWidth);
		if (!m.matches() || m.group(1).equals("-") || !NONZERO.matcher(m.group(2)).find())
			throw new IllegalArgumentException("Invalid number string");
		m = ASCII_FLOAT.matcher(pixelHeight);
		if (!m.matches() || m.group(1).equals("-") || !NONZERO.matcher(m.group(2)).find())
			throw new IllegalArgumentException("Invalid number string");
		
		Util.checkedLengthSum(pixelWidth, pixelHeight, 2);
	}
	
	
	public static Scal read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		UnitSpecifier unitSpecifier = Util.indexInto(UnitSpecifier.values(), in.readUnsignedByte() - 1);
		byte[][] parts = Util.readAndSplitByNull(dataLen - 1, in, 2);
		return new Scal(
			unitSpecifier,
			new String(parts[0], StandardCharsets.UTF_8),
			new String(parts[1], StandardCharsets.UTF_8));
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
		out.write(pixelWidth.getBytes(StandardCharsets.UTF_8));
		out.writeByte(0);
		out.write(pixelHeight.getBytes(StandardCharsets.UTF_8));
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		METRE,  // Value 1
		RADIAN,  // Value 2
	}
	
}
