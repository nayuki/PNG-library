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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import io.nayuki.png.Chunk;


/**
 * A suggested palette (sPLT) chunk. This contains a palette name,
 * sample depth, and color samples with alpha and frequency. Instances
 * should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11sPLT
 */
public record Splt(
		String paletteName,
		int sampleDepth,
		byte[] data)
	implements Chunk {
	
	
	static final String TYPE = "sPLT";
	
	
	/*---- Constructor ----*/
	
	public Splt {
		Util.checkKeyword(paletteName, true);
		
		int bytesPerEntry = switch (sampleDepth) {
			case 8 -> 6;
			case 16 -> 10;
			default -> throw new IllegalArgumentException("Invalid sample depth");
		};
		if (data.length % bytesPerEntry != 0)
			throw new IllegalArgumentException("Invalid data length");
		
		Util.checkedLengthSum(paletteName, data, 2);
	}
	
	
	public static Splt read(int dataLen, DataInput in) throws IOException {
		byte[][] parts = Util.readAndSplitByNull(dataLen, in, 2);
		if (parts[1].length < 1)
			throw new IllegalArgumentException("Missing sample depth");
		return new Splt(
			new String(parts[0], StandardCharsets.ISO_8859_1),
			parts[1][0],
			Arrays.copyOfRange(parts[1], 1, parts[1].length));
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return Util.checkedLengthSum(paletteName, data, 2);
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(paletteName.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(sampleDepth);
		out.write(data);
	}
	
}
