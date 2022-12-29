/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Ensures that the exact number of bytes are read/skipped.
 */
final class BoundedInputStream extends FilterInputStream {
	
	private int remain;
	
	
	public BoundedInputStream(InputStream in, int count) {
		super(in);
		if (count < 0)
			throw new IllegalArgumentException();
		remain = count;
	}
	
	
	@Override public int read() throws IOException {
		if (remain < 1)
			throw new IllegalStateException();
		int result = in.read();
		if (result != -1)
			remain--;
		return result;
	}
	
	
	@Override public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	
	@Override public int read(byte[] b, int off, int len) throws IOException {
		if (len > remain)
			throw new IllegalStateException();
		int result = in.read(b, off, len);
		if (result != -1)
			remain -= result;
		return result;
	}
	
	
	@Override public long skip(long n) throws IOException {
		if (!(0 <= n && n <= remain))
			throw new IllegalStateException();
		long result = in.skip(Math.min(n, remain));
		remain -= result;
		return result;
	}
	
	
	@Override public int available() throws IOException {
		return Math.min(in.available(), remain);
	}
	
	
	public void finish() {
		if (remain != 0)
			throw new IllegalStateException();
	}
	
}
