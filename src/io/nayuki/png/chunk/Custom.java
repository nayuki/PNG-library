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
 */
public record Custom(String type, byte[] data) implements BytesDataChunk {
	
	/*---- Constructor ----*/
	
	public Custom {
		Chunk.checkType(type);
		Objects.requireNonNull(data);
	}
	
	
	public static Custom read(String type, int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		return new Custom(type, data);
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return type;
	}
	
}
