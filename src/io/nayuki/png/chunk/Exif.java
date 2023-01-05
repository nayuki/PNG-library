/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;


/**
 * An exchangeable image file profile (eXIf) chunk. This typically conveys
 * metadata for images produced by digital cameras. Instances should
 * be treated as immutable, but arrays are not copied defensively.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.eXIf
 */
public record Exif(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "eXIf";
	
	
	/*---- Constructor ----*/
	
	public Exif {
		Objects.requireNonNull(data);
	}
	
	
	public static Exif read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		return new Exif(data);
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
