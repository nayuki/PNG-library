package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Idat(byte[] data) implements Chunk {
	
	/*---- Constructor ----*/
	
	public Idat {
		Objects.requireNonNull(data);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "IDAT";
	}
	
	
	@Override public int getDataLength() {
		return data.length;
	}
	
	
	@Override public byte[] getData() {
		return data;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(data);
	}
	
}
