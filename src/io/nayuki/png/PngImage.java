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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Iend;
import io.nayuki.png.chunk.Ihdr;


/**
 * A structured representation of chunks that form a PNG file. Instances are mutable.
 * There is some degree of validation and exclusion of invalid data. When serializing
 * an instance, the resulting list of chunks is: field {@code ihdr} (must be
 * present), field {@beforeIdats} (zero or more), field {@code idats} (one or more),
 * field {@code afterIdats} (zero or more), {@code Iend.SINGLETON} (implicit).
 * This class operates at the next level up from {@link XngFile}.
 */
public final class PngImage {
	
	/**
	 * Reads the specified input file and returns a new
	 * {@code PngImage} object representing chunks read.
	 * @param inFile the input file to read from
	 * @return a new {@code XngFile} object representing chunks read
	 * @throws NullPointerException if {@code inFile} is {@code null}
	 * @throws IllegalArgumentException if the file contains invalid data in the header
	 * signature, chunk outer structure, chunk inner structure, or constraints between chunks
	 * @throws IOException if an I/O exception occurs
	 */
	public static PngImage read(File inFile) throws IOException {
		Objects.requireNonNull(inFile);
		try (var in = new BufferedInputStream(new FileInputStream(inFile))) {
			return read(in);
		}
	}
	
	
	/**
	 * Reads the specified input stream and returns a new {@code PngImage}
	 * object representing chunks read. This does not close the stream.
	 * This reads until the end of stream if no exception is thrown.
	 * @param in the input stream to read from
	 * @return a new {@code XngFile} object representing chunks read
	 * @throws NullPointerException if {@code inFile} is {@code null}
	 * @throws IllegalArgumentException if the stream contains invalid data in the header
	 * signature, chunk outer structure, chunk inner structure, or constraints between chunks
	 * @throws IOException if an I/O exception occurs
	 */
	public static PngImage read(InputStream in) throws IOException {
		Objects.requireNonNull(in);
		XngFile xng = XngFile.read(in, true);
		if (xng.type() != XngFile.Type.PNG)
			throw new IllegalArgumentException("File signature is not PNG");
		return new PngImage(xng.chunks());
	}
	
	
	/** The single IHDR chunk, if present. */
	public Optional<Ihdr> ihdr = Optional.empty();
	
	/** The chunks positioned before the IDAT chunks. */
	public List<Chunk> beforeIdats = new ArrayList<>();
	
	/** The consecutive IDAT chunks. */
	public List<Idat> idats = new ArrayList<>();
	
	/** The chunks positioned after the IDAT chunks. */
	public List<Chunk> afterIdats = new ArrayList<>();
	
	
	/**
	 * Constructs a blank PNG image where all fields are initially empty (not {@code null}).
	 */
	public PngImage() {}
	
	
	private PngImage(List<Chunk> chunks) {
		enum State {
			BEFORE_IHDR,
			AFTER_IHDR,
			DURING_IDATS,
			AFTER_IDATS,
			AFTER_IEND,
		}
		State state = State.BEFORE_IHDR;
		Set<String> seenChunkTypes = new HashSet<>();
		for (Chunk chunk : chunks) {
			String type = chunk.getType();
			if (!seenChunkTypes.add(type) && UNIQUE_CHUNK_TYPES.contains(type))
				throw new IllegalArgumentException("Duplicate " + type + " chunk");
			
			switch (state) {
				case BEFORE_IHDR: {
					if (chunk instanceof Ihdr chk) {
						ihdr = Optional.of(chk);
						state = State.AFTER_IHDR;
					} else
						throw new IllegalArgumentException("Expected IHDR chunk");
					break;
				}
				
				case AFTER_IHDR: {
					if (chunk instanceof Idat chk) {
						idats.add(chk);
						state = State.DURING_IDATS;
					} else if (chunk instanceof Iend)
						throw new IllegalArgumentException("Unexpected IEND chunk");
					else
						beforeIdats.add(chunk);
					break;
				}
				
				case DURING_IDATS: {
					if (chunk instanceof Idat chk)
						idats.add(chk);
					else if (chunk instanceof Iend)
						state = State.AFTER_IEND;
					else {
						afterIdats.add(chunk);
						state = State.AFTER_IDATS;
					}
					break;
				}
				
				case AFTER_IDATS: {
					if (chunk instanceof Iend)
						state = State.AFTER_IEND;
					else if (chunk instanceof Idat)
						throw new IllegalArgumentException("Non-consecutive IDAT chunk");
					else {
						afterIdats.add(chunk);
						state = State.AFTER_IDATS;
					}
					break;
				}
				
				case AFTER_IEND:
					throw new IllegalArgumentException("Unexpected chunk after IEND");
				
				default:
					throw new AssertionError();
			}
		}
		if (state != State.AFTER_IEND)
			throw new IllegalArgumentException("Missing some required chunks");
	}
	
	
	private static final Set<String> UNIQUE_CHUNK_TYPES = new HashSet<>(Arrays.asList(
		"bKGD",
		"cHRM",
		"gAMA",
		"hIST",
		"iCCP",
		"IEND",
		"IHDR",
		"pHYs",
		"PLTE",
		"sBIT",
		"sRGB",
		"tIME",
		"tRNS"));
	
	
	/**
	 * Writes the signature and chunks of this PNG file to the specified output file.
	 * @throws NullPointerException if {@code outFile}
	 * or any of this object's fields is {@code null}
	 * @throws IllegalStateException if the current
	 * lists of chunks do not form a valid PNG file
	 * @throws IOException if an I/O exception occurs
	 */
	public void write(File outFile) throws IOException {
		Objects.requireNonNull(outFile);
		try (var out = new BufferedOutputStream(new FileOutputStream(outFile))) {
			write(out);
		}
	}
	
	
	/**
	 * Writes the signature and chunks of this PNG file to the
	 * specified output stream. This does not close the stream.
	 * @throws NullPointerException if {@code out}
	 * or any of this object's fields is {@code null}
	 * @throws IllegalStateException if the current
	 * lists of chunks do not form a valid PNG file
	 * @throws IOException if an I/O exception occurs
	 */
	public void write(OutputStream out) throws IOException {
		Objects.requireNonNull(out);
		if (ihdr.isEmpty() || idats.isEmpty())
			throw new IllegalStateException("Missing some mandatory chunks");
		
		List<Chunk> chunks = new ArrayList<>();
		chunks.add(ihdr.get());
		chunks.addAll(beforeIdats);
		chunks.addAll(idats);
		chunks.addAll(afterIdats);
		chunks.add(Iend.SINGLETON);
		new XngFile(XngFile.Type.PNG, chunks).write(out);
	}
	
}
