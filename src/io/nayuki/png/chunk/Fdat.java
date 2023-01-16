/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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
	
	
	/*---- Constructor ----*/
	
	public Fdat {
		if (sequence <= 0)
			throw new IllegalArgumentException("Invalid sequence number");
		Objects.requireNonNull(data);
		Util.checkedLengthSum(data, Integer.BYTES);
	}
	
	
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
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(data, Integer.BYTES);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(sequence);
		out.write(data);
	}
	
}