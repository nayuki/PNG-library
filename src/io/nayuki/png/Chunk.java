package io.nayuki.png;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;


public interface Chunk {
	
	public abstract String getType();
	
	
	public default int getDataLength() {
		return getData().length;
	}
	
	
	public default byte[] getData() {
		try {
			var out = new ByteArrayOutputStream();
			writeData(new DataOutputStream(out));
			return out.toByteArray();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	
	public abstract void writeData(DataOutput out) throws IOException;
	
}
