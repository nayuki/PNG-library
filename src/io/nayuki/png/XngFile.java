/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import io.nayuki.png.chunk.Custom;
import io.nayuki.png.chunk.Ihdr;


/**
 * A low-level representation of a list of PNG/MNG/JNG chunks, plus methods
 * to read/write such files. This class checks and handles chunk lengths,
 * basic constraints on chunk types as defined by {@link Chunk#checkType(String)},
 * and CRC-32 values. This does not check the data within chunks (e.g. invalid
 * field values) or constraints between chunks (e.g. chunks after IEND). Instances
 * should be treated as immutable, but lists and chunks are not copied defensively.
 */
public record XngFile(Type type, List<Chunk> chunks) {
	
	/**
	 * Constructs a new XNG file with the specified type and list of chunks.
	 * @param type the file type (PNG/MNG/JNG) (not {@code null})
	 * @param chunks the list of chunks (not {@code null})
	 */
	public XngFile {
		Objects.requireNonNull(type);
		Objects.requireNonNull(chunks);
	}
	
	
	/**
	 * Reads the specified input file and returns a new {@code XngFile} object
	 * representing the type and chunks that were read. If {@code parse} is false,
	 * then every chunk returned is a {@link Custom}; otherwise, every chunk that
	 * matches a known type (e.g. "IHDR") will be read with that type's specific
	 * parser and return that an object of that class (e.g. {@link Ihdr}).
	 * @param inFile the input file to read from
	 * @param parse whether to try to parse each chunk's internal fields
	 * @return a new {@code XngFile} object representing the type and chunks read
	 * @throws NullPointerException if {@code inFile} is {@code null}
	 * @throws IllegalArgumentException if the file contains invalid data in the header
	 * signature, chunk outer structure, or chunk inner structure (if parsing is enabled)
	 * @throws IOException if an I/O exception occurs
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
	 * This reads until the end of stream if no exception is thrown. If {@code parse}
	 * is false, then every chunk returned is a {@link Custom}; otherwise, every chunk
	 * that matches a known type (e.g. "IHDR") will be read with that type's
	 * specific parser and return that an object of that class (e.g. {@link Ihdr}).
	 * @param in the input stream to read from
	 * @param parse whether to try to parse each chunk's internal fields
	 * @return a new {@code XngFile} object representing the type and chunks read
	 * @throws NullPointerException if {@code in} is {@code null}
	 * @throws IllegalArgumentException if the stream contains invalid data in the header
	 * signature, chunk outer structure, or chunk inner structure (if parsing is enabled)
	 * @throws IOException if an I/O exception occurs
	 */
	public static XngFile read(InputStream in, boolean parse) throws IOException {
		Objects.requireNonNull(in);
		
		var sig = new byte[8];
		new DataInputStream(in).readFully(sig);
		Type fileType = null;
		for (Type t : Type.values()) {
			if (Arrays.equals(t.signature, sig))
				fileType = t;
		}
		if (fileType == null)
			throw new IllegalArgumentException("Unrecognized file signature");
		
		List<Chunk> chunks = new ArrayList<>();
		while (true) {
			Optional<? extends Chunk> chk = parse ? Chunk.read(in) : Custom.read(in);
			if (chk.isEmpty())
				break;
			chunks.add(chk.get());
		}
		return new XngFile(fileType, chunks);
	}
	
	
	/**
	 * Returns the single chunk that matches the specified type or empty.
	 * @param <T> the chunk type
	 * @param type the class object of the desired chunk type
	 * @return the single chunk matching the type or empty
	 * @throws IllegalArgumentException if multiple chunks match the type
	 */
	public <T> Optional<T> getChunk(Class<T> type) {
		Optional<T> result = Optional.empty();
		for (Chunk chk : chunks) {
			if (type.isInstance(chk)) {
				if (result.isPresent())
					throw new IllegalArgumentException("Multiple chunks with given type");
				result = Optional.of(type.cast(chk));
			}
		}
		return result;
	}
	
	
	/**
	 * Returns a readable list of all the chunks that
	 * match the specified type, possibly an empty list.
	 * @param <T> the chunk type
	 * @param type the class object of the desired chunk type
	 * @return a list (not {@code null}) of all the chunks matching the type
	 */
	public <T> List<T> getChunks(Class<T> type) {
		return chunks.stream()
			.filter(chk -> type.isInstance(chk))
			.map(chk -> type.cast(chk))
			.toList();
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
		for (Chunk chk : chunks)
			chk.writeChunk(out);
	}
	
	
	
	/*---- Enumeration ----*/
	
	/**
	 * Distinguishes between PNG/MNG/JNG files.
	 */
	public enum Type {
		/** The Portable Network Graphics (PNG) file format. */
		PNG(0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'),
		/** The Multiple-image Network Graphics (MNG) file format. */
		MNG(0x8A, 'M', 'N', 'G', '\r', '\n', 0x1A, '\n'),
		/** The JPEG Network Graphics (JNG) file format. */
		JNG(0x8B, 'J', 'N', 'G', '\r', '\n', 0x1A, '\n');
		
		private final byte[] signature;
		
		private Type(int... sig) {
			signature = new byte[sig.length];
			for (int i = 0; i < sig.length; i++)
				signature[i] = (byte)sig[i];
		}
		
		/**
		 * Returns a new length-8 array representing file header signature for this type.
		 * @return this file type's signature (not {@code null})
		 */
		public byte[] getSignature() {
			return signature.clone();
		}
	}
	
}
