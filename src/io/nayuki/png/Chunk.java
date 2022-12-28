package io.nayuki.png;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;


public interface Chunk {
	
	public abstract String getType();
	
	
	public default boolean isCritical() {
		return (getType().charAt(0) & 0x20) == 0;
	}
	
	public default boolean isPublic() {
		return (getType().charAt(1) & 0x20) == 0;
	}
	
	public default boolean isSafeToCopy() {
		return (getType().charAt(3) & 0x20) == 1;
	}
	
	
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
