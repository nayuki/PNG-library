package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Trns(byte[] data) implements Chunk {
	
	/*---- Constructor ----*/
	
	public Trns {
		Objects.requireNonNull(data);
		if (data.length > 256)
			throw new IllegalArgumentException();
	}
	
	
	public Trns(short[] channelValues) {
		this(convert(channelValues));
	}
	
	
	private static byte[] convert(short[] channelValues) {
		Objects.requireNonNull(channelValues);
		if (channelValues.length != 1 && channelValues.length != 3)
			throw new IllegalArgumentException();
		ByteBuffer bb = ByteBuffer.allocate(Math.multiplyExact(channelValues.length, Short.BYTES));
		bb.asShortBuffer().put(channelValues);
		if (!bb.hasArray())
			throw new AssertionError();
		return bb.array();
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "tRNS";
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
