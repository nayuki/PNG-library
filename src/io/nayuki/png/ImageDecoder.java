package io.nayuki.png;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.image.BufferedRgbaImage;


public class ImageDecoder {
	
	public static Object toImage(PngImage png) {
		Ihdr ihdr = png.ihdr.get();
		if (ihdr.compressionMethod() != Ihdr.CompressionMethod.DEFLATE)
			throw new IllegalArgumentException();
		if (ihdr.filterMethod() != Ihdr.FilterMethod.ADAPTIVE)
			throw new IllegalArgumentException();
		if (ihdr.interlaceMethod() != Ihdr.InterlaceMethod.NONE)
			throw new IllegalArgumentException();
		
		if (ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR || ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA)
			return toRgbaImage(png);
		else
			throw new UnsupportedOperationException();
	}
	
	
	private static BufferedRgbaImage toRgbaImage(PngImage png) {
		Ihdr ihdr = png.ihdr.get();
		int width = ihdr.width();
		int height = ihdr.height();
		int bitDepth = ihdr.bitDepth();
		boolean hasAlpha = ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA;
		
		var result = new BufferedRgbaImage(width, height, new int[]{bitDepth, bitDepth, bitDepth, hasAlpha ? bitDepth : 0});
		List<byte[]> idatDatas = png.idats.stream()
			.map(idat -> idat.data())
			.collect(Collectors.toList());
		try (var din = new DataInputStream(new InflaterInputStream(new ByteArraysInputStream(idatDatas)))) {
			
			int bytesPerPixel = bitDepth / 8 * (hasAlpha ? 4 : 3);
			var prevRow = new byte[Math.multiplyExact(width, bytesPerPixel)];
			var row = prevRow.clone();
			for (int y = 0; y < height; y++) {
				int filter = din.readUnsignedByte();
				din.readFully(row);
				for (int i = 0, j = -bytesPerPixel; i < row.length; i++, j++) {
					int a = j >= 0 ? row[j] & 0xFF : 0;  // Left
					int b = prevRow[i] & 0xFF;  // Up
					int c = j >= 0 ? prevRow[j] & 0xFF : 0;  // Up left
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
				
				if (bitDepth == 8) {
					if (!hasAlpha) {
						for (int x = 0, i = 0; x < width; x++, i += 3) {
							long val = (row[i + 0] & 0xFFL) << 48
							         | (row[i + 1] & 0xFFL) << 32
							         | (row[i + 2] & 0xFFL) << 16;
							result.setPixel(x, y, val);
						}
					} else {
						for (int x = 0, i = 0; x < width; x++, i += 4) {
							long val = (row[i + 0] & 0xFFL) << 48
							         | (row[i + 1] & 0xFFL) << 32
							         | (row[i + 2] & 0xFFL) << 16
							         | (row[i + 3] & 0xFFL) <<  0;
							result.setPixel(x, y, val);
						}
					}
				} else if (bitDepth == 16) {
					if (!hasAlpha) {
						for (int x = 0, i = 0; x < width; x++, i += 6) {
							long val = (row[i + 0] & 0xFFL) << 56
							         | (row[i + 1] & 0xFFL) << 48
							         | (row[i + 2] & 0xFFL) << 40
							         | (row[i + 3] & 0xFFL) << 32
							         | (row[i + 4] & 0xFFL) << 24
							         | (row[i + 5] & 0xFFL) << 16;
							result.setPixel(x, y, val);
						}
					} else {
						for (int x = 0, i = 0; x < width; x++, i += 8) {
							long val = (row[i + 0] & 0xFFL) << 56
							         | (row[i + 1] & 0xFFL) << 48
							         | (row[i + 2] & 0xFFL) << 40
							         | (row[i + 3] & 0xFFL) << 32
							         | (row[i + 4] & 0xFFL) << 24
							         | (row[i + 5] & 0xFFL) << 16
							         | (row[i + 6] & 0xFFL) <<  8
							         | (row[i + 7] & 0xFFL) <<  0;
							result.setPixel(x, y, val);
						}
					}
				} else
					throw new AssertionError();
				
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
