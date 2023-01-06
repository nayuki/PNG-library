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
import io.nayuki.png.Chunk;


/**
 * A custom chunk that is not parsed. This can represent a chunk of a
 * type that already has a defined class (e.g. {@code class Ihdr} for
 * type "IHDR") or it can represent an unrecognized chunk type. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#5Chunk-layout
 */
public record Custom(String type, byte[] data) implements BytesDataChunk {
	
	/*---- Constructor ----*/
	
	public Custom {
		Chunk.checkType(type);
		Objects.requireNonNull(data);
	}
	
	
	public static Custom read(String type, int dataLen, DataInput in) throws IOException {
		Objects.requireNonNull(type);
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		return new Custom(type, Util.readBytes(in, dataLen));
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return type;
	}
	
}
