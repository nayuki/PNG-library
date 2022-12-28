package io.nayuki.png;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.image.Argb16Image;
import io.nayuki.png.image.RgbaImage;


public final class ImageEncoder {
	
	public static PngImage toPng(RgbaImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int[] bitDepths = img.getBitDepths();
		if (bitDepths[0] != 8 || bitDepths[1] != 8 || bitDepths[2] != 8)
			throw new IllegalArgumentException();
		boolean hasAlpha = switch (bitDepths[3]) {
			case 0 -> false;
			case 8 -> true;
			default -> throw new IllegalArgumentException();
		};
		
		var result = new PngImage();
		result.ihdr = Optional.of(new Ihdr(
			width, height, 8,
			hasAlpha ? Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA : Ihdr.ColorType.TRUE_COLOR,
			Ihdr.CompressionMethod.DEFLATE,
			Ihdr.FilterMethod.ADAPTIVE,
			Ihdr.InterlaceMethod.NONE));
		
		int bytesPerRow = Math.addExact(1, Math.multiplyExact(width, hasAlpha ? 4 : 3));
		byte[] filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		for (int y = 0, i = 0; y < height; y++) {
			filtersAndSamples[i] = 0;
			i++;
			if (hasAlpha) {
				for (int x = 0; x < width; x++) {
					long val = img.getPixel(x, y);
					filtersAndSamples[i + 0] = (byte)(val >>> 48);
					filtersAndSamples[i + 1] = (byte)(val >>> 32);
					filtersAndSamples[i + 2] = (byte)(val >>> 16);
					filtersAndSamples[i + 3] = (byte)(val >>>  0);
					i += 4;
				}
			} else {
				for (int x = 0; x < width; x++) {
					long val = img.getPixel(x, y);
					filtersAndSamples[i + 0] = (byte)(val >>> 48);
					filtersAndSamples[i + 1] = (byte)(val >>> 32);
					filtersAndSamples[i + 2] = (byte)(val >>> 16);
					i += 3;
				}
			}
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
	
	
	public static PngImage toPng(Argb16Image img) {
		int width = img.getWidth();
		int height = img.getHeight();
		long[] pixels = new long[Math.multiplyExact(width, height)];
		int opacity = 0xFFFF;
		for (int y = 0, i = 0; y < height; y++) {
			for (int x = 0; x < width; x++, i++) {
				long val = img.getPixel(x, y);
				pixels[i] = val;
				opacity &= (int)(val >>> 48);
			}
		}
		boolean hasNonOpaque = opacity != 0xFFFF;
		
		var result = new PngImage();
		result.ihdr = Optional.of(new Ihdr(
			width, height, 16,
			hasNonOpaque ? Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA : Ihdr.ColorType.TRUE_COLOR,
			Ihdr.CompressionMethod.DEFLATE,
			Ihdr.FilterMethod.ADAPTIVE,
			Ihdr.InterlaceMethod.NONE));
		
		int bytesPerRow = Math.addExact(1, Math.multiplyExact(width, hasNonOpaque ? 8 : 6));
		byte[] filtersAndSamples = new byte[Math.multiplyExact(bytesPerRow, height)];
		if (hasNonOpaque) {
			for (int inIndex = 0, x = 0, outIndex = 0; inIndex < pixels.length; inIndex++, x++) {
				if (x == 0 || x == width) {
					x = 0;
					filtersAndSamples[outIndex] = 0;
					outIndex++;
				}
				long val = pixels[inIndex];
				filtersAndSamples[outIndex + 0] = (byte)(val >>> 40);
				filtersAndSamples[outIndex + 1] = (byte)(val >>> 32);
				filtersAndSamples[outIndex + 2] = (byte)(val >>> 24);
				filtersAndSamples[outIndex + 3] = (byte)(val >>> 16);
				filtersAndSamples[outIndex + 4] = (byte)(val >>>  8);
				filtersAndSamples[outIndex + 5] = (byte)(val >>>  0);
				filtersAndSamples[outIndex + 6] = (byte)(val >>> 56);
				filtersAndSamples[outIndex + 7] = (byte)(val >>> 48);
				outIndex += 8;
			}
		} else {
			for (int inIndex = 0, x = 0, outIndex = 0; inIndex < pixels.length; inIndex++, x++) {
				if (x == 0 || x == width) {
					x = 0;
					filtersAndSamples[outIndex] = 0;
					outIndex++;
				}
				long val = pixels[inIndex];
				filtersAndSamples[outIndex + 0] = (byte)(val >>> 40);
				filtersAndSamples[outIndex + 1] = (byte)(val >>> 32);
				filtersAndSamples[outIndex + 2] = (byte)(val >>> 24);
				filtersAndSamples[outIndex + 3] = (byte)(val >>> 16);
				filtersAndSamples[outIndex + 4] = (byte)(val >>>  8);
				filtersAndSamples[outIndex + 5] = (byte)(val >>>  0);
				outIndex += 6;
			}
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
