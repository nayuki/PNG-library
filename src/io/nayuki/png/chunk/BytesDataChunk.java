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


/**
 * Convenient mix-in for chunk types that choose to represent all of its payload data as one
 * byte array instead of as various typed fields ({@code int}, {@code String}, enums, etc.).
 */
interface BytesDataChunk extends Chunk {
	
	@Override public default void writeChunk(OutputStream out) throws IOException {
		try (var cout = new ChunkWriter(data().length, getType(), out)) {
			cout.write(data());
		}
	}
	
	
	public byte[] data();
	
}
