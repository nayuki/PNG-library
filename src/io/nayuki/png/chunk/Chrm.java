package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import io.nayuki.png.Chunk;


public record Chrm(
		int whitePointX, int whitePointY,
		int redX, int redY,
		int greenX, int greenY,
		int blueX, int blueY)
	implements Chunk {
	
	
	/*---- Constructor ----*/
	
	public Chrm {
		if (whitePointX <= 0 || whitePointY <= 0)
			throw new IllegalArgumentException();
		if (redX <= 0 || redY <= 0)
			throw new IllegalArgumentException();
		if (greenX <= 0 || greenY <= 0)
			throw new IllegalArgumentException();
		if (blueX <= 0 || blueY <= 0)
			throw new IllegalArgumentException();
	}
	
	
	public Chrm(
			double whitePointX, double whitePointY,
			double redX, double redY,
			double greenX, double greenY,
			double blueX, double blueY) {
		this(
			convert(whitePointX), convert(whitePointY),
			convert(redX), convert(redY),
			convert(greenX), convert(greenY),
			convert(blueX), convert(blueY));
	}
	
	
	private static int convert(double val) {
		val *= 100_000;
		if (!(Double.isFinite(val) && 0 <= val && val <= Integer.MAX_VALUE))
			throw new IllegalArgumentException();
		long result = Math.round(val);
		if (!(0 <= result && result <= Integer.MAX_VALUE))
			throw new IllegalArgumentException();
		return (int)result;
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "cHRM";
	}
	
	
	@Override public int getDataLength() {
		return 32;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeInt(whitePointX);
		out.writeInt(whitePointY);
		out.writeInt(redX);
		out.writeInt(redY);
		out.writeInt(greenX);
		out.writeInt(greenY);
		out.writeInt(blueX);
		out.writeInt(blueY);
	}
	
}
