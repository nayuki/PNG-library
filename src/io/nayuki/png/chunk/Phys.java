package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Phys(
		int pixelsPerUnitX,
		int pixelsPerUnitY,
		UnitSpecifier unitSpecifier)
	implements Chunk {
	
	
	static final String TYPE = "pHYs";
	
	
	/*---- Constructor ----*/
	
	public Phys {
		if (pixelsPerUnitX <= 0 || pixelsPerUnitY <= 0)
			throw new IllegalArgumentException();
		Objects.requireNonNull(unitSpecifier);
	}
	
	
	public static Phys read(DataInput in) throws IOException {
		int pixelsPerUnitX = in.readInt();
		int pixelsPerUnitY = in.readInt();
		UnitSpecifier unitSpecifier = Util.indexInto(UnitSpecifier.values(), in.readUnsignedByte());
		return new Phys(pixelsPerUnitX, pixelsPerUnitY, unitSpecifier);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
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
