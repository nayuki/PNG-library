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
		var cout = new CheckedOutputStream(out, new CRC32());
		var dout = new DataOutputStream(cout);
		dout.write(SIGNATURE);
		for (Chunk chunk : chunks) {
			dout.writeInt(chunk.getDataLength());
			cout.getChecksum().reset();
			dout.write(chunk.getType().getBytes(StandardCharsets.US_ASCII));
			chunk.writeData(dout);
			dout.writeInt((int)cout.getChecksum().getValue());
		}
	}
	
	
	private static final byte[] SIGNATURE = {(byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
	
}
