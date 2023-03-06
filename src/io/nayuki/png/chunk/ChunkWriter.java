/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import io.nayuki.png.Chunk;


public final class ChunkWriter {
	
	/*---- Fields ----*/
	
	private OutputStream output;
	private Checksum checksum = new CRC32();
	private int dataRemaining;  // Never negative
	
	
	
	/*---- Constructor ----*/
	
	public ChunkWriter(int dataLen, String type, OutputStream out) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Chunk.checkType(type);
		output = Objects.requireNonNull(out);
		
		dataRemaining = 8;
		writeInt32(dataLen);
		checksum.reset();
		writeAscii(type);
		dataRemaining = dataLen;
	}
	
	
	
	/*---- Methods ----*/
	
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}
	
	
	public void write(byte[] b, int off, int len) throws IOException {
		if (dataRemaining < len)
			throw new IllegalStateException("Attempt to write too many bytes");
		output.write(b, off, len);
		checksum.update(b, off, len);
		dataRemaining -= len;
	}
	
	
	public void writeUint8(int val) throws IOException {
		writeInt(val, 1);
	}
	
	
	public void writeUint16(int val) throws IOException {
		writeInt(val, 2);
	}
	
	
	public void writeInt32(int val) throws IOException {
		writeInt(val, 4);
	}
	
	
	private void writeInt(int val, int numBytes) throws IOException {
		var b = new byte[numBytes];
		for (int i = b.length - 1; i >= 0; i--, val >>>= 8)
			b[i] = (byte)val;
		write(b);
	}
	
	
	public void writeAscii(String s) throws IOException {
		write(s.getBytes(StandardCharsets.US_ASCII));
	}
	
	
	public void finish() throws IOException {
		if (output == null)
			throw new IllegalStateException("Already finished");
		if (dataRemaining > 0)
			throw new IllegalStateException("Wrote too few bytes");
		if (dataRemaining < 0)  // Due to external bad concurrency or internal logic error
			throw new AssertionError("Wrote too many bytes");
		
		long crc = checksum.getValue();
		if (crc >>> 32 != 0)
			throw new AssertionError("Unreachable value");
		dataRemaining = 4;
		writeInt32((int)crc);
		
		checksum = null;
		output = null;
		dataRemaining = -1;
	}
	
}
