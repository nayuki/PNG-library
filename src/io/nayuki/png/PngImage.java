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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import io.nayuki.png.chunk.Chunk;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Iend;
import io.nayuki.png.chunk.Ihdr;


/**
 * A structured representation of chunks that form a PNG file. Instances
 * are mutable. There is some degree of validation and exclusion of invalid
 * data. This class operates at the next level up from {@link XngFile}.
 * <p>When serializing a {@code PngImage} object, the resulting
 * list of chunks is composed in the following order:</p>
 * <ol>
 *   <li>Field {@code ihdr} (must be present)</li>
 *   <li>Field {@code afterIhdr} (zero or more chunks)</li>
 *   <li>Field {@code idats} (one or more chunks)</li>
 *   <li>Field {@code afterIdats} (zero or more chunks)</li>
 *   <li>Constant {@code Iend.SINGLETON}</li>
 * </ol>
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
	
	
	/**
	 * From the specified lists, returns the single chunk that matches the specified type or empty.
	 * @param <T> the chunk type
	 * @param type the class object of the desired chunk type
	 * @param lists the list of lists of chunks to read from
	 * @return the single chunk matching the type or empty
	 * @throws IllegalArgumentException if multiple chunks match the type
	 */
	@SafeVarargs
	public static <T> Optional<T> getChunk(Class<T> type, List<? extends Chunk>... lists) {
		Optional<T> result = Optional.empty();
		for (List<? extends Chunk> lst : lists) {
			for (Chunk chk : lst) {
				if (type.isInstance(chk)) {
					if (result.isPresent())
						throw new IllegalArgumentException("Multiple chunks with given type");
					result = Optional.of(type.cast(chk));
				}
			}
		}
		return result;
	}
	
	
	/**
	 * From the specified lists, returns a readable list of all the
	 * chunks that match the specified type, possibly an empty list.
	 * @param <T> the chunk type
	 * @param type the class object of the desired chunk type
	 * @param lists the list of lists of chunks to read from
	 * @return a list (not {@code null}) of all the chunks matching the type
	 */
	@SafeVarargs
	public static <T> List<T> getChunks(Class<T> type, List<? extends Chunk>... lists) {
		List<T> result = new ArrayList<>();
		for (List<? extends Chunk> lst : lists) {
			for (Chunk chk : lst) {
				if (type.isInstance(chk))
					result.add(type.cast(chk));
			}
		}
		return result;
	}
	
	
	/** The single IHDR chunk, if present. */
	public Optional<Ihdr> ihdr = Optional.empty();
	
	/** The chunks positioned after IHDR. */
	public List<Chunk> afterIhdr = new ArrayList<>();
	
	/** The consecutive IDAT chunks. */
	public List<Idat> idats = new ArrayList<>();
	
	/** The chunks positioned after the IDAT chunks. */
	public List<Chunk> afterIdats = new ArrayList<>();
	
	
	/**
	 * Constructs a blank PNG image where all fields are initially empty (not {@code null}).
	 */
	public PngImage() {}
	
	
	private PngImage(List<Chunk> chunks) {
		boolean hasIend = false;
		Set<String> seenChunkTypes = new HashSet<>();
		for (Chunk chunk : chunks) {
			String type = chunk.getType();
			if (hasIend)
				throw new IllegalArgumentException("Unexpected chunk after IEND");
			else if (!seenChunkTypes.add(type) && UNIQUE_CHUNK_TYPES.contains(type))
				throw new IllegalArgumentException("Duplicate " + type + " chunk");
			for (String t : NOT_BEFORE_CHUNK_TYPES.getOrDefault(type, Set.of())) {
				if (seenChunkTypes.contains(t))
					throw new IllegalArgumentException("Unexpected " + t + " chunk before " + type);
			}
			if (ihdr.isEmpty()) {
				if (!(chunk instanceof Ihdr chk))
					throw new IllegalArgumentException("Expected IHDR chunk");
				ihdr = Optional.of(chk);
			} else if (chunk instanceof Idat chk) {
				if (!afterIdats.isEmpty())
					throw new IllegalArgumentException("Non-consecutive IDAT chunk");
				idats.add(chk);
			} else if (chunk instanceof Iend) {
				if (idats.isEmpty())
					throw new IllegalArgumentException("Unexpected IEND chunk");
				hasIend = true;
			} else
				(idats.isEmpty() ? afterIhdr : afterIdats).add(chunk);
		}
		if (ihdr.isEmpty() || idats.isEmpty() || !hasIend)
			throw new IllegalArgumentException("Missing some required chunks");
	}
	
	
	private static final Set<String> UNIQUE_CHUNK_TYPES = Set.of(
		"acTL",
		"bKGD",
		"cHRM",
		"cICP",
		"cLLI",
		"eXIf",
		"gAMA",
		"hIST",
		"iCCP",
		"IEND",
		"IHDR",
		"mDCV",
		"oFFs",
		"pCAL",
		"pHYs",
		"PLTE",
		"sBIT",
		"sCAL",
		"sRGB",
		"sTER",
		"tIME",
		"tRNS");
	
	
	// Each entry (k, vs) means each v must not precede k
	private static final Map<String,Set<String>> NOT_BEFORE_CHUNK_TYPES = new HashMap<>();
	
	static {
		// Each entry {x, y} means that if both x and y exist in the chunk list, then x must precede y
		String[][] CHUNK_ORDERING_CONSTRAINTS = {
			{"cHRM", "PLTE"},
			{"gAMA", "PLTE"},
			{"iCCP", "PLTE"},
			{"sBIT", "PLTE"},
			{"sRGB", "PLTE"},
			
			{"PLTE", "bKGD"},
			{"PLTE", "hIST"},
			{"PLTE", "tRNS"},
			
			{"acTL", "IDAT"},
			{"bKGD", "IDAT"},
			{"cHRM", "IDAT"},
			{"cICP", "IDAT"},
			{"cLLI", "IDAT"},
			{"eXIf", "IDAT"},
			{"gAMA", "IDAT"},
			{"hIST", "IDAT"},
			{"iCCP", "IDAT"},
			{"mDCV", "IDAT"},
			{"oFFs", "IDAT"},
			{"pCAL", "IDAT"},
			{"pHYs", "IDAT"},
			{"PLTE", "IDAT"},
			{"sBIT", "IDAT"},
			{"sCAL", "IDAT"},
			{"sPTL", "IDAT"},
			{"sRGB", "IDAT"},
			{"sTER", "IDAT"},
			{"tRNS", "IDAT"},
			
			{"IDAT", "fdAT"},
		};
		for (String[] entry : CHUNK_ORDERING_CONSTRAINTS)
			NOT_BEFORE_CHUNK_TYPES.computeIfAbsent(entry[0], k -> new HashSet<>()).add(entry[1]);
	}
	
	
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
		List<Chunk> chunks = new ArrayList<>();
		chunks.add(ihdr.orElseThrow(() -> new IllegalStateException("Missing IHDR chunk")));
		chunks.addAll(afterIhdr);
		if (idats.isEmpty())
			throw new IllegalStateException("Missing IDAT chunks");
		chunks.addAll(idats);
		chunks.addAll(afterIdats);
		chunks.add(Iend.SINGLETON);
		new XngFile(XngFile.Type.PNG, chunks).write(out);
	}
	
}
