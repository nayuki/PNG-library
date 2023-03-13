/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A custom chunk that is not parsed. This can represent a chunk of a
 * type that already has a defined class (e.g. {@code class Ihdr} for
 * type "IHDR") or it can represent an unrecognized chunk type. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#5Chunk-layout
 */
public record Custom(String type, byte[] data) implements BytesDataChunk {
	
	/*---- Constructor and factory ----*/
	
	public Custom {
		Chunk.checkType(type);
		Objects.requireNonNull(data);
	}
	
	
	/**
	 * Reads from the specified chunk reader, stores the byte data
	 * and type string, and returns a new chunk object of this type.
	 * @param in the chunk reader to read the chunk's data from (not {@code null})
	 * @return a new {@code Custom} chunk object (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IOException if an I/O exception occurs
	 */
	public static Custom read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		return new Custom(in.getType(), in.readRemainingBytes());
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return type;
	}
	
}
