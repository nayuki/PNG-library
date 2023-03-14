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


/**
 * A palette (PLTE) chunk. This is required for indexed-color images,
 * but only behaves as a suggestion for true-color images. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11PLTE
 */
public record Plte(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "PLTE";
	
	
	/*---- Constructor and factory ----*/
	
	public Plte {
		Objects.requireNonNull(data);
		if (data.length % 3 != 0)
			throw new IllegalArgumentException("Invalid data length");
		int numEntries = data.length / 3;
		if (!(1 <= numEntries && numEntries <= 256))
			throw new IllegalArgumentException("Number of entries out of range");
	}
	
	
	static Plte read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		return new Plte(in.readRemainingBytes());
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
