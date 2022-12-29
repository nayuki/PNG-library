/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import io.nayuki.png.Chunk;


/**
 * A primary chromaticities and white point (cHRM) chunk. This specifies
 * the 1931 CIE <var>x</var> and <var>y</var> coordinates of the
 * RGB primaries used in the image and the reference white point.
 * Instances are immutable.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11cHRM
 */
public record Chrm(
		int whitePointX, int whitePointY,
		int redX, int redY,
		int greenX, int greenY,
		int blueX, int blueY)
	implements Chunk {
	
	
	static final String TYPE = "cHRM";
	
	
	/*---- Constructors ----*/
	
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
	
	
	public static Chrm read(DataInput in) throws IOException {
		int whitePointX = in.readInt();
		int whitePointY = in.readInt();
		int redX = in.readInt();
		int redY = in.readInt();
		int greenX = in.readInt();
		int greenY = in.readInt();
		int blueX = in.readInt();
		int blueY = in.readInt();
		return new Chrm(
			whitePointX, whitePointY,
			redX, redY,
			greenX, greenY,
			blueX, blueY);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
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
