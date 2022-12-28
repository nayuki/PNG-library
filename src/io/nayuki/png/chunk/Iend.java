/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import io.nayuki.png.Chunk;


public enum Iend implements Chunk {
	
	/*---- Constants ----*/
	
	SINGLETON;
	
	static final String TYPE = "IEND";
	
	private static final byte[] DATA = {};
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 0;
	}
	
	
	@Override public byte[] getData() {
		return DATA;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {}
	
}
