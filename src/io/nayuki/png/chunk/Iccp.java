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
		checkString(profileName);
		if (!(1 <= profileName.length() && profileName.length() <= 79))
			throw new IllegalArgumentException();
		Objects.requireNonNull(compressionMethod);
		Objects.requireNonNull(compressedProfile);
		if (2L + profileName.length() + compressedProfile.length > Integer.MAX_VALUE)
			throw new IllegalArgumentException();
	}
	
	
	private static void checkString(String s) {
		Objects.requireNonNull(s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!(32 <= c && c <= 126 || 161 <= c && c <= 255))
				throw new IllegalArgumentException();
		}
		if (s.startsWith(" ") || s.endsWith(" ") || s.contains("  "))
			throw new IllegalArgumentException();
	}
	
	
	public static Iccp read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		int index = 0;
		while (index < data.length && data[index] != 0)
			index++;
		if (data.length - index < 2)
			throw new IllegalArgumentException();
		return new Iccp(
			new String(Arrays.copyOf(data, index), StandardCharsets.ISO_8859_1),
			Util.indexInto(CompressionMethod.values(), data[index + 1]),
			Arrays.copyOfRange(data, index + 2, data.length));
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
	
	
	
	/*---- Enumeration ----*/
	
	public enum CompressionMethod {
		DEFLATE,
	}
	
}
