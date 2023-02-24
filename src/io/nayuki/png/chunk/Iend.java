/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;


/**
 * An image trailer (IEND) chunk. This marks the end of a PNG data stream.
 * There is a singleton immutable instance because this chunk type has no data.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11IEND
 */
public enum Iend implements BytesDataChunk {
	
	/*---- Constants ----*/
	
	SINGLETON;
	
	static final String TYPE = "IEND";
	
	
	/*---- Methods ----*/
	
	@Override public byte[] data() {
		return new byte[]{};
	}
	
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {}
	
}
