/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
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
import io.nayuki.png.chunk.Plte;
import io.nayuki.png.chunk.Sbit;
import io.nayuki.png.chunk.Trns;
import io.nayuki.png.image.GrayImage;
import io.nayuki.png.image.PaletteImage;
import io.nayuki.png.image.RgbaImage;


/**
 * Encodes an image (where pixels can be read) to a {@link PngImage} object. Not instantiable.
 * @see ImageDecoder
 */
public final class ImageEncoder {
	
	/**
	 * Encodes the specified image to a new PNG image. The input image
	 * can have any bit depth allowed by the {@code RgbaImage} contract.
	 * @param img the image to encode (not {@code null})
	 * @return a new PNG image (not {@code null})
	 * @throws NullPointerException if {@code img} is {@code null}
	 */
	public static PngImage toPng(RgbaImage img) {
		Objects.requireNonNull(img);
		int[] bitDepths = img.getBitDepths();
		int bitDepth = bitDepths[0];
		boolean hasAlpha = bitDepths[3] > 0;
		// If bitDepths is not in the set {(8,8,8,0), (8,8,8,8), (16,16,16,0), (16,16,16,16)}
		boolean supported = (bitDepth == 8 || bitDepth == 16) && bitDepths[1] == bitDepth && bitDepths[2] == bitDepth && (!hasAlpha || bitDepths[3] == bitDepth);
		if (!supported) {
			PngImage result = toPng(new UpBitDepthRgbaImage(img));
			byte[] bitDepthsBytes;
			if (!hasAlpha)
				bitDepthsBytes = new byte[]{(byte)bitDepths[0], (byte)bitDepths[1], (byte)bitDepths[2]};
			else
				bitDepthsBytes = new byte[]{(byte)bitDepths[0], (byte)bitDepths[1], (byte)bitDepths[2], (byte)bitDepths[3]};
			result.afterIhdr.add(new Sbit(bitDepthsBytes));
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
		
		int bytesPerRow = Math.toIntExact(Math.ceilDiv((long)width * bitDepth * (hasAlpha ? 4 : 3), 8) + 1);
		var filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		for (int y = 0, i = 0; y < height; y++) {
			filtersAndSamples[i] = 0;
			i++;
			
			switch (bitDepth * 10 + (hasAlpha ? 1 : 0)) {
				case 8_0 -> {
					for (int x = 0; x < width; x++, i += 3) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 48);
						filtersAndSamples[i + 1] = (byte)(val >>> 32);
						filtersAndSamples[i + 2] = (byte)(val >>> 16);
					}
				}
				case 8_1 -> {
					for (int x = 0; x < width; x++, i += 4) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 48);
						filtersAndSamples[i + 1] = (byte)(val >>> 32);
						filtersAndSamples[i + 2] = (byte)(val >>> 16);
						filtersAndSamples[i + 3] = (byte)(val >>>  0);
					}
				}
				case 16_0 -> {
					for (int x = 0; x < width; x++, i += 6) {
						long val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 56);
						filtersAndSamples[i + 1] = (byte)(val >>> 48);
						filtersAndSamples[i + 2] = (byte)(val >>> 40);
						filtersAndSamples[i + 3] = (byte)(val >>> 32);
						filtersAndSamples[i + 4] = (byte)(val >>> 24);
						filtersAndSamples[i + 5] = (byte)(val >>> 16);
					}
				}
				case 16_1 -> {
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
				default -> throw new AssertionError("Unreachable value");
			}
		}
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(filtersAndSamples);
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		result.idats.add(new Idat(bout.toByteArray()));
		
		return result;
	}
	
	
	/**
	 * Encodes the specified image to a new PNG image. The input image
	 * can have any bit depth allowed by the {@code GrayImage} contract.
	 * @param img the image to encode (not {@code null})
	 * @return a new PNG image (not {@code null})
	 * @throws NullPointerException if {@code img} is {@code null}
	 */
	public static PngImage toPng(GrayImage img) {
		Objects.requireNonNull(img);
		int[] bitDepths = img.getBitDepths();
		int bitDepth = bitDepths[0];
		boolean hasAlpha = bitDepths[1] > 0;
		// If bitDepths is not in the set {(1,0), (2,0), (4,0), (8,0), (8,8), (16,0), (16,16)}
		boolean supported = (bitDepth == 1 || bitDepth == 2 || bitDepth == 4) && !hasAlpha;
		supported |= (bitDepth == 8 || bitDepth == 16) && (!hasAlpha || bitDepths[1] == bitDepth);
		if (!supported) {
			PngImage result = toPng(new UpBitDepthGrayImage(img));
			byte[] bitDepthsBytes;
			if (!hasAlpha)
				bitDepthsBytes = new byte[]{(byte)bitDepths[0]};
			else
				bitDepthsBytes = new byte[]{(byte)bitDepths[0], (byte)bitDepths[1]};
			result.afterIhdr.add(new Sbit(bitDepthsBytes));
			return result;
		}

		int width = img.getWidth();
		int height = img.getHeight();
		var result = new PngImage();
		result.ihdr = Optional.of(new Ihdr(
			width, height, bitDepth,
			hasAlpha ? Ihdr.ColorType.GRAYSCALE_WITH_ALPHA : Ihdr.ColorType.GRAYSCALE,
			Ihdr.CompressionMethod.ZLIB_DEFLATE,
			Ihdr.FilterMethod.ADAPTIVE,
			Ihdr.InterlaceMethod.NONE));
		
		int bytesPerRow = Math.toIntExact(Math.ceilDiv((long)width * bitDepth * (hasAlpha ? 2 : 1), 8) + 1);
		var filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		for (int y = 0, i = 0; y < height; y++) {
			filtersAndSamples[i] = 0;
			i++;
			
			switch (bitDepth * 10 + (hasAlpha ? 1 : 0)) {
				case 1_0, 2_0, 4_0 -> {
					int xMask = 8 / bitDepth - 1;
					int b = 0;
					for (int x = 0; x < width; x++) {
						int val = img.getPixel(x, y);
						b = (b << bitDepth) | (val >>> 16);
						if ((x & xMask) == xMask) {
							filtersAndSamples[i] = (byte)b;
							i++;
						}
					}
					if ((width & xMask) != 0) {
						filtersAndSamples[i] = (byte)(b << (8 - (width & xMask) * bitDepth));
						i++;
					}
				}
				case 8_0 -> {
					for (int x = 0; x < width; x++, i += 1) {
						int val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 16);
					}
				}
				case 8_1 -> {
					for (int x = 0; x < width; x++, i += 2) {
						int val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 16);
						filtersAndSamples[i + 1] = (byte)(val >>>  0);
					}
				}
				case 16_0 -> {
					for (int x = 0; x < width; x++, i += 2) {
						int val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 24);
						filtersAndSamples[i + 1] = (byte)(val >>> 16);
					}
				}
				case 16_1 -> {
					for (int x = 0; x < width; x++, i += 4) {
						int val = img.getPixel(x, y);
						filtersAndSamples[i + 0] = (byte)(val >>> 24);
						filtersAndSamples[i + 1] = (byte)(val >>> 16);
						filtersAndSamples[i + 2] = (byte)(val >>>  8);
						filtersAndSamples[i + 3] = (byte)(val >>>  0);
					}
				}
				default -> throw new AssertionError("Unreachable value");
			}
		}
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(filtersAndSamples);
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		result.idats.add(new Idat(bout.toByteArray()));
		
		return result;
	}
	
	
	/**
	 * Encodes the specified image to a new PNG image.
	 * @param img the image to encode (not {@code null})
	 * @return a new PNG image (not {@code null})
	 * @throws NullPointerException if {@code img} is {@code null}
	 */
	public static PngImage toPng(PaletteImage img) {
		Objects.requireNonNull(img);
		long[] palette = img.getPalette();
		int bitDepth;  // Equal to 2^ceil(log2(ceil(log2(palette.length))))}
		if (palette.length <= (1 << 1))
			bitDepth = 1;
		else if (palette.length <= (1 << 2))
			bitDepth = 2;
		else if (palette.length <= (1 << 4))
			bitDepth = 4;
		else if (palette.length <= (1 << 8))
			bitDepth = 8;
		else
			throw new AssertionError("Unreachable value");
		
		int width = img.getWidth();
		int height = img.getHeight();
		var result = new PngImage();
		result.ihdr = Optional.of(new Ihdr(
			width, height, bitDepth,
			Ihdr.ColorType.INDEXED_COLOR,
			Ihdr.CompressionMethod.ZLIB_DEFLATE,
			Ihdr.FilterMethod.ADAPTIVE,
			Ihdr.InterlaceMethod.NONE));
		
		var paletteBytes = new byte[Math.multiplyExact(palette.length, 3)];
		int transpLen = 0;
		{  // Up-convert RGB channels to 8 bits
			long mul = (2 << 8) - 2;
			int[] bitDepths = img.getBitDepths();
			long rDiv = (1 << bitDepths[0]) - 1;
			long gDiv = (1 << bitDepths[1]) - 1;
			long bDiv = (1 << bitDepths[2]) - 1;
			for (int i = 0; i < palette.length; i++) {
				long rgba = palette[i];
				long r = (rgba >>> 48) & 0xFF;
				long g = (rgba >>> 32) & 0xFF;
				long b = (rgba >>> 16) & 0xFF;
				long a = (rgba >>>  0) & 0xFF;
				r = (r * mul + rDiv) / rDiv >>> 1;
				g = (g * mul + gDiv) / gDiv >>> 1;
				b = (b * mul + bDiv) / bDiv >>> 1;
				paletteBytes[i * 3 + 0] = (byte)r;
				paletteBytes[i * 3 + 1] = (byte)g;
				paletteBytes[i * 3 + 2] = (byte)b;
				if (bitDepths[3] > 0 && a < 0xFF)
					transpLen = i + 1;
			}
			if (bitDepths[0] != 8 || bitDepths[1] != 8 || bitDepths[2] != 8)
				result.afterIhdr.add(new Sbit(new byte[]{(byte)bitDepths[0], (byte)bitDepths[1], (byte)bitDepths[2]}));
		}
		result.plte = Optional.of(new Plte(paletteBytes));
		if (transpLen > 0) {
			var transpBytes = new byte[transpLen];
			for (int i = 0; i < transpBytes.length; i++)
				transpBytes[i] = (byte)palette[i];
			result.afterPlte.add(new Trns(transpBytes));
		}
		
		int bytesPerRow = Math.toIntExact(Math.ceilDiv((long)width * bitDepth, 8) + 1);
		var filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		for (int y = 0, i = 0; y < height; y++) {
			filtersAndSamples[i] = 0;
			i++;
			
			switch (bitDepth) {
				case 1, 2, 4 -> {
					int xMask = 8 / bitDepth - 1;
					int b = 0;
					for (int x = 0; x < width; x++) {
						b = (b << bitDepth) | img.getPixel(x, y);
						if ((x & xMask) == xMask) {
							filtersAndSamples[i] = (byte)b;
							i++;
						}
					}
					if ((width & xMask) != 0) {
						filtersAndSamples[i] = (byte)(b << (8 - (width & xMask) * bitDepth));
						i++;
					}
				}
				case 8 -> {
					for (int x = 0; x < width; x++, i++)
						filtersAndSamples[i] = (byte)img.getPixel(x, y);
				}
				default -> throw new AssertionError("Unreachable value");
			}
		}
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(filtersAndSamples);
		} catch (IOException e) {
			throw new AssertionError("Unreachable exception", e);
		}
		result.idats.add(new Idat(bout.toByteArray()));
		
		return result;
	}
	
	
	private ImageEncoder() {}
	
	
	
	/**
	 * Linearly up-scales sample values so that all channels
	 * have the same bit depth that is a multiple of 8.
	 */
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
				if (!((i == bitDepths.length - 1 ? 0 : 1) <= bitDepth && bitDepth <= 16))
					throw new IllegalArgumentException("Invalid bit depths");
			}
			// Round up to nearest multiple of 8
			int chosenBitDepth = Math.ceilDiv(IntStream.of(bitDepths).max().getAsInt(), 8) * 8;
			if (chosenBitDepth != 8 && chosenBitDepth != 16)
				throw new AssertionError("Unreachable value");
			
			mul = (2 << chosenBitDepth) - 2;
			rDiv = (1 << bitDepths[0]) - 1;
			gDiv = (1 << bitDepths[1]) - 1;
			bDiv = (1 << bitDepths[2]) - 1;
			aDiv = Math.max((1 << bitDepths[3]) - 1, 1);
			
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
			// For each channel: out = floor(in / IN_MAX * OUT_MAX + 0.5)
			long r = (((val >>> 48) & 0xFFFF) * mul + rDiv) / rDiv >>> 1;
			long g = (((val >>> 32) & 0xFFFF) * mul + gDiv) / gDiv >>> 1;
			long b = (((val >>> 16) & 0xFFFF) * mul + bDiv) / bDiv >>> 1;
			long a = (((val >>>  0) & 0xFFFF) * mul + aDiv) / aDiv >>> 1;
			return r << 48 | g << 32 | b << 16 | a << 0;
		}
		
	}
	
	
	
	/**
	 * Linearly up-scales sample values to match one of the
	 * supported grayscale (with or without alpha) image formats.
	 */
	private static final class UpBitDepthGrayImage implements GrayImage {
		
		private GrayImage image;
		private int[] bitDepths;
		private final long mul, wDiv, aDiv;
		
		
		public UpBitDepthGrayImage(GrayImage img) {
			image = img;
			
			bitDepths = img.getBitDepths().clone();
			if (bitDepths.length != 2)
				throw new IllegalArgumentException("Invalid bit depths array");
			for (int i = 0; i < bitDepths.length; i++) {
				int bitDepth = bitDepths[i];
				if (!((i == bitDepths.length - 1 ? 0 : 1) <= bitDepth && bitDepth <= 16))
					throw new IllegalArgumentException("Invalid bit depths");
			}
			
			int chosenBitDepth;
			if (bitDepths[1] > 0) {
				// Round up to nearest multiple of 8
				chosenBitDepth = Math.ceilDiv(IntStream.of(bitDepths).max().getAsInt(), 8) * 8;
				if (chosenBitDepth != 8 && chosenBitDepth != 16)
					throw new AssertionError("Unreachable value");
			} else {
				// Round up to nearest power of 2, i.e. 2^ceil(log2(bitDepths[0]))
				chosenBitDepth = 0x8000_0000 >>> (Integer.numberOfLeadingZeros(bitDepths[0] - 1) - 1);
				if (chosenBitDepth != 1 && chosenBitDepth != 2 && chosenBitDepth != 4 && chosenBitDepth != 8 && chosenBitDepth != 16)
					throw new AssertionError("Unreachable value");
			}
			
			mul = (2 << chosenBitDepth) - 2;
			wDiv = (1 << bitDepths[0]) - 1;
			aDiv = Math.max((1 << bitDepths[1]) - 1, 1);
			
			bitDepths[0] = chosenBitDepth;
			if (bitDepths[1] != 0)
				bitDepths[1] = chosenBitDepth;
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
		
		
		@Override public int getPixel(int x, int y) {
			int val = image.getPixel(x, y);
			// For each channel: out = floor(in / IN_MAX * OUT_MAX + 0.5)
			int w = (int)((((val >>> 16) & 0xFFFF) * mul + wDiv) / wDiv >>> 1);
			int a = (int)((((val >>>  0) & 0xFFFF) * mul + aDiv) / aDiv >>> 1);
			return w << 16 | a << 0;
		}
		
	}
	
}
