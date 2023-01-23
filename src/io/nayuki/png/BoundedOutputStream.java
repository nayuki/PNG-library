/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Wraps an output stream to ensure that the exact number of bytes are written.
 * Calls to {@code write()} enforce an upper bound on the total number of bytes
 * written. Call {@code finish()} at the end to also ensure that the total bytes
 * written is not less than the initially specified count. Example usage:
 * <pre>OutputStream out = (...);
 *var bout = new BoundedOutputStream(out, 3);
 *bout.write(0xFF);
 *bout.write(new byte[2]);
 *bout.finish();
 *(... continue using out ...)</pre>
 */
final class BoundedOutputStream extends FilterOutputStream {
	
	private int remain;  // Never negative
	
	
	public BoundedOutputStream(OutputStream out, int count) {
		super(out);
		if (count < 0)
			throw new IllegalArgumentException("Negative byte count");
		remain = count;
	}
	
	
	@Override public void write(int b) throws IOException {
		if (remain < 1)
			throw new IllegalStateException("Insufficient remaining bytes");
		out.write(b);
		remain--;
	}
	
	
	@Override public void write(byte[] b, int off, int len) throws IOException {
		if (len > remain)
			throw new IllegalStateException("Insufficient remaining bytes");
		out.write(b, off, len);
		remain -= len;
	}
	
	
	// Checks that precisely the expected number of bytes were written.
	public void finish() {
		if (remain > 0)
			throw new IllegalStateException("Wrote too few bytes");
		else if (remain < 0)  // Due to external bad concurrency or internal logic error
			throw new AssertionError("Wrote too many bytes");
	}
	
}
