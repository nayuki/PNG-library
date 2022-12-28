package io.nayuki.png;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.image.Argb8Image;
import io.nayuki.png.image.BufferedArgb8Image;


public class ImageDecoder {
	
	public static Object toImage(PngImage png) {
		Ihdr ihdr = png.ihdr.get();
		if (ihdr.compressionMethod() != Ihdr.CompressionMethod.DEFLATE)
			throw new IllegalArgumentException();
		if (ihdr.filterMethod() != Ihdr.FilterMethod.ADAPTIVE)
			throw new IllegalArgumentException();
		if (ihdr.interlaceMethod() != Ihdr.InterlaceMethod.NONE)
			throw new IllegalArgumentException();
		
		if (ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR || ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA) {
			return switch (ihdr.bitDepth()) {
				case 8 -> toArgb8Image(png);
				default -> throw new UnsupportedOperationException();
			};
		} else
			throw new UnsupportedOperationException();
	}
	
	
	private static Argb8Image toArgb8Image(PngImage png) {
		Ihdr ihdr = png.ihdr.get();
		int width = ihdr.width();
		int height = ihdr.height();
		boolean hasAlpha = ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA;
		
		var result = new BufferedArgb8Image(width, height);
		List<byte[]> idatDatas = png.idats.stream()
			.map(idat -> idat.data())
			.collect(Collectors.toList());
		try (var din = new DataInputStream(new InflaterInputStream(new ByteArraysInputStream(idatDatas)))) {
			
			int bytesPerPixel = hasAlpha ? 4 : 3;
			var prevRow = new byte[Math.addExact(1, Math.multiplyExact(width, bytesPerPixel))];
			var row = prevRow.clone();
			for (int y = 0; y < height; y++) {
				din.readFully(row);
				byte filter = row[0];
				for (int i = 1, j = 1 - bytesPerPixel; i < row.length; i++, j++) {
					int a = j >= 1 ? row[j] & 0xFF : 0;  // Left
					int b = prevRow[i] & 0xFF;  // Up
					int c = j >= 1 ? prevRow[j] & 0xFF : 0;  // Up left
					row[i] += switch (filter) {  // Prediction by filter type
						case 0 -> 0;
						case 1 -> a;
						case 2 -> b;
						case 3 -> (a + b) >>> 1;
						case 4 -> {
							int p = a + b - c;
							int pa = Math.abs(p - a);
							int pb = Math.abs(p - b);
							int pc = Math.abs(p - c);
							if (pa <= pb && pa <= pc) yield a;
							else if (pb <= pc) yield b;
							else yield c;
						}
						default -> throw new IllegalArgumentException();
					};
				}
				
				if (hasAlpha) {
					for (int x = 0, i = 1; x < width; x++) {
						int val = (row[i + 0] & 0xFF) << 16
						        | (row[i + 1] & 0xFF) <<  8
						        | (row[i + 2] & 0xFF) <<  0
						        | (row[i + 3] & 0xFF) << 24;
						result.setPixel(x, y, val);
						i += 4;
					}
				} else {
					for (int x = 0, i = 1; x < width; x++) {
						int val = (row[i + 0] & 0xFF) << 16
						        | (row[i + 1] & 0xFF) <<  8
						        | (row[i + 2] & 0xFF) <<  0
						        | 0xFF << 24;
						result.setPixel(x, y, val);
						i += 3;
					}
				}
				
				byte[] temp = row;
				row = prevRow;
				prevRow = temp;
			}
			
			if (din.read() != -1)
				throw new IllegalArgumentException();
			
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}
	
}
