/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * An embedded ICC profile (iCCP) chunk. This indicates the image
 * samples conform to the color space represented by the profile.
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11iCCP
 */
public record Iccp(
		String profileName,
		CompressionMethod compressionMethod,
		byte[] compressedProfile)
	implements Chunk {
	
	
	static final String TYPE = "iCCP";
	
	
	/*---- Constructor ----*/
	
	public Iccp {
		Util.checkKeyword(profileName, false);
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(compressedProfile);
		if (2L + profileName.length() + compressedProfile.length > Integer.MAX_VALUE)
			throw new IllegalArgumentException();
	}
	
	
	public static Iccp read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 2);
		if (parts[1].length < 1)
			throw new IllegalArgumentException();
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
		return profileName.length() + 1 + 1 + compressedProfile.length;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(profileName.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(compressionMethod.ordinal());
		out.write(compressedProfile);
	}
	
}
