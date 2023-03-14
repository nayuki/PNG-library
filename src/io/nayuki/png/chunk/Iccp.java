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
	
	
	static Iccp read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String profileName = in.readString(StandardCharsets.ISO_8859_1, true);
		CompressionMethod compMethod = in.readEnum(CompressionMethod.values());
		byte[] compProfile = in.readRemainingBytes();
		return new Iccp(profileName, compMethod, compProfile);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(profileName, 2 * Byte.BYTES, compressedProfile);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeString(profileName, StandardCharsets.ISO_8859_1, true);
		cout.writeUint8(compressionMethod);
		cout.write(compressedProfile);
		cout.finish();
	}
	
}
