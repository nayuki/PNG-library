/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.InflaterOutputStream;


/**
 * A PNG/MNG/JNG chunk. Each chunk has a type (4 ASCII uppercase/lowercase
 * letters) and binary data (0 to 2<sup>31</sup>&minus;1 bytes).
 * The CRC-32 field is excluded and handled automatically when reading/writing files.
 * Classes that implement this interface can choose to have mutable or immutable instances.
 */
public interface Chunk {
	
	/**
	 * Returns the type of this chunk, a length-4 ASCII uppercase/lowercase string.
	 * @return the type of this chunk (not {@code null})
	 */
	public abstract String getType();
	
	
	/**
	 * Tests whether this chunk is critical. Generally speaking, when a
	 * decoder encounters an unrecognized critical chunk, it should stop
	 * processing the file and return an error. Non-critical chunks are
	 * also known as ancillary. Critical chunks must be unsafe to copy.
	 * @return whether this chunk is critical
	 */
	public default boolean isCritical() {
		return (getType().charAt(0) & 0x20) == 0;
	}
	
	/**
	 * Tests whether this chunk is public. Each public chunk type must be defined
	 * in the PNG standard or in a list maintained by the registration authority.
	 * @return whether this chunk is public
	 */
	public default boolean isPublic() {
		return (getType().charAt(1) & 0x20) == 0;
	}
	
	/**
	 * Tests whether this chunk is safe to copy. Generally speaking, an
	 * application that reads a PNG file, performs some editing, and writes a PNG
	 * file should handle unrecognized chunks by copying safe-to-copy ones and
	 * omitting unsafe-to-copy ones. Safe-to-copy chunks must be non-critical
	 * (ancillary). The detailed exact rules are found in the PNG standard.
	 * @return whether this chunk is safe to copy
	 */
	public default boolean isSafeToCopy() {
		return (getType().charAt(3) & 0x20) != 0;
	}
	
	
	/**
	 * Returns the byte length of this chunk's data field, which excludes
	 * the type and CRC-32. This can be any non-negative number.
	 * The default implementation relies on {@code getData()}.
	 * @return the number of data bytes, in the range [0, 2<sup>31</sup>&minus;1)
	 */
	public default int getDataLength() {
		return getData().length;
	}
	
	
	/**
	 * Returns a byte array representing this chunk's data field, which excludes
	 * the type and CRC-32. The default implementation relies on {@code writeData()}.
	 * @return the data bytes (not {@code null})
	 */
	public default byte[] getData() {
		try {
			var out = new ByteArrayOutputStream();
			writeData(new DataOutputStream(out));
			return out.toByteArray();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	
	/**
	 * Writes this chunk's data field (excluding type
	 * and CRC-32) to the specified output stream.
	 * @param out the output stream to write to
	 * @throws NullPointerException if {@code out} is {@code null}
	 * @throws IOException if an I/O exceptions occurs
	 */
	public abstract void writeData(DataOutput out) throws IOException;
	
	
	/**
	 * Throws an exception if the specified chunk type string is invalid.
	 * A type is valid iff all these conditions are met:
	 * <ul>
	 *   <li>The string has length 4</li>
	 *   <li>All characters are ASCII uppercase or lowercase</li>
	 *   <li>Character 2 (0-based) is uppercase</li>
	 *   <li>If character 0 (0-based) is uppercase, then character 3 is uppercase</li>
	 * </ul>
	 * @param type the chunk type string to check
	 * @throws NullPointerException if the string is {@code null}
	 * @throws IllegalArgumentException if the string does not satisfy all requirements
	 */
	public static void checkType(String type) {
		Objects.requireNonNull(type);
		if (type.length() != 4)
			throw new IllegalArgumentException();
		for (int i = 0; i < type.length(); i++) {
			char c = type.charAt(i);
			if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z'))
				throw new IllegalArgumentException();
		}
		if (type.charAt(2) >= 'a')
			throw new IllegalArgumentException();
		if (type.charAt(0) <= 'Z' && type.charAt(3) >= 'a')
			throw new IllegalArgumentException();
	}
	
	
	
	/*---- Enumeration ----*/
	
	/**
	 * The list of defined compression methods. This is used in several chunk types.
	 */
	public enum CompressionMethod {
		/** The DEFLATE compressed format (specified in RFC 1951) wrapped in a ZLIB container (RFC 1950). */
		ZLIB_DEFLATE(data -> {
			var bout = new ByteArrayOutputStream();
			try (var iout = new InflaterOutputStream(bout)) {
				iout.write(data);
			}
			return bout.toByteArray();
		});
		
		private Function decompressor;
		
		private CompressionMethod(Function decomp) {
			decompressor = decomp;
		}
		
		public byte[] decompress(byte[] data) throws IOException {
			return decompressor.apply(data);
		}
		
		
		private interface Function {
			public byte[] apply(byte[] data) throws IOException;
		}
	}
	
}
