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
 * A digital signature (dSIG) chunk. This allows detecting unauthorized changes
 * to the chunks enclosed in a pair of digital signature chunks. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#RC.dSIG
 */
public record Dsig(byte[] data) implements Chunk {
	
	static final String TYPE = "dSIG";
	
	
	/*---- Constructor ----*/
	
	public Dsig {
		Objects.requireNonNull(data);
	}
	
	
	public static Dsig read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		return new Dsig(data);
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
