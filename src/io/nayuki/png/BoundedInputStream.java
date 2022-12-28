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


public final class BoundedInputStream extends FilterInputStream {
	
	private int count;
	
	
	public BoundedInputStream(InputStream in, int count) {
		super(in);
		if (count < 0)
			throw new IllegalArgumentException();
		this.count = count;
	}
	
	
	@Override public int read() throws IOException {
		if (count < 1)
			throw new IllegalStateException();
		int result = in.read();
		if (result != -1)
			count--;
		return result;
	}
	
	
	@Override public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	
	@Override public int read(byte[] b, int off, int len) throws IOException {
		if (!(0 <= len && len <= count))
			throw new IllegalStateException();
		int result = in.read(b, off, len);
		if (result != -1)
			count -= result;
		return result;
	}
	
	
	@Override public long skip(long n) throws IOException {
		if (!(0 <= n && n <= count))
			throw new IllegalStateException();
		long result = in.skip(Math.min(n, count));
		count -= result;
		return result;
	}
	
	
	@Override public int available() throws IOException {
		return Math.min(in.available(), count);
	}
	
	
	public void finish() {
		if (count != 0)
			throw new IllegalStateException();
	}
	
}
