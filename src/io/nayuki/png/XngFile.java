/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import io.nayuki.png.chunk.Custom;
import io.nayuki.png.chunk.Util;


/**
 * A low-level representation of PNG/MNG/JNG chunks and methods to read/write such files.
 */
public record XngFile(Type type, List<Chunk> chunks) {
	
	public XngFile {
		Objects.requireNonNull(type);
		Objects.requireNonNull(chunks);
	}
	
	
	public static XngFile read(InputStream in, boolean parse) throws IOException {
		var din0 = new DataInputStream(in);
		
		var sig = new byte[8];
		din0.readFully(sig);
		Type fileType = null;
		for (Type t : Type.values()) {
			if (Arrays.equals(t.signature, sig))
				fileType = t;
		}
		if (fileType == null)
			throw new IllegalArgumentException();
		
		List<Chunk> chunks = new ArrayList<>();
		while (true) {
			int dataLen = din0.read();
			if (dataLen == -1)
				break;
			for (int i = 0; i < 3; i++)
				dataLen = (dataLen << 8) | din0.readUnsignedByte();
			if (dataLen < 0)
				throw new IllegalArgumentException();
			
			var cin = new CheckedInputStream(in, new CRC32());
			DataInput din1 = new DataInputStream(cin);
			var sb = new StringBuilder();
			for (int i = 0; i < 4; i++)
				sb.append((char)din1.readUnsignedByte());
			String type = sb.toString();
			Chunk.checkType(type);
			if ((type.charAt(2) & 0x20) != 0)
				throw new IllegalArgumentException();
			
			var bin = new BoundedInputStream(cin, dataLen);
			if (parse)
				chunks.add(Util.readChunk(type, dataLen, new DataInputStream(bin)));
			else
				chunks.add(Custom.read(type, dataLen, new DataInputStream(bin)));
			bin.finish();
			
			long crc = cin.getChecksum().getValue();
			if (crc >>> 32 != 0)
				throw new AssertionError();
			if (din0.readInt() != (int)crc)
				throw new IllegalArgumentException();
		}
		return new XngFile(fileType, chunks);
	}
	
	
	public void write(OutputStream out) throws IOException {
		out.write(type.getSignature());
		DataOutput dout = new DataOutputStream(out);
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
	
	
	
	public enum Type {
		PNG(0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'),
		MNG(0x8A, 'M', 'N', 'G', '\r', '\n', 0x1A, '\n'),
		JNG(0x8B, 'J', 'N', 'G', '\r', '\n', 0x1A, '\n');
		
		private final byte[] signature;
		
		private Type(int... sig) {
			signature = new byte[sig.length];
			for (int i = 0; i < sig.length; i++)
				signature[i] = (byte)sig[i];
		}
		
		public byte[] getSignature() {
			return signature.clone();
		}
	}
	
}
