package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Trns(byte[] data) implements Chunk {
	
	/*---- Constructors ----*/
	
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
	
	
	public static Trns read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		return new Trns(data);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "tRNS";
	}
	
	
	@Override public byte[] getData() {
		return data;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(data);
	}
	
}
