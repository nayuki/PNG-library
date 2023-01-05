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
 * Wraps an input stream to ensure that the exact number of bytes are read/skipped.
 * Calls to {@code read()} and {@code skip()} enforce an upper bound on the total number
 * of bytes read/skipped. Call {@code finish()} at the end to also ensure that the total
 * bytes read/skipped is not less than the initially specified count. Example usage:
 * <pre>InputStream in = (...);
 *var bin = new BoundedInputStream(in, 3);
 *bin.read();
 *bin.read(new byte[2]);
 *bin.finish();
 *(... continue using in ...)</pre>
 * <p>If the underlying stream initially has at least as many bytes as the
 * byte count passed into the constructor, then this object will never
 * return an EOF because the bound takes priority over the end of stream.</p>
 */
final class BoundedInputStream extends FilterInputStream {
	
	private int remain;  // Never negative
	
	
	public BoundedInputStream(InputStream in, int count) {
		super(in);
		if (count < 0)
			throw new IllegalArgumentException("Negative byte count");
		remain = count;
	}
	
	
	@Override public int read() throws IOException {
		if (remain < 1)
			throw new IllegalStateException("Insufficient remaining bytes");
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
			throw new IllegalStateException("Insufficient remaining bytes");
		int result = in.read(b, off, len);
		if (result != -1)
			remain -= result;
		return result;
	}
	
	
	@Override public long skip(long n) throws IOException {
		if (!(0 <= n && n <= remain))
			throw new IllegalStateException("Insufficient remaining bytes");
		long result = in.skip(Math.min(n, remain));
		remain -= result;
		return result;
	}
	
	
	@Override public int available() throws IOException {
		return Math.min(in.available(), remain);
	}
	
	
	// Checks that precisely the expected number of bytes were read.
	public void finish() {
		if (remain > 0)
			throw new IllegalStateException("Read too few bytes");
		else if (remain < 0)  // Due to external bad concurrency or internal logic error
			throw new AssertionError("Read too many bytes");
	}
	
}
