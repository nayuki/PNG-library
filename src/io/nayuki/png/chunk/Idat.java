/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * An image data (IDAT) chunk. This contains pixel data that is filtered and compressed.
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11IDAT
 */
public record Idat(byte[] data) implements Chunk {
	
	static final String TYPE = "IDAT";
	
	
	/*---- Constructor ----*/
	
	public Idat {
		Objects.requireNonNull(data);
	}
	
	
	public static Idat read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		return new Idat(data);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public byte[] getData() {
		return data;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(data);
	}
	
}
