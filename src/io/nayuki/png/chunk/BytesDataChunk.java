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
import io.nayuki.png.Chunk;


/**
 * Convenient mix-in for chunk types that choose to represent all of its payload data as one
 * byte array instead of as various typed fields ({@code int}, {@code String}, enums, etc.).
 */
interface BytesDataChunk extends Chunk {
	
	public byte[] data();
	
	
	@Override public default byte[] getData() {
		return data();
	}
	
	
	@Override public default void writeData(DataOutput out) throws IOException {
		out.write(data());
	}
	
}
