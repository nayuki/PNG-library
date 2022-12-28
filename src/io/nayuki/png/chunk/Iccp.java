package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Iccp(
		String profileName,
		CompressionMethod compressionMethod,
		byte[] compressedProfile)
	implements Chunk {
	
	
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
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "iCCP";
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
