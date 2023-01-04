/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.image.BufferedRgbaImage;


/**
 * Decodes a {@link PngImage} object to a buffered image
 * where pixels can be directly read. Not instantiable.
 */
public final class ImageDecoder {
	
	public static Object toImage(PngImage png) {
		Ihdr ihdr = png.ihdr.get();
		if (ihdr.compressionMethod() != Ihdr.CompressionMethod.DEFLATE)
			throw new IllegalArgumentException();
		if (ihdr.filterMethod() != Ihdr.FilterMethod.ADAPTIVE)
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
		List<InputStream> ins = png.idats.stream()
			.map(idat -> new ByteArrayInputStream(idat.data()))
			.collect(Collectors.toList());
		try (var din = new DataInputStream(new InflaterInputStream(
				new SequenceInputStream(Collections.enumeration(ins))))) {
			
			int xOffset = 0, yOffset = 0;
			int xStep = switch (ihdr.interlaceMethod()) {
				case NONE  -> 1;
				case ADAM7 -> 8;
			};
			int yStep = xStep;
			do {
				decodeSubimage(din, xOffset, yOffset, xStep, yStep,
					Math.ceilDiv(width - xOffset, xStep), Math.ceilDiv(height - yOffset, yStep),
					bitDepth, hasAlpha, result);
				if (xStep == yStep) {
					if (xOffset == 0)  // True only in the foremost iteration
						xOffset = xStep / 2;
					else {
						yOffset = xOffset;
						xOffset = 0;
						xStep /= 2;
					}
				} else {
					xOffset = yOffset / 2;
					yOffset = 0;
					yStep /= 2;
				}
			} while (yStep > 1);
			
			if (din.read() != -1)
				throw new IllegalArgumentException();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return result;
	}
	
	
	private static void decodeSubimage(DataInput din, int xOffset, int yOffset, int xStep, int yStep, int width, int height, int bitDepth, boolean hasAlpha, BufferedRgbaImage result) throws IOException {
		int bytesPerPixel = bitDepth / 8 * (hasAlpha ? 4 : 3);
		var prevRow = new byte[Math.multiplyExact(Math.addExact(1, width), bytesPerPixel)];
		var row = prevRow.clone();
		for (int y = 0; y < height; y++) {
			int filter = din.readUnsignedByte();
			din.readFully(row, bytesPerPixel, row.length - bytesPerPixel);
			switch (filter) {
				case 0:  // None
					break;
				case 1:  // Sub
					for (int i = bytesPerPixel; i < row.length; i++)
						row[i] += row[i - bytesPerPixel];
					break;
				case 2:  // Up
					for (int i = bytesPerPixel; i < row.length; i++)
						row[i] += prevRow[i];
					break;
				case 3:  // Average
					for (int i = bytesPerPixel; i < row.length; i++)
						row[i] += ((row[i - bytesPerPixel] & 0xFF) + (prevRow[i] & 0xFF)) >>> 1;
					break;
				case 4:  // Paeth
					for (int i = bytesPerPixel; i < row.length; i++) {
						int a = row[i - bytesPerPixel] & 0xFF;  // Left
						int b = prevRow[i] & 0xFF;  // Up
						int c = prevRow[i - bytesPerPixel] & 0xFF;  // Up left
						int p = a + b - c;
						int pa = Math.abs(p - a);
						int pb = Math.abs(p - b);
						int pc = Math.abs(p - c);
						int pr;
						if (pa <= pb && pa <= pc) pr = a;
						else if (pb <= pc) pr = b;
						else pr = c;
						row[i] += pr;
					}
					break;
				default:
					throw new IllegalArgumentException();
			}
			
			if (bitDepth == 8) {
				if (!hasAlpha) {
					for (int x = 0, i = bytesPerPixel; x < width; x++, i += 3) {
						long val = (row[i + 0] & 0xFFL) << 48
						         | (row[i + 1] & 0xFFL) << 32
						         | (row[i + 2] & 0xFFL) << 16;
						result.setPixel(xOffset + x * xStep, yOffset + y * yStep, val);
					}
				} else {
					for (int x = 0, i = bytesPerPixel; x < width; x++, i += 4) {
						long val = (row[i + 0] & 0xFFL) << 48
						         | (row[i + 1] & 0xFFL) << 32
						         | (row[i + 2] & 0xFFL) << 16
						         | (row[i + 3] & 0xFFL) <<  0;
						result.setPixel(xOffset + x * xStep, yOffset + y * yStep, val);
					}
				}
			} else if (bitDepth == 16) {
				if (!hasAlpha) {
					for (int x = 0, i = bytesPerPixel; x < width; x++, i += 6) {
						long val = (row[i + 0] & 0xFFL) << 56
						         | (row[i + 1] & 0xFFL) << 48
						         | (row[i + 2] & 0xFFL) << 40
						         | (row[i + 3] & 0xFFL) << 32
						         | (row[i + 4] & 0xFFL) << 24
						         | (row[i + 5] & 0xFFL) << 16;
						result.setPixel(xOffset + x * xStep, yOffset + y * yStep, val);
					}
				} else {
					for (int x = 0, i = bytesPerPixel; x < width; x++, i += 8) {
						long val = (row[i + 0] & 0xFFL) << 56
						         | (row[i + 1] & 0xFFL) << 48
						         | (row[i + 2] & 0xFFL) << 40
						         | (row[i + 3] & 0xFFL) << 32
						         | (row[i + 4] & 0xFFL) << 24
						         | (row[i + 5] & 0xFFL) << 16
						         | (row[i + 6] & 0xFFL) <<  8
						         | (row[i + 7] & 0xFFL) <<  0;
						result.setPixel(xOffset + x * xStep, yOffset + y * yStep, val);
					}
				}
			} else
				throw new AssertionError();
			
			byte[] temp = row;
			row = prevRow;
			prevRow = temp;
		}
	}
	
	
	private ImageDecoder() {}
	
}
