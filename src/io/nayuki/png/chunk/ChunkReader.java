/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import io.nayuki.png.Chunk;


public final class ChunkReader {
	
	/*---- Fields ----*/
	
	private InputStream input;
	private Checksum checksum = new CRC32();
	private final String type;
	private int dataRemaining;  // Never negative
	private byte[] buffer = new byte[4];
	
	
	
	/*---- Constructor ----*/
	
	public ChunkReader(InputStream in) throws IOException {
		input = Objects.requireNonNull(in);
		
		dataRemaining = 4;
		int dataLen = readInt();
		if (dataLen < 0)
			throw new IllegalArgumentException("Chunk data length out of range");
		
		checksum.reset();
		dataRemaining = 4;
		readFully(buffer, 0, 4);
		type = new String(buffer, 0, 4, StandardCharsets.US_ASCII);
		Chunk.checkType(type);
		
		dataRemaining = dataLen;
	}
	
	
	
	/*---- Methods ----*/
	
	public int getRemainingCount() {
		return dataRemaining;
	}
	
	
	public void readFully(byte[] b, int off, int len) throws IOException {
		Objects.checkFromIndexSize(off, len, b.length);
		if (dataRemaining < len)
			throw new IllegalStateException("Attempt to read too many bytes");
		for (int end = off + len; off < end; ) {
			int n = input.read(b, off, end - off);
			if (n == -1)
				throw new EOFException();
			checksum.update(b, off, n);
			off += n;
			dataRemaining -= n;
		}
	}
	
	
	public String getType() throws IOException {
		return type;
	}
	
	
	public byte[] readRemainingBytes() throws IOException {
		var result = new byte[dataRemaining];
		readFully(result, 0, result.length);
		return result;
	}
	
	
	public int readUint8() throws IOException {
		return readInt(1);
	}
	
	
	public int readUint16() throws IOException {
		return readInt(2);
	}
	
	
	public int readInt() throws IOException {
		return readInt(4);
	}
	
	
	private int readInt(int numBytes) throws IOException {
		readFully(buffer, 0, numBytes);
		int result = 0;
		for (int i = 0; i < numBytes; i++)
			result = (result << 8) | (buffer[i] & 0xFF);
		return result;
	}
	
	
	public void finish() throws IOException {
		if (input == null)
			throw new IllegalStateException("Already finished");
		if (dataRemaining > 0)
			throw new IllegalStateException("Read too few bytes");
		if (dataRemaining < 0)  // Due to external bad concurrency or internal logic error
			throw new AssertionError("Read too many bytes");
		
		long crc = checksum.getValue();
		if (crc >>> 32 != 0)
			throw new AssertionError("Unreachable value");
		dataRemaining = 4;
		if (readInt() != (int)crc)
			throw new IllegalArgumentException("Chunk CRC-32 mismatch");
		
		checksum = null;
		input = null;
		dataRemaining = -1;
	}
	
}
