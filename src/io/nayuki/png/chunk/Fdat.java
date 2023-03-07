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
import java.io.OutputStream;
import java.util.Objects;
import io.nayuki.png.Chunk;


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
	public static Fdat read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		int sequence = in.readInt();
		byte[] data = Util.readBytes(in, dataLen - Integer.BYTES);
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
