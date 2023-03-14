/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;


/**
 * A frame data (fdAT) chunk. This contains a sequence number and pixel data that is filtered and
 * compressed. Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://wiki.mozilla.org/APNG_Specification#.60fdAT.60:_The_Frame_Data_Chunk
 */
public record Fdat(
		int sequence,
		byte[] data)
	implements Chunk {
	
	
	static final String TYPE = "fdAT";
	
	
	/*---- Constructor and factory ----*/
	
	public Fdat {
		if (sequence < 0)
			throw new IllegalArgumentException("Invalid sequence number");
		Objects.requireNonNull(data);
		Util.checkedLengthSum(Integer.BYTES, data);
	}
	
	
	/**
	 * Reads from the specified chunk reader, parses the
	 * fields, and returns a new chunk object of this type.
	 * @param in the chunk reader to read the chunk's data from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Fdat read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		int sequence = in.readInt32();
		byte[] data = in.readRemainingBytes();
		return new Fdat(sequence, data);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(Integer.BYTES, data);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeInt32(sequence);
		cout.write(data);
		cout.finish();
	}
	
}
