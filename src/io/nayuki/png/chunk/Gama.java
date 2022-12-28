package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import io.nayuki.png.Chunk;


public record Gama(int gamma) implements Chunk {
	
	static final String TYPE = "gAMA";
	
	
	/*---- Constructors ----*/
	
	public Gama {
		if (gamma <= 0)
			throw new IllegalArgumentException();
	}
	
	
	public Gama(double gamma) {
		this(convert(gamma));
	}
	
	
	private static int convert(double gamma) {
		gamma *= 100_000;
		if (!(Double.isFinite(gamma) && 0 < gamma && gamma <= Integer.MAX_VALUE))
			throw new IllegalArgumentException();
		long result = Math.round(gamma);
		if (!(0 < result && result <= Integer.MAX_VALUE))
			throw new IllegalArgumentException();
		return (int)result;
	}
	
	
	public static Gama read(DataInput in) throws IOException {
		return new Gama(in.readInt());
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 4;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(gamma);
	}
	
}
