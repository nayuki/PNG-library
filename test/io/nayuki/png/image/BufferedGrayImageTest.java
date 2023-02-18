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
import io.nayuki.png.chunk.TestUtil;


public final class BufferedGrayImageTest {
	
	@Test public void testCreateDimensionsRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int width = rand.nextInt(100) + 1;
			int height = rand.nextInt(100) + 1;
			var img = new BufferedGrayImage(width, height, DEFAULT_BIT_DEPTHS);
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
			
			int w = width, h = height;
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new BufferedGrayImage(w, h, DEFAULT_BIT_DEPTHS));
		}
	}
	
	
	@Test public void testCreateBitDepths() {
		int[][] CASES = {
			{8, 0},
			{8, 8},
			{16, 0},
			{16, 16},
			{1, 0},
			{1, 1},
			{3, 1},
			{4, 5},
			{16, 7},
			{11, 10},
			{14, 8},
			{6, 1},
			{2, 5},
			{9, 13},
			{7, 0},
			{14, 0},
			{12, 0},
		};
		
		for (int[] cs : CASES) {
			var img = new BufferedGrayImage(1, 1, cs);
			assertArrayEquals(cs, img.getBitDepths());
		}
	}
	
	
	@Test public void testCreateBitDepthsBad() {
		int[][] CASES = {
			{},
			{1},
			{2, 3, 4},
			{5, 6, 7, 8},
			{9, 10, 11, 12, 13},
			
			{0, 0},
			{-1, 0},
			{8, Integer.MIN_VALUE},
			{17, 16},
			{0, 8},
			{8, -1},
			{-16, 16},
			{Integer.MAX_VALUE, 1},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new BufferedGrayImage(1, 1, cs));
		}
	}
	
	
	@Test public void testSetPixelBasic() {
		var img = new BufferedGrayImage(3, 2, new int[]{8, 8});
		img.setPixel(0, 0, 0x0001_0002);
		assertEquals(0x0001_0002, img.getPixel(0, 0));
		
		img.setPixel(2, 1, 0x0004_0003);
		img.setPixel(1, 0, 0x0005_0006);
		assertEquals(0x0004_0003, img.getPixel(2, 1));
		assertEquals(0x0005_0006, img.getPixel(1, 0));
		
		img.setPixel(1, 1, 0x0007_0008);
		img.setPixel(2, 0, 0x0009_000A);
		img.setPixel(0, 1, 0x000C_000B);
		img.setPixel(2, 0, 0x000E_000D);
		assertEquals(0x000C_000B, img.getPixel(0, 1));
		assertEquals(0x0007_0008, img.getPixel(1, 1));
		assertEquals(0x000E_000D, img.getPixel(2, 0));
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
			TestUtil.runExpect(IndexOutOfBoundsException.class, () -> {
				var img = new BufferedGrayImage(cs[0], cs[1], DEFAULT_BIT_DEPTHS);
				img.setPixel(cs[2], cs[3], 0);
			});
		}
	}
	
	
	@Test public void testSetPixelBitDepths() {
		record Case(int pixelValue, int... bitDepths) {}
		Case[] CASES = {
			new Case(0x0080_0000, 8, 0),
			new Case(0x00FF_0000, 8, 0),
			new Case(0x0080_0080, 8, 8),
			new Case(0x00FF_00FF, 8, 8),
			new Case(0x8000_0000, 16, 0),
			new Case(0xFFFF_0000, 16, 0),
			new Case(0x8000_8000, 16, 16),
			new Case(0xFFFF_FFFF, 16, 16),
			new Case(0x0000_0008, 1, 4),
			new Case(0x0001_000F, 1, 4),
			new Case(0x3000_0010, 15, 6),
			new Case(0x7FFF_003F, 15, 6),
			new Case(0x0010_0000, 5, 0),
			new Case(0x001F_0000, 5, 0),
		};
		int[] GOOD_PIXEL_VALUES = {
			0x0000_0000,
			0x0001_0000,
		};
		
		for (Case cs : CASES) {
			var img = new BufferedGrayImage(1, 1, cs.bitDepths);
			assertArrayEquals(cs.bitDepths, img.getBitDepths());
			for (int val : GOOD_PIXEL_VALUES)
				img.setPixel(0, 0, val);
			img.setPixel(0, 0, cs.pixelValue);
		}
	}
	
	
	@Test public void testSetPixelBitDepthBad() {
		record Case(int pixelValue, int... bitDepths) {}
		Case[] CASES = {
			new Case(0x0100_0000, 8, 0),
			new Case(0x1234_0000, 8, 0),
			new Case(0xFFFF_0000, 8, 0),
			new Case(0x0000_0001, 8, 0),
			new Case(0xDEAD_0000, 8, 8),
			new Case(0x0000_0FE0, 8, 8),
			new Case(0x0000_0002, 16, 0),
			new Case(0x0000_0904, 16, 0),
			new Case(0x0000_FFFF, 16, 0),
			new Case(0x0002_0000, 1, 4),
			new Case(0x0000_0010, 1, 4),
			new Case(0xFFFF_FFFF, 1, 4),
			new Case(0x8000_0000, 15, 6),
			new Case(0xEE00_DD00, 15, 6),
			new Case(0x9000_C000, 15, 6),
			new Case(0x0500_0000, 5, 0),
			new Case(0x0222_0777, 5, 0),
			new Case(0x1000_1000, 5, 0),
		};
		
		for (Case cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class, () -> {
				var img = new BufferedGrayImage(1, 1, cs.bitDepths);
				assertArrayEquals(cs.bitDepths, img.getBitDepths());
				img.setPixel(0, 0, cs.pixelValue);
			});
		}
	}
	
	
	@Test public void testSetPixelBitDepthsRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int[] bitDepths = {
				rand.nextInt(16) + 1,
				rand.nextInt(17) + 0,
			};
			
			int w, a;
			if (rand.nextDouble() < 0.5) {
				w = rand.nextInt(1 << bitDepths[0]);
				a = rand.nextInt(1 << bitDepths[1]);
				// valid == true
			} else {
				w = rand.nextInt(1 << 16);
				a = rand.nextInt(1 << 16);
			}
			boolean valid =
				(w >>> bitDepths[0] == 0) &&
				(a >>> bitDepths[1] == 0);
			
			var img = new BufferedGrayImage(1, 1, bitDepths);
			assertArrayEquals(bitDepths, img.getBitDepths());
			try {
				int val = w << 16 | a << 0;
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
	
	
	@Test public void testCreateCopy() {
		var img = new BufferedGrayImage(new GrayImage() {
			public int getWidth() { return 7; }
			public int getHeight() { return 4; }
			public int[] getBitDepths() { return new int[]{4, 3}; }
			public int getPixel(int x, int y) { return x << 16 | y << 0; }
		});
		assertEquals(7, img.getWidth());
		assertEquals(4, img.getHeight());
		assertArrayEquals(new int[]{4, 3}, img.getBitDepths());
		assertEquals(0x0000_0000, img.getPixel(0, 0));
		assertEquals(0x0001_0000, img.getPixel(1, 0));
		assertEquals(0x0003_0000, img.getPixel(3, 0));
		assertEquals(0x0000_0001, img.getPixel(0, 1));
		assertEquals(0x0002_0001, img.getPixel(2, 1));
		assertEquals(0x0000_0003, img.getPixel(0, 3));
		assertEquals(0x0006_0000, img.getPixel(6, 0));
		assertEquals(0x0006_0003, img.getPixel(6, 3));
	}
	
	
	@Test public void testClone() {
		var img0 = new BufferedGrayImage(2, 1, new int[]{8, 8});
		img0.setPixel(0, 0, 0x000F_00F0);
		img0.setPixel(1, 0, 0x0000_0000);
		var img1 = img0.clone();
		assertEquals(0x000F_00F0, img0.getPixel(0, 0));
		assertEquals(0x0000_0000, img0.getPixel(1, 0));
		assertEquals(0x000F_00F0, img1.getPixel(0, 0));
		assertEquals(0x0000_0000, img1.getPixel(1, 0));
		
		img0.setPixel(0, 0, 0x00AA_00BB);
		assertEquals(0x00AA_00BB, img0.getPixel(0, 0));
		assertEquals(0x0000_0000, img0.getPixel(1, 0));
		assertEquals(0x000F_00F0, img1.getPixel(0, 0));
		assertEquals(0x0000_0000, img1.getPixel(1, 0));
		
		img1.setPixel(1, 0, 0x000D_00C0);
		assertEquals(0x00AA_00BB, img0.getPixel(0, 0));
		assertEquals(0x0000_0000, img0.getPixel(1, 0));
		assertEquals(0x000F_00F0, img1.getPixel(0, 0));
		assertEquals(0x000D_00C0, img1.getPixel(1, 0));
	}
	
	
	private static final int[] DEFAULT_BIT_DEPTHS = new int[]{8, 0};
	
	private static Random rand = new Random();
	
}
