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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * An embedded ICC profile (iCCP) chunk. This indicates the image samples
 * conform to the color space represented by the profile. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11iCCP
 */
public record Iccp(
		String profileName,
		CompressionMethod compressionMethod,
		byte[] compressedProfile)
	implements Chunk {
	
	
	static final String TYPE = "iCCP";
	
	
	/*---- Constructor and factory ----*/
	
	public Iccp {
		Util.checkKeyword(profileName, false);
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(compressedProfile);
		compressionMethod.decompress(compressedProfile);
		Util.checkedLengthSum(profileName, 2 * Byte.BYTES, compressedProfile);
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
	public static Iccp read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		byte[][] parts = Util.readAndSplitByNul(dataLen, in, 2);
		if (parts[1].length < 1)
			throw new IllegalArgumentException("Missing compression method");
		return new Iccp(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			Util.indexInto(CompressionMethod.values(), parts[1][0]),
			Arrays.copyOfRange(parts[1], 1, parts[1].length));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(profileName, 2 * Byte.BYTES, compressedProfile);
	}
	
	
	@Override public void writeData(ChunkWriter out) throws IOException {
		out.write(profileName.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(compressionMethod.ordinal());
		out.write(compressedProfile);
	}
	
}
