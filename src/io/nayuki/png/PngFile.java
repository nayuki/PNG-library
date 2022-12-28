package io.nayuki.png;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;


public final class PngFile {
	
	public List<Chunk> chunks = new ArrayList<>();
	
	
	public void write(File outFile) throws IOException {
		try (var out = new BufferedOutputStream(new FileOutputStream(outFile))) {
			write(out);
		}
	}
	
	
	public void write(OutputStream out) throws IOException {
		var cout = new CheckedOutputStream(out, new CRC32());
		var dout = new DataOutputStream(cout);
		dout.write(SIGNATURE);
		for (Chunk chunk : chunks) {
			dout.writeInt(chunk.getDataLength());
			cout.getChecksum().reset();
			
			String type = chunk.getType();
			checkChunkType(type);
			dout.write(type.getBytes(StandardCharsets.US_ASCII));
			
			chunk.writeData(dout);
			long crc = cout.getChecksum().getValue();
			if (crc >>> 32 != 0)
				throw new AssertionError();
			dout.writeInt((int)crc);
		}
	}
	
	
	private static void checkChunkType(String type) {
		Objects.requireNonNull(type);
		if (type.length() != 4)
			throw new IllegalArgumentException();
		for (int i = 0; i < type.length(); i++) {
			char c = type.charAt(i);
			if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z'))
				throw new IllegalArgumentException();
		}
	}
	
	
	private static final byte[] SIGNATURE = {(byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
	
}
