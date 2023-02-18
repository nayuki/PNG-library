/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import static io.nayuki.png.TestUtil.rand;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import io.nayuki.png.image.BufferedGrayImage;
import io.nayuki.png.image.BufferedPaletteImage;
import io.nayuki.png.image.BufferedRgbaImage;


public final class ImageCodecTest {
	
	@Test public void testRgbaImage() throws IOException {
		final int TRIALS = 1000;
		for (int i = 0; i < TRIALS; i++) {
			
			int width  = rand.nextInt(30) + 1;
			int height = rand.nextInt(30) + 1;
			int[] bitDepths = switch (rand.nextInt(4)) {
				case 0 -> new int[]{ 8,  8,  8,  0};
				case 1 -> new int[]{ 8,  8,  8,  8};
				case 2 -> new int[]{16, 16, 16,  0};
				case 3 -> new int[]{16, 16, 16, 16};
				default -> throw new AssertionError("Unreachable value");
			};
			if (rand.nextDouble() < 0.8) {
				for (int j = 0; j < bitDepths.length; j++) {
					if (bitDepths[j] != 0)
						bitDepths[j] = rand.nextInt(bitDepths[j]) + 1;
				}
			}
			
			var img0 = new BufferedRgbaImage(width, height, bitDepths);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					long val = 0;
					for (int bits : bitDepths)
						val = (val << 16) | rand.nextInt(1 << bits);
					img0.setPixel(x, y, val);
				}
			}
			PngImage png0 = ImageEncoder.toPng(img0);
			var bout = new ByteArrayOutputStream();
			png0.write(bout);
			
			var bin = new ByteArrayInputStream(bout.toByteArray());
			PngImage png1 = PngImage.read(bin);
			var img1 = (BufferedRgbaImage)ImageDecoder.toImage(png1);
			Assert.assertArrayEquals(img0.getBitDepths(), img1.getBitDepths());
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					Assert.assertEquals(img0.getPixel(x, y), img1.getPixel(x, y));
			}
		}
	}
	
	
	@Test public void testGrayImage() throws IOException {
		final int TRIALS = 1000;
		for (int i = 0; i < TRIALS; i++) {
			
			int width  = rand.nextInt(30) + 1;
			int height = rand.nextInt(30) + 1;
			int[] bitDepths = switch (rand.nextInt(7)) {
				case 0 -> new int[]{ 1,  0};
				case 1 -> new int[]{ 2,  0};
				case 2 -> new int[]{ 4,  0};
				case 3 -> new int[]{ 8,  0};
				case 4 -> new int[]{ 8,  8};
				case 5 -> new int[]{16,  0};
				case 6 -> new int[]{16, 16};
				default -> throw new AssertionError("Unreachable value");
			};
			if (rand.nextDouble() < 0.8) {
				for (int j = 0; j < bitDepths.length; j++) {
					if (bitDepths[j] != 0)
						bitDepths[j] = rand.nextInt(bitDepths[j]) + 1;
				}
			}
			
			var img0 = new BufferedGrayImage(width, height, bitDepths);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int val = 0;
					for (int bits : bitDepths)
						val = (val << 16) | rand.nextInt(1 << bits);
					img0.setPixel(x, y, val);
				}
			}
			PngImage png0 = ImageEncoder.toPng(img0);
			var bout = new ByteArrayOutputStream();
			png0.write(bout);
			
			var bin = new ByteArrayInputStream(bout.toByteArray());
			PngImage png1 = PngImage.read(bin);
			var img1 = (BufferedGrayImage)ImageDecoder.toImage(png1);
			Assert.assertArrayEquals(img0.getBitDepths(), img1.getBitDepths());
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					Assert.assertEquals(img0.getPixel(x, y), img1.getPixel(x, y));
			}
		}
	}
	
	
	@Test public void testPaletteImage() throws IOException {
		final int TRIALS = 1000;
		for (int i = 0; i < TRIALS; i++) {
			
			int width  = rand.nextInt(30) + 1;
			int height = rand.nextInt(30) + 1;
			int[] bitDepths = {8, 8, 8, 8};
			if (rand.nextDouble() < 0.8) {
				for (int j = 0; j < bitDepths.length; j++) {
					if (bitDepths[j] != 0)
						bitDepths[j] = rand.nextInt(bitDepths[j]) + 1;
				}
			}
			bitDepths[3] = rand.nextInt(2) * 8;
			long[] palette = new long[rand.nextInt(256) + 1];
			for (int j = 0; j < palette.length; j++) {
				long val = 0;
				for (int bits : bitDepths)
					val = (val << 16) | rand.nextInt(1 << bits);
				palette[j] = val;
			}
			
			var img0 = new BufferedPaletteImage(width, height, bitDepths, palette);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					img0.setPixel(x, y, rand.nextInt(palette.length));
			}
			PngImage png0 = ImageEncoder.toPng(img0);
			var bout = new ByteArrayOutputStream();
			png0.write(bout);
			
			var bin = new ByteArrayInputStream(bout.toByteArray());
			PngImage png1 = PngImage.read(bin);
			var img1 = (BufferedPaletteImage)ImageDecoder.toImage(png1);
			Assert.assertArrayEquals(img0.getBitDepths(), img1.getBitDepths());
			Assert.assertArrayEquals(img0.getPalette(), img1.getPalette());
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++)
					Assert.assertEquals(img0.getPixel(x, y), img1.getPixel(x, y));
			}
		}
	}
	
}
