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
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A GIF application extension (gIFx) chunk. This provides backward compatibility for GIF
 * images. Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.gIFx
 */
public record Gifx(
		byte[] applicationIdentifier,
		byte[] authenticationCode,
		byte[] applicationData)
	implements Chunk {
	
	
	static final String TYPE = "gIFx";
	
	
	/*---- Constructor ----*/
	
	public Gifx {
		Objects.requireNonNull(applicationIdentifier);
		Objects.requireNonNull(authenticationCode);
		Objects.requireNonNull(applicationData);
		if (applicationIdentifier.length != 8 || authenticationCode.length != 3)
			throw new IllegalArgumentException("Data array length out of range");
		Util.checkedLengthSum(applicationIdentifier, authenticationCode, applicationData);
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
	public static Gifx read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		byte[] appIdentifier = Util.readBytes(in, 8);
		byte[] authCode = Util.readBytes(in, 3);
		byte[] appData = Util.readBytes(in, dataLen - appIdentifier.length - authCode.length);
		return new Gifx(appIdentifier, authCode, appData);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(applicationIdentifier, authenticationCode, applicationData);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(applicationIdentifier);
		out.write(authenticationCode);
		out.write(applicationData);
	}
	
}
