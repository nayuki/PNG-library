/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import io.nayuki.png.Chunk;


/**
 * Convenient mix-in for chunk types whose data length has a small upper bound,
 * and thus the chunk can be serialized to memory to calculate the length.
 */
interface SmallDataChunk extends Chunk {
	
	public abstract void writeData(ChunkWriter out) throws IOException;
	
	
	@Override public default void writeChunk(OutputStream out) throws IOException {
		var bout = new ByteArrayOutputStream();
		try {
			writeData(new ChunkWriter(Integer.MAX_VALUE, "AAAA", bout));
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		byte[] temp = bout.toByteArray();
		byte[] data = Arrays.copyOfRange(temp, 8, temp.length);
		var cout = new ChunkWriter(data.length, getType(), out);
		cout.write(data);
		cout.finish();
	}
	
}
