/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.chunk.Util;


/**
 * A low-level representation of a list of PNG/MNG/JNG chunks, plus methods to read/write
 * such files. Any list of chunks is considered valid; this structure will not check any
 * constraints within or between chunks (e.g. invalid field values, chunks after IEND).
 * Instances should be treated as immutable, but lists and chunks are not copied defensively.
 */
public record XngFile(Type type, List<Chunk> chunks) {
	
	public XngFile {
		Objects.requireNonNull(type);
		Objects.requireNonNull(chunks);
	}
	
	
	/**
	 * Reads the specified input file and returns a new {@code XngFile}
	 * object representing the type and chunks that were read.
	 * If {@code parse} is false, then every chunk returned is a {@link Custom};
	 * otherwise, every chunk that matches a known type (e.g. "IHDR") will be read with that
	 * type's specific parser and return that an object of that class (e.g. {@link Ihdr}).
	 * @param inFile the input file to read from
	 * @param parse whether to try to parse each chunk's internal fields
	 * @throws NullPointerException if {@code inFile} is {@code null}
	 * @throws IOException if an I/O exception occurs
	 * @returns a new {@code XngFile} object representing the type and chunks read
	 */
	public static XngFile read(File inFile, boolean parse) throws IOException {
		Objects.requireNonNull(inFile);
		try (var in = new BufferedInputStream(new FileInputStream(inFile))) {
			return read(in, parse);
		}
	}
	
	
	/**
	 * Reads the specified input stream and returns a new {@code XngFile} object
	 * representing the type and chunks that were read. This does not close the stream.
	 * This reads until the end of stream if no exception is thrown.
	 * If {@code parse} is false, then every chunk returned is a {@link Custom};
	 * otherwise, every chunk that matches a known type (e.g. "IHDR") will be read with that
	 * type's specific parser and return that an object of that class (e.g. {@link Ihdr}).
	 * @param in the input stream to read from
	 * @param parse whether to try to parse each chunk's internal fields
	 * @throws NullPointerException if {@code in} is {@code null}
	 * @throws IOException if an I/O exception occurs
	 * @returns a new {@code XngFile} object representing the type and chunks read
	 */
	public static XngFile read(InputStream in, boolean parse) throws IOException {
		Objects.requireNonNull(in);
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
	
	
	/**
	 * Writes the type and chunks of this XNG file to the specified output file.
	 * @param outFile the output file to write to
	 * @throws NullPointerException if {@code outFile} is {@code null}
	 * @throws IOException if an I/O exception occurs
	 */
	public void write(File outFile) throws IOException {
		Objects.requireNonNull(outFile);
		try (var out = new BufferedOutputStream(new FileOutputStream(outFile))) {
			write(out);
		}
	}
	
	
	/**
	 * Writes the type and chunks of this XNG file to the
	 * specified output stream. This does not close the stream.
	 * @param out the output stream to write to
	 * @throws NullPointerException if {@code out} is {@code null}
	 * @throws IOException if an I/O exception occurs
	 */
	public void write(OutputStream out) throws IOException {
		Objects.requireNonNull(out);
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
	
	
	
	/*---- Enumeration ----*/
	
	/**
	 * Distinguishes between PNG/MNG/JNG files.
	 */
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
		
		/**
		 * Returns a new length-8 array representing file header signature for this type.
		 */
		public byte[] getSignature() {
			return signature.clone();
		}
	}
	
}
