/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;


/**
 * A palette (PLTE) chunk. This is required for indexed-color images,
 * but only behaves as a suggestion for true-color images. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11PLTE
 */
public record Plte(byte[] data) implements BytesDataChunk {
	
	static final String TYPE = "PLTE";
	
	
	/*---- Constructors ----*/
	
	public Plte {
		Objects.requireNonNull(data);
		if (data.length % 3 != 0)
			throw new IllegalArgumentException("Invalid data length");
		checkNumEntries(data.length / 3);
	}
	
	
	public Plte(int[] entries) {
		this(convert(entries));
	}
	
	
	private static byte[] convert(int[] entries) {
		Objects.requireNonNull(entries);
		checkNumEntries(entries.length);
		var result = new byte[Math.multiplyExact(entries.length, 3)];
		int i = 0;
		for (int entry : entries) {
			if (entry >>> 24 != 0)
				throw new IllegalArgumentException("Uint24 value out of range");
			for (int j = 16; j >= 0; j -= 8, i++)
				result[i] = (byte)(entry >>> j);
		}
		return result;
	}
	
	
	private static void checkNumEntries(int numEntries) {
		if (!(1 <= numEntries && numEntries <= 256))
			throw new IllegalArgumentException("Number of entries out of range");
	}
	
	
	/**
	 * Reads the specified number of bytes from the specified input stream,
	 * parses the fields, and returns a new chunk object of this type.
	 * @param dataLen the expected number of bytes of chunk data (non-negative)
	 * @param in the input stream to read from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if {@code dataLen} is negative
	 * or the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Plte read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		return new Plte(Util.readBytes(in, dataLen));
	}
	
	
	/*---- Method ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
}
