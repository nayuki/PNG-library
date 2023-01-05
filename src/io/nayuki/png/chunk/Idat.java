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
 * An image data (IDAT) chunk. This contains pixel data that is filtered and compressed.
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11IDAT
 */
public record Idat(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "IDAT";
	
	
	/*---- Constructor ----*/
	
	public Idat {
		Objects.requireNonNull(data);
	}
	
	
	public static Idat read(int dataLen, DataInput in) throws IOException {
		return new Idat(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
