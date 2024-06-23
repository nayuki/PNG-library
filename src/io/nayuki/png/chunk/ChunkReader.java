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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


/**
 * Helper class for parsing chunks: Common header fields reading, manual
 * reading of many field types, data length checking, CRC-32 handling.
 */
final class ChunkReader implements AutoCloseable {
	
	/*---- Factory ----*/
	
	public static Optional<ChunkReader> tryNew(InputStream in) throws IOException {
		Objects.requireNonNull(in);
		int b = in.read();
		if (b == -1)
			return Optional.empty();
		return Optional.of(new ChunkReader(b, in));
	}
	
	
	
	/*---- Fields ----*/
	
	private InputStream input;
	private Checksum checksum = new CRC32();
	private final String type;
	private int dataRemaining;  // Never negative
	private byte[] buffer = new byte[4];
	
	
	
	/*---- Constructor ----*/
	
	private ChunkReader(int lenByte0, InputStream in) throws IOException {
		input = Objects.requireNonNull(in);
		
		dataRemaining = 3;
		int dataLen = lenByte0 << 24 | readInt(dataRemaining);
		if (dataLen < 0)
			throw new IllegalArgumentException("Chunk data length out of range");
		
		checksum.reset();
		dataRemaining = 4;
		type = readString(StandardCharsets.US_ASCII, false);
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
	
	
	public int readInt32() throws IOException {
		int result = readInt(4);
		if (result == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Invalid int32 value (-2147483648)");
		return result;
	}
	
	
	private int readInt(int numBytes) throws IOException {
		readFully(buffer, 0, numBytes);
		int result = 0;
		for (int i = 0; i < numBytes; i++)
			result = (result << 8) | (buffer[i] & 0xFF);
		return result;
	}
	
	
	public <E extends Enum<E>> E readEnum(E[] allValues) throws IOException {
		int index = readUint8();
		if (0 <= index && index < allValues.length)
			return allValues[index];
		else
			throw new IllegalArgumentException("Unrecognized value for enumeration");
	}
	
	
	public String readString(Charset cs, boolean endByNul) throws IOException {
		if (endByNul) {
			var buf = new byte[1];
			int bufLen = 0;
			while (true) {
				if (dataRemaining <= 0)
					throw new IllegalStateException("Attempt to read too many bytes");
				int b = input.read();
				if (b == -1)
					throw new EOFException();
				checksum.update(b);
				dataRemaining--;
				if (b == 0)
					break;
				if (bufLen >= buf.length) {
					int newLen = (int)Math.min((long)buf.length * 2, Integer.MAX_VALUE - 8);
					if (newLen <= buf.length)
						throw new IllegalArgumentException("String too long");
					buf = Arrays.copyOf(buf, newLen);
				}
				buf[bufLen] = (byte)b;
				bufLen++;
			}
			return new String(buf, 0, bufLen, cs);
		} else {
			var buf = new byte[dataRemaining];
			readFully(buf, 0, buf.length);
			return new String(buf, cs);
		}
	}
	
	
	public void close() throws IOException {
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
		if (readInt32() != (int)crc)
			throw new IllegalArgumentException("Chunk CRC-32 mismatch");
		
		checksum = null;
		input = null;
		dataRemaining = -1;
	}
	
}
