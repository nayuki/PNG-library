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
		out.write(SIGNATURE);
		var dout = new DataOutputStream(out);
		for (Chunk chunk : chunks) {
			int dataLen = chunk.getDataLength();
			if (dataLen < 0)
				throw new IllegalArgumentException();
			dout.writeInt(dataLen);
			
			String type = chunk.getType();
			Chunk.checkType(type);
			var cout = new CheckedOutputStream(out, new CRC32());
			cout.write(type.getBytes(StandardCharsets.US_ASCII));
			
			var bout = new BoundedOutputStream(cout, dataLen);
			chunk.writeData(new DataOutputStream(bout));
			bout.finish();
			
			long crc = cout.getChecksum().getValue();
			if (crc >>> 32 != 0)
				throw new AssertionError();
			dout.writeInt((int)crc);
		}
	}
	
	
	private static final byte[] SIGNATURE = {(byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
	
}
