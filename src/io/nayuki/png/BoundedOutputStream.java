/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Ensures that the exact number of bytes are written.
 */
final class BoundedOutputStream extends FilterOutputStream {
	
	private int remain;
	
	
	public BoundedOutputStream(OutputStream out, int count) {
		super(out);
		if (count < 0)
			throw new IllegalArgumentException();
		this.remain = count;
	}
	
	
	@Override public void write(int b) throws IOException {
		if (remain < 1)
			throw new IllegalStateException();
		remain--;
		out.write(b);
	}
	
	
	@Override public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	
	@Override public void write(byte[] b, int off, int len) throws IOException {
		if (!(0 <= len && len <= remain))
			throw new IllegalStateException();
		remain -= len;
		out.write(b, off, len);
	}
	
	
	public void finish() {
		if (remain != 0)
			throw new IllegalStateException();
	}
	
}
