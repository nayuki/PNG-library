/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.image;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;


public final class BufferedRgbaImageTest {
	
	@Test public void testCreateDimensionsRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int width = rand.nextInt(100) + 1;
			int height = rand.nextInt(100) + 1;
			var img = new BufferedRgbaImage(width, height, DEFAULT_BIT_DEPTHS);
			assertEquals(width, img.getWidth());
			assertEquals(height, img.getHeight());
			assertArrayEquals(DEFAULT_BIT_DEPTHS, img.getBitDepths());
			assertEquals(0, img.getPixel(rand.nextInt(width), rand.nextInt(height)));
		}
	}
	
	
	@Test public void testCreateDimensionsBadRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			// Quasi log-uniform distribution
			int width = 1 << rand.nextInt(31);
			width |= rand.nextInt(width);
			int height = 1 << rand.nextInt(31);
			height |= rand.nextInt(height);
			assert width > 0 && height > 0;
			
			switch (rand.nextInt(3)) {
				case 0 -> width  = -width ;
				case 1 -> height = -height;
				case 2 -> {
					width  = -width ;
					height = -height;
				}
				default -> throw new AssertionError("Unreachable value");
			}
			if (rand.nextDouble() < 0.01)
				width = Integer.MIN_VALUE;
			if (rand.nextDouble() < 0.01)
				height = Integer.MIN_VALUE;
			assert width < 0 || height < 0;
			
			try {
				new BufferedRgbaImage(width, height, DEFAULT_BIT_DEPTHS);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testCreateBitDepths() {
		int[][] CASES = {
			{8, 8, 8, 0},
			{8, 8, 8, 8},
			{16, 16, 16, 0},
			{16, 16, 16, 16},
			{1, 1, 1, 0},
			{1, 1, 1, 1},
			{3, 1, 4, 5},
			{16, 7, 11, 10},
			{14, 8, 6, 1},
			{2, 5, 9, 13},
			{7, 14, 12, 0},
		};
		for (int[] cs : CASES) {
			var img = new BufferedRgbaImage(1, 1, cs);
			assertArrayEquals(cs, img.getBitDepths());
		}
	}
	
	
	@Test public void testCreateBitDepthsBad() {
		int[][] CASES = {
			{},
			{1},
			{2, 3},
			{4, 5, 6},
			{7, 8, 9, 10, 11},
			
			{0, 0, 0, 0},
			{-1, 0, 0, 0},
			{8, Integer.MIN_VALUE, 8, 8},
			{17, 16, 15, 14},
			{0, 8, 8, 8},
			{8, 8, 8, -1},
			{16, 16, -16, 16},
			{Integer.MAX_VALUE, 1, 2, 3},
		};
		for (int[] cs : CASES) {
			try {
				new BufferedRgbaImage(1, 1, cs);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testSetPixelBasic() {
		var img = new BufferedRgbaImage(3, 2, new int[]{8, 8, 8, 8});
		img.setPixel(0, 0, 0x0001_0002_0003_0004L);
		assertEquals(0x0001_0002_0003_0004L, img.getPixel(0, 0));
		
		img.setPixel(2, 1, 0x0008_0006_0005_0007L);
		img.setPixel(1, 0, 0x0009_000B_000A_000CL);
		assertEquals(0x0008_0006_0005_0007L, img.getPixel(2, 1));
		assertEquals(0x0009_000B_000A_000CL, img.getPixel(1, 0));
		
		img.setPixel(1, 1, 0x000E_000F_000D_000CL);
		img.setPixel(2, 0, 0x0011_0010_0013_0012L);
		img.setPixel(0, 1, 0x0016_0017_0014_0015L);
		img.setPixel(2, 0, 0x0018_001B_001A_0019L);
		assertEquals(0x0016_0017_0014_0015L, img.getPixel(0, 1));
		assertEquals(0x000E_000F_000D_000CL, img.getPixel(1, 1));
		assertEquals(0x0018_001B_001A_0019L, img.getPixel(2, 0));
	}
	
	
	@Test public void testSetPixelCoordinatesBad() {
		int[][] CASES = {
			{3, 2, -1, 0},
			{3, 2, 0, -1},
			{3, 2, -1, -1},
			{3, 2, Integer.MIN_VALUE, 0},
			{3, 2, 0, Integer.MIN_VALUE},
			{3, 2, Integer.MIN_VALUE, Integer.MIN_VALUE},
			{65536, 10, 66000, -1},
			{65536, 10, 0, 65536},
		};
		
		for (int[] cs : CASES) {
			try {
				var img = new BufferedRgbaImage(cs[0], cs[1], DEFAULT_BIT_DEPTHS);
				img.setPixel(cs[2], cs[3], 0);
				Assert.fail("Expected exception");
			} catch (IndexOutOfBoundsException e) {}  // Pass
		}
	}
	
	
	@Test public void testSetPixelBitDepths() {
		record Case(long pixelValue, int... bitDepths) {}
		Case[] CASES = {
			new Case(0x0080_0080_0080_0000L, 8, 8, 8, 0),
			new Case(0x00FF_00FF_00FF_0000L, 8, 8, 8, 0),
			new Case(0x0080_0080_0080_0080L, 8, 8, 8, 8),
			new Case(0x00FF_00FF_00FF_00FFL, 8, 8, 8, 8),
			new Case(0x8000_8000_8000_0000L, 16, 16, 16, 0),
			new Case(0xFFFF_FFFF_FFFF_0000L, 16, 16, 16, 0),
			new Case(0x8000_8000_8000_8000L, 16, 16, 16, 16),
			new Case(0xFFFF_FFFF_FFFF_FFFFL, 16, 16, 16, 16),
			new Case(0x0000_0002_0004_0008L, 1, 2, 3, 4),
			new Case(0x0001_0003_0007_000FL, 1, 2, 3, 4),
			new Case(0x3000_0080_0400_0010L, 15, 9, 11, 6),
			new Case(0x7FFF_01FF_07FF_003FL, 15, 9, 11, 6),
			new Case(0x0010_0800_0040_0000L, 5, 13, 7, 0),
			new Case(0x001F_1FFF_007F_0000L, 5, 13, 7, 0),
		};
		long[] GOOD_PIXEL_VALUES = {
			0x0000_0000_0000_0000L,
			0x0000_0000_0001_0000L,
			0x0000_0001_0000_0000L,
			0x0000_0001_0001_0000L,
			0x0001_0000_0000_0000L,
			0x0001_0000_0001_0000L,
			0x0001_0001_0000_0000L,
			0x0001_0001_0001_0000L,
		};
		
		for (Case cs : CASES) {
			var img = new BufferedRgbaImage(1, 1, cs.bitDepths);
			assertArrayEquals(cs.bitDepths, img.getBitDepths());
			for (long val : GOOD_PIXEL_VALUES)
				img.setPixel(0, 0, val);
			img.setPixel(0, 0, cs.pixelValue);
		}
	}
	
	
	@Test public void testSetPixelBitDepthBad() {
		record Case(long pixelValue, int... bitDepths) {}
		Case[] CASES = {
			new Case(0x0100_0000_0000_0000L, 8, 8, 8, 0),
			new Case(0x0000_FFFF_0000_0000L, 8, 8, 8, 0),
			new Case(0x1234_0000_5678_0000L, 8, 8, 8, 0),
			new Case(0x0000_0000_0000_0001L, 8, 8, 8, 0),
			new Case(0xDEAD_0000_0000_0000L, 8, 8, 8, 8),
			new Case(0x0000_0BEF_CA00_0FE0L, 8, 8, 8, 8),
			new Case(0x0000_0000_0000_0002L, 16, 16, 16, 0),
			new Case(0x0000_0000_0000_0904L, 16, 16, 16, 0),
			new Case(0x0000_0000_0000_FFFFL, 16, 16, 16, 0),
			new Case(0x0002_0000_0000_0000L, 1, 2, 3, 4),
			new Case(0x0000_0000_0003_0010L, 1, 2, 3, 4),
			new Case(0xFFFF_FFFF_FFFF_FFFFL, 1, 2, 3, 4),
			new Case(0x8000_0000_0000_0000L, 15, 9, 11, 6),
			new Case(0x0000_EE00_DD00_0000L, 15, 9, 11, 6),
			new Case(0x9000_A000_B000_C000L, 15, 9, 11, 6),
			new Case(0x0500_0000_0000_0000L, 5, 13, 7, 0),
			new Case(0x0000_0222_0777_0000L, 5, 13, 7, 0),
			new Case(0x1000_1000_1000_1000L, 5, 13, 7, 0),
		};
		
		for (Case cs : CASES) {
			try {
				var img = new BufferedRgbaImage(1, 1, cs.bitDepths);
				assertArrayEquals(cs.bitDepths, img.getBitDepths());
				img.setPixel(0, 0, cs.pixelValue);
				Assert.fail("Expected exception");
			} catch (IllegalArgumentException e) {}  // Pass
		}
	}
	
	
	@Test public void testSetPixelBitDepthsRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int[] bitDepths = {
				rand.nextInt(16) + 1,
				rand.nextInt(16) + 1,
				rand.nextInt(16) + 1,
				rand.nextInt(17) + 0,
			};
			
			long r, g, b, a;
			if (rand.nextDouble() < 0.5) {
				r = rand.nextInt(1 << bitDepths[0]);
				g = rand.nextInt(1 << bitDepths[1]);
				b = rand.nextInt(1 << bitDepths[2]);
				a = rand.nextInt(1 << bitDepths[3]);
				// valid == true
			} else {
				r = rand.nextInt(1 << 16);
				g = rand.nextInt(1 << 16);
				b = rand.nextInt(1 << 16);
				a = rand.nextInt(1 << 16);
			}
			boolean valid =
				(r >>> bitDepths[0] == 0) &&
				(g >>> bitDepths[1] == 0) &&
				(b >>> bitDepths[2] == 0) &&
				(a >>> bitDepths[3] == 0);
			
			var img = new BufferedRgbaImage(1, 1, bitDepths);
			assertArrayEquals(bitDepths, img.getBitDepths());
			try {
				long val = r << 48 | g << 32 | b << 16 | a << 0;
				img.setPixel(0, 0, val);
				if (!valid)
					Assert.fail("Expected exception");
				assertEquals(val, img.getPixel(0, 0));
			} catch (IllegalArgumentException e) {
				if (valid)
					Assert.fail("Unexpected exception");
			}
		}
	}
	
	
	private static final int[] DEFAULT_BIT_DEPTHS = new int[]{8, 8, 8, 0};
	
	private static Random rand = new Random();
	
}
