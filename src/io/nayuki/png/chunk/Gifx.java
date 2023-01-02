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
			throw new IllegalArgumentException();
		if ((long)applicationIdentifier.length + authenticationCode.length + applicationData.length > Integer.MAX_VALUE)
			throw new IllegalArgumentException();
	}
	
	
	public static Gifx read(int dataLen, DataInput in) throws IOException {
		var appIdentifier = new byte[8];
		var authCode = new byte[3];
		var appData = new byte[dataLen - appIdentifier.length - authCode.length];
		in.readFully(appIdentifier);
		in.readFully(authCode);
		in.readFully(appData);
		return new Gifx(appIdentifier, authCode, appData);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 9;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(applicationIdentifier);
		out.write(authenticationCode);
		out.write(applicationData);
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		PIXEL,
		MICROMETRE,
	}
	
}