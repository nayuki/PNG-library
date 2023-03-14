/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


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
	
	
	/*---- Constructor and factory ----*/
	
	public Splt {
		Util.checkKeyword(paletteName, true);
		
		int bytesPerEntry = switch (sampleDepth) {
			case 8 -> 6;
			case 16 -> 10;
			default -> throw new IllegalArgumentException("Invalid sample depth");
		};
		if (data.length % bytesPerEntry != 0)
			throw new IllegalArgumentException("Invalid data length");
		
		Util.checkedLengthSum(paletteName, 2 * Byte.BYTES, data);
	}
	
	
	static Splt read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String paletteName = in.readString(StandardCharsets.ISO_8859_1, true);
		int sampleDepth = in.readUint8();
		byte[] data = in.readRemainingBytes();
		return new Splt(paletteName, sampleDepth, data);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		int dataLen = Util.checkedLengthSum(paletteName, 2 * Byte.BYTES, data);
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeString(paletteName, StandardCharsets.ISO_8859_1, true);
		cout.writeUint8(sampleDepth);
		cout.write(data);
		cout.finish();
	}
	
}
