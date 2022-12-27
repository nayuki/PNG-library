package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Phys(
		int pixelsPerUnitX,
		int pixelsPerUnitY,
		UnitSpecifier unitSpecifier)
	implements Chunk {
	
	
	/*---- Constructor ----*/
	
	public Phys {
		if (pixelsPerUnitX <= 0 || pixelsPerUnitY <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(unitSpecifier);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "pHYs";
	}
	
	
	@Override public int getDataLength() {
		return 9;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(pixelsPerUnitX);
		out.writeInt(pixelsPerUnitY);
		out.writeByte(unitSpecifier.ordinal());
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum UnitSpecifier {
		UNKNOWN,
		METRE,
	}
	
}
