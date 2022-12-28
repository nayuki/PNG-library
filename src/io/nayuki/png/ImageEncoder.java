/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.image.RgbaImage;


public final class ImageEncoder {
	
	public static PngImage toPng(RgbaImage img) {
		int[] bitDepths = img.getBitDepths();
		int bitDepth = bitDepths[0];
		if (bitDepths[1] != bitDepth || bitDepths[2] != bitDepth || bitDepths[3] != 0 && bitDepths[3] != bitDepth)
			throw new IllegalArgumentException();
		boolean hasAlpha = bitDepths[3] != 0;

		int width = img.getWidth();
		int height = img.getHeight();
		var result = new PngImage();
		result.ihdr = Optional.of(new Ihdr(
			width, height, bitDepth,
			hasAlpha ? Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA : Ihdr.ColorType.TRUE_COLOR,
			Ihdr.CompressionMethod.DEFLATE,
			Ihdr.FilterMethod.ADAPTIVE,
			Ihdr.InterlaceMethod.NONE));
		
		int bytesPerRow = Math.addExact(1, Math.multiplyExact(width, bitDepth / 8 * (hasAlpha ? 4 : 3)));
		byte[] filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		for (int y = 0, i = 0; y < height; y++) {
			filtersAndSamples[i] = 0;
			i++;
			
			if (bitDepth == 8) {
				if (!hasAlpha) {
					for (int x = 0; x < width; x++) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 48);
						filtersAndSamples[i + 1] = (byte)(val >>> 32);
						filtersAndSamples[i + 2] = (byte)(val >>> 16);
						i += 3;
					}
				} else {
					for (int x = 0; x < width; x++) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 48);
						filtersAndSamples[i + 1] = (byte)(val >>> 32);
						filtersAndSamples[i + 2] = (byte)(val >>> 16);
						filtersAndSamples[i + 3] = (byte)(val >>>  0);
						i += 4;
					}
				}
			} else if (bitDepth == 16) {
				if (!hasAlpha) {
					for (int x = 0; x < width; x++) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 56);
						filtersAndSamples[i + 1] = (byte)(val >>> 48);
						filtersAndSamples[i + 2] = (byte)(val >>> 40);
						filtersAndSamples[i + 3] = (byte)(val >>> 32);
						filtersAndSamples[i + 4] = (byte)(val >>> 24);
						filtersAndSamples[i + 5] = (byte)(val >>> 16);
						i += 6;
					}
				} else {
					for (int x = 0; x < width; x++) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 56);
						filtersAndSamples[i + 1] = (byte)(val >>> 48);
						filtersAndSamples[i + 2] = (byte)(val >>> 40);
						filtersAndSamples[i + 3] = (byte)(val >>> 32);
						filtersAndSamples[i + 4] = (byte)(val >>> 24);
						filtersAndSamples[i + 5] = (byte)(val >>> 16);
						filtersAndSamples[i + 6] = (byte)(val >>>  8);
						filtersAndSamples[i + 7] = (byte)(val >>>  0);
						i += 8;
					}
				}
			} else
				throw new AssertionError();
		}
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(filtersAndSamples);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		result.idats.add(new Idat(bout.toByteArray()));
		
		return result;
	}
	
}
