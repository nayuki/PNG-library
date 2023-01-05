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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.zip.DeflaterOutputStream;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.chunk.Sbit;
import io.nayuki.png.image.RgbaImage;


/**
 * Encodes an image (where pixels can be read) to a {@link PngImage} object. Not instantiable.
 */
public final class ImageEncoder {
	
	public static PngImage toPng(RgbaImage img) {
		Objects.requireNonNull(img);
		int[] bitDepths = img.getBitDepths();
		int bitDepth = bitDepths[0];
		boolean hasAlpha = bitDepths[3] != 0;
		if (bitDepths[1] != bitDepth || bitDepths[2] != bitDepth || bitDepths[3] != 0 && bitDepths[3] != bitDepth) {
			PngImage result = toPng(new UpBitDepthRgbaImage(img));
			byte[] bitDepthsBytes;
			if (!hasAlpha)
				bitDepthsBytes = new byte[]{(byte)bitDepths[0], (byte)bitDepths[1], (byte)bitDepths[2]};
			else
				bitDepthsBytes = new byte[]{(byte)bitDepths[0], (byte)bitDepths[1], (byte)bitDepths[2], (byte)bitDepths[3]};
			result.beforeIdats.add(new Sbit(bitDepthsBytes));
			return result;
		}

		int width = img.getWidth();
		int height = img.getHeight();
		var result = new PngImage();
		result.ihdr = Optional.of(new Ihdr(
			width, height, bitDepth,
			hasAlpha ? Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA : Ihdr.ColorType.TRUE_COLOR,
			Ihdr.CompressionMethod.ZLIB_DEFLATE,
			Ihdr.FilterMethod.ADAPTIVE,
			Ihdr.InterlaceMethod.NONE));
		
		int bytesPerRow = Math.addExact(1, Math.multiplyExact(width, bitDepth / 8 * (hasAlpha ? 4 : 3)));
		byte[] filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		for (int y = 0, i = 0; y < height; y++) {
			filtersAndSamples[i] = 0;
			i++;
			
			if (bitDepth == 8) {
				if (!hasAlpha) {
					for (int x = 0; x < width; x++, i += 3) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 48);
						filtersAndSamples[i + 1] = (byte)(val >>> 32);
						filtersAndSamples[i + 2] = (byte)(val >>> 16);
					}
				} else {
					for (int x = 0; x < width; x++, i += 4) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 48);
						filtersAndSamples[i + 1] = (byte)(val >>> 32);
						filtersAndSamples[i + 2] = (byte)(val >>> 16);
						filtersAndSamples[i + 3] = (byte)(val >>>  0);
					}
				}
			} else if (bitDepth == 16) {
				if (!hasAlpha) {
					for (int x = 0; x < width; x++, i += 6) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 56);
						filtersAndSamples[i + 1] = (byte)(val >>> 48);
						filtersAndSamples[i + 2] = (byte)(val >>> 40);
						filtersAndSamples[i + 3] = (byte)(val >>> 32);
						filtersAndSamples[i + 4] = (byte)(val >>> 24);
						filtersAndSamples[i + 5] = (byte)(val >>> 16);
					}
				} else {
					for (int x = 0; x < width; x++, i += 8) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 56);
						filtersAndSamples[i + 1] = (byte)(val >>> 48);
						filtersAndSamples[i + 2] = (byte)(val >>> 40);
						filtersAndSamples[i + 3] = (byte)(val >>> 32);
						filtersAndSamples[i + 4] = (byte)(val >>> 24);
						filtersAndSamples[i + 5] = (byte)(val >>> 16);
						filtersAndSamples[i + 6] = (byte)(val >>>  8);
						filtersAndSamples[i + 7] = (byte)(val >>>  0);
					}
				}
			} else
				throw new AssertionError("Unsupported bit depth");
		}
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(filtersAndSamples);
		} catch (IOException e) {
			throw new AssertionError("Caught impossible exception", e);
		}
		result.idats.add(new Idat(bout.toByteArray()));
		
		return result;
	}
	
	
	private ImageEncoder() {}
	
	
	
	private static final class UpBitDepthRgbaImage implements RgbaImage {
		
		private RgbaImage image;
		private int[] bitDepths;
		private final int mul, rDiv, gDiv, bDiv, aDiv;
		
		
		public UpBitDepthRgbaImage(RgbaImage img) {
			image = img;
			
			bitDepths = img.getBitDepths().clone();
			if (bitDepths.length != 4)
				throw new IllegalArgumentException("Invalid bit depths array");
			for (int i = 0; i < bitDepths.length; i++) {
				int bitDepth = bitDepths[i];
				if (!((i == 3 ? 0 : 1) <= bitDepth && bitDepth <= 16))
					throw new IllegalArgumentException("Invalid bit depths");
			}
			int chosenBitDepth = Math.ceilDiv(IntStream.of(bitDepths).max().getAsInt(), 8) * 8;
			if (chosenBitDepth != 8 && chosenBitDepth != 16)
				throw new AssertionError("Unsupported bit depth");
			
			mul = (2 << chosenBitDepth) - 2;
			rDiv = (1 << bitDepths[0]) - 1;
			gDiv = (1 << bitDepths[1]) - 1;
			bDiv = (1 << bitDepths[2]) - 1;
			aDiv = bitDepths[3] != 0 ? (1 << bitDepths[3]) - 1 : 1;
			
			bitDepths[0] = chosenBitDepth;
			bitDepths[1] = chosenBitDepth;
			bitDepths[2] = chosenBitDepth;
			if (bitDepths[3] != 0)
				bitDepths[3] = chosenBitDepth;
		}
		
		
		@Override public int getWidth() {
			return image.getWidth();
		}
		
		@Override public int getHeight() {
			return image.getHeight();
		}
		
		@Override public int[] getBitDepths() {
			return bitDepths;
		}
		
		
		@Override public long getPixel(int x, int y) {
			long val = image.getPixel(x, y);
			long r = (((val >>> 48) & 0xFFFF) * mul + rDiv) / rDiv >>> 1;
			long g = (((val >>> 32) & 0xFFFF) * mul + gDiv) / gDiv >>> 1;
			long b = (((val >>> 16) & 0xFFFF) * mul + bDiv) / bDiv >>> 1;
			long a = (((val >>>  0) & 0xFFFF) * mul + aDiv) / aDiv >>> 1;
			return r << 48 | g << 32 | b << 16 | a << 0;
		}
		
	}
	
}
