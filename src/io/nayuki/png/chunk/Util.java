/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.IOException;
import io.nayuki.png.Chunk;


/**
 * Utility functions for chunks. Not instantiable.
 */
public final class Util {
	
	public static Chunk readChunk(String type, int dataLen, DataInput in) throws IOException {
		return switch (type) {
			case Bkgd.TYPE -> Bkgd.read(dataLen, in);
			case Chrm.TYPE -> Chrm.read(         in);
			case Gama.TYPE -> Gama.read(         in);
			case Hist.TYPE -> Hist.read(dataLen, in);
			case Iccp.TYPE -> Iccp.read(dataLen, in);
			case Idat.TYPE -> Idat.read(dataLen, in);
			case Iend.TYPE -> Iend.SINGLETON;
			case Ihdr.TYPE -> Ihdr.read(         in);
			case Phys.TYPE -> Phys.read(         in);
			case Plte.TYPE -> Plte.read(dataLen, in);
			case Sbit.TYPE -> Sbit.read(dataLen, in);
			case Srgb.TYPE -> Srgb.read(         in);
			case Text.TYPE -> Text.read(dataLen, in);
			case Time.TYPE -> Time.read(         in);
			case Trns.TYPE -> Trns.read(dataLen, in);
			default -> Custom.read(type, dataLen, in);
		};
	}
	
	
	static <E> E indexInto(E[] array, int index) {
		if (0 <= index && index < array.length)
			return array[index];
		else
			return null;
	}
	
	
	private Util() {}
	
}
