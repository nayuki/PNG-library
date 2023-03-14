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
	
	
	/*---- Constructor and factory ----*/
	
	public Scal {
		Objects.requireNonNull(unitSpecifier);
		if (Util.testAsciiFloat(pixelWidth) != 1 || Util.testAsciiFloat(pixelHeight) != 1)
			throw new IllegalArgumentException("Invalid number string");
		Util.checkedLengthSum(Byte.BYTES, pixelWidth, Byte.BYTES, pixelHeight);
	}
	
	
	static Scal read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		int unitSpecIndex = in.readUint8() - 1;
		if (!(0 <= unitSpecIndex && unitSpecIndex < UnitSpecifier.values().length))
			throw new IllegalArgumentException("Unrecognized value for enumeration");
		UnitSpecifier unitSpecifier = UnitSpecifier.values()[unitSpecIndex];
		String pixelWidth = in.readString(StandardCharsets.US_ASCII, true);
		String pixelHeight = in.readString(StandardCharsets.US_ASCII, false);
		return new Scal(unitSpecifier, pixelWidth, pixelHeight);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(Byte.BYTES, pixelWidth, Byte.BYTES, pixelHeight);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeUint8(unitSpecifier.ordinal() + 1);
		cout.writeString(pixelWidth, StandardCharsets.US_ASCII, true);
		cout.writeString(pixelHeight, StandardCharsets.US_ASCII, false);
		cout.finish();
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		METRE,  // Value 1
		RADIAN,  // Value 2
	}
	
}
