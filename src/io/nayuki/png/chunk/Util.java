/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.zip.InflaterOutputStream;
import io.nayuki.png.Chunk;


/**
 * Utility functions for chunks. Not instantiable.
 */
public final class Util {
	
	public static Chunk readChunk(String type, int dataLen, DataInput in) throws IOException {
		return switch (type) {
			case Bkgd.TYPE -> Bkgd.read(dataLen, in);
			case Chrm.TYPE -> Chrm.read(         in);
			case Dsig.TYPE -> Dsig.read(dataLen, in);
			case Exif.TYPE -> Exif.read(dataLen, in);
			case Gama.TYPE -> Gama.read(         in);
			case Gifg.TYPE -> Gifg.read(         in);
			case Gifx.TYPE -> Gifx.read(dataLen, in);
			case Hist.TYPE -> Hist.read(dataLen, in);
			case Iccp.TYPE -> Iccp.read(dataLen, in);
			case Idat.TYPE -> Idat.read(dataLen, in);
			case Iend.TYPE -> Iend.SINGLETON;
			case Ihdr.TYPE -> Ihdr.read(         in);
			case Itxt.TYPE -> Itxt.read(dataLen, in);
			case Offs.TYPE -> Offs.read(         in);
			case Phys.TYPE -> Phys.read(         in);
			case Plte.TYPE -> Plte.read(dataLen, in);
			case Sbit.TYPE -> Sbit.read(dataLen, in);
			case Splt.TYPE -> Splt.read(dataLen, in);
			case Srgb.TYPE -> Srgb.read(         in);
			case Ster.TYPE -> Ster.read(         in);
			case Text.TYPE -> Text.read(dataLen, in);
			case Time.TYPE -> Time.read(         in);
			case Trns.TYPE -> Trns.read(dataLen, in);
			case Ztxt.TYPE -> Ztxt.read(dataLen, in);
			default -> Custom.read(type, dataLen, in);
		};
	}
	
	
	public static byte[] decompressZlibDeflate(byte[] data) throws IOException {
		var bout = new ByteArrayOutputStream();
		try (var iout = new InflaterOutputStream(bout)) {
			iout.write(data);
		}
		return bout.toByteArray();
	}
	
	
	static <E> E indexInto(E[] array, int index) {
		if (0 <= index && index < array.length)
			return array[index];
		else
			return null;
	}
	
	
	private Util() {}
	
}
