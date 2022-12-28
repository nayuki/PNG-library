package io.nayuki.png;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public final class BoundedOutputStream extends FilterOutputStream {
	
	private int count;
	
	
	public BoundedOutputStream(OutputStream out, int count) {
		super(out);
		if (count < 0)
			throw new IllegalArgumentException();
		this.count = count;
	}
	
	
	@Override public void write(int b) throws IOException {
		if (count < 1)
			throw new IllegalStateException();
		count--;
		out.write(b);
	}
	
	
	@Override public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	
	@Override public void write(byte[] b, int off, int len) throws IOException {
		if (!(0 <= len && len <= count))
			throw new IllegalStateException();
		count -= len;
		out.write(b, off, len);
	}
	
	
	public void finish() {
		if (count != 0)
			throw new IllegalStateException();
	}
	
}
