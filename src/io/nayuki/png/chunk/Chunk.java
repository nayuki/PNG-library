/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;


/**
 * A PNG/MNG/JNG chunk. Each chunk has a type (4 ASCII uppercase/lowercase
 * letters) and binary data (0 to 2<sup>31</sup>&minus;1 bytes). The CRC-32
 * field is excluded and handled automatically when reading/writing files.
 * Chunk types where the reserved bit is set (e.g. "ABcD") cannot be represented
 * because they can potentially have a different set of fields. Classes that
 * implement this interface can choose to have mutable or immutable instances.
 */
public interface Chunk {
	
	/**
	 * Reads from the specified input stream and returns a chunk object representing
	 * the data that is read, or empty if the end of stream is immediately
	 * encountered. If the chunk type corresponds to a known class in this package
	 * ({@link io.nayuki.png.chunk}), then this function returns a chunk of that type
	 * (e.g. reading type "IHDR" will return an object of the class {@link Ihdr} or
	 * throw an {@code IllegalArgumentException} if the data is invalid. Otherwise if
	 * the chunk type is not of a known type, then a {@link Custom} chunk is returned.
	 * @param in the input to read the chunk's data from (not {@code null})
	 * @return a chunk object representing the data parsed from the chunk reader,
	 * or empty if the end of stream is immediately encountered, not {@code null}
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if the chunk contains invalid data
	 * @throws IOException if an I/O exception occurs
	 */
	public static Optional<Chunk> read(InputStream in) throws IOException {
		Optional<ChunkReader> temp = ChunkReader.tryNew(in);
		if (temp.isEmpty())
			return Optional.empty();
		
		ChunkReader cin = temp.get();
		Chunk result = switch (cin.getType()) {
			case Actl.TYPE -> Actl.read(cin);
			case Bkgd.TYPE -> Bkgd.read(cin);
			case Chrm.TYPE -> Chrm.read(cin);
			case Dsig.TYPE -> Dsig.read(cin);
			case Exif.TYPE -> Exif.read(cin);
			case Fctl.TYPE -> Fctl.read(cin);
			case Fdat.TYPE -> Fdat.read(cin);
			case Gama.TYPE -> Gama.read(cin);
			case Gifg.TYPE -> Gifg.read(cin);
			case Gift.TYPE -> Gift.read(cin);
			case Gifx.TYPE -> Gifx.read(cin);
			case Hist.TYPE -> Hist.read(cin);
			case Iccp.TYPE -> Iccp.read(cin);
			case Idat.TYPE -> Idat.read(cin);
			case Iend.TYPE -> Iend.SINGLETON;
			case Ihdr.TYPE -> Ihdr.read(cin);
			case Itxt.TYPE -> Itxt.read(cin);
			case Offs.TYPE -> Offs.read(cin);
			case Pcal.TYPE -> Pcal.read(cin);
			case Phys.TYPE -> Phys.read(cin);
			case Plte.TYPE -> Plte.read(cin);
			case Sbit.TYPE -> Sbit.read(cin);
			case Scal.TYPE -> Scal.read(cin);
			case Splt.TYPE -> Splt.read(cin);
			case Srgb.TYPE -> Srgb.read(cin);
			case Ster.TYPE -> Ster.read(cin);
			case Text.TYPE -> Text.read(cin);
			case Time.TYPE -> Time.read(cin);
			case Trns.TYPE -> Trns.read(cin);
			case Ztxt.TYPE -> Ztxt.read(cin);
			default -> Custom.read(cin);
		};
		cin.finish();
		return Optional.of(result);
	}
	
	
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
	 * Writes this chunk's entire sequence of bytes (length,
	 * type, data, CRC-32) to the specified output stream.
	 * @param out the output stream to write to (not {@code null})
	 * @throws NullPointerException if {@code out} is {@code null}
	 * @throws IOException if an I/O exceptions occurs
	 */
	public abstract void writeChunk(OutputStream out) throws IOException;
	
	
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
			throw new IllegalArgumentException("Invalid type string length");
		for (int i = 0; i < type.length(); i++) {
			char c = type.charAt(i);
			if (!('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z'))
				throw new IllegalArgumentException("Invalid type string characters");
		}
		if (type.charAt(2) >= 'a')
			throw new IllegalArgumentException("Reserved chunk type");
		if (type.charAt(0) <= 'Z' && type.charAt(3) >= 'a')
			throw new IllegalArgumentException("Chunk type that is critical must be unsafe to copy");
	}
	
	
	
	/*---- Enumeration ----*/
	
	/**
	 * The list of defined compression methods. This is used in several chunk types.
	 */
	public enum CompressionMethod {
		
		/** The DEFLATE compressed format (specified in RFC 1951) wrapped in a ZLIB container (RFC 1950). */
		ZLIB_DEFLATE {
			public byte[] compress(byte[] data) {
				var bout = new ByteArrayOutputStream();
				try (var dout = new DeflaterOutputStream(bout)) {
					dout.write(data);
				} catch (IOException e) {
					throw new AssertionError("Unreachable exception", e);
				}
				return bout.toByteArray();
			}
			
			public byte[] decompress(byte[] data) {
				var bout = new ByteArrayOutputStream(data.length);
				try (var iout = new InflaterOutputStream(bout)) {
					iout.write(data);
				} catch (IOException e) {
					throw new IllegalArgumentException("Invalid compressed data", e);
				}
				return bout.toByteArray();
			}
		};
		
		
		public abstract byte[] compress(byte[] data);
		
		public abstract byte[] decompress(byte[] data);
		
	}
	
}
