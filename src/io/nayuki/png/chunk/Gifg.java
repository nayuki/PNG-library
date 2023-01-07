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
 * A GIF graphic control extension (gIFg) chunk. This provides
 * backward compatibility for GIF images. Instances are immutable.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.gIFg
 */
public record Gifg(
		int disposalMethod,
		boolean userInputFlag,
		int delayTime)
	implements Chunk {
	
	
	static final String TYPE = "gIFg";
	
	
	/*---- Constructor ----*/
	
	public Gifg {
		if (disposalMethod >>> 3 != 0)
			throw new IllegalArgumentException("Disposal method out of range");
	}
	
	
	public static Gifg read(DataInput in) throws IOException {
		Objects.requireNonNull(in);
		int disposalMethod = in.readUnsignedByte();
		int userInputFlag = in.readUnsignedByte();
		int delayTime = in.readUnsignedShort();
		if (userInputFlag >>> 1 != 0)
			throw new IllegalArgumentException("User input flag out of range");
		return new Gifg(disposalMethod, userInputFlag != 0, delayTime);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 4;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeByte(disposalMethod);
		out.writeByte(userInputFlag ? 1 : 0);
		out.writeShort(delayTime);
	}
	
}
