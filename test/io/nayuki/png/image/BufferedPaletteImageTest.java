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
import org.junit.Test;
import io.nayuki.png.TestUtil;


public final class BufferedPaletteImageTest {
	
	@Test public void testCreateDimensionsRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int width = rand.nextInt(100) + 1;
			int height = rand.nextInt(100) + 1;
			int[] bitDepths = {8, 8, 8, 0};
			var img = new BufferedPaletteImage(width, height, bitDepths, new long[1]);
			assertEquals(width, img.getWidth());
			assertEquals(height, img.getHeight());
			assertArrayEquals(bitDepths, img.getBitDepths());
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
				() -> new BufferedPaletteImage(w, h, DEFAULT_BIT_DEPTHS, new long[1]));
		}
	}
	
	
	@Test public void testCreateBitDepths() {
		int[][] CASES = {
			{8, 8, 8, 0},
			{8, 8, 8, 8},
			{3, 1, 4, 0},
			{6, 7, 1, 8},
			{4, 8, 6, 0},
			{2, 5, 8, 8},
			{7, 4, 2, 0},
		};
		
		for (int[] cs : CASES) {
			var img = new BufferedPaletteImage(1, 1, cs, new long[1]);
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
			{1, 1, 1, 1},
			{8, 8, 8, 1},
			{8, 8, 8, 9},
			{0, 8, 8, 8},
			{8, 8, 8, -1},
			{16, 16, 16, 0},
			{16, 16, 16, 16},
			{16, 16, -16, 16},
			{Integer.MAX_VALUE, 1, 2, 3},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new BufferedPaletteImage(1, 1, cs, new long[1]));
		}
	}
	
	
	@Test public void testCreatePaletteLengths() {
		int[] CASES = {
			1,
			2,
			3,
			4,
			5,
			7,
			8,
			10,
			13,
			16,
			24,
			32,
			49,
			64,
			65,
			91,
			128,
			144,
			192,
			224,
			237,
			255,
			256,
		};
		
		for (int cs : CASES)
			new BufferedPaletteImage(1, 1, new int[]{8, 8, 8, 8}, new long[cs]);
	}
	
	
	@Test public void testCreatePaletteLengthsBad() {
		int[] CASES = {
			0,
			257,
			258,
			512,
			1024,
			3072,
			4096,
			10000,
			65536,
		};
		
		for (int cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new BufferedPaletteImage(1, 1, new int[]{8, 8, 8, 8}, new long[cs]));
		}
	}
	
	
	@Test public void testSetPixel() {
		var img = new BufferedPaletteImage(3, 2, new int[]{8, 8, 8, 8}, new long[8]);
		img.setPixel(0, 0, 1);
		assertEquals(1, img.getPixel(0, 0));
		
		img.setPixel(2, 1, 2);
		img.setPixel(1, 0, 3);
		assertEquals(2, img.getPixel(2, 1));
		assertEquals(3, img.getPixel(1, 0));
		
		img.setPixel(1, 1, 4);
		img.setPixel(2, 0, 5);
		img.setPixel(0, 1, 6);
		img.setPixel(2, 0, 7);
		assertEquals(6, img.getPixel(0, 1));
		assertEquals(4, img.getPixel(1, 1));
		assertEquals(7, img.getPixel(2, 0));
	}
	
	
	@Test public void testSetPixelRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int paletteLen = rand.nextInt(256) + 1;
			var img = new BufferedPaletteImage(1, 1, DEFAULT_BIT_DEPTHS, new long[paletteLen]);
			int val = rand.nextInt(1024) - 256;
			if (0 <= val && val < paletteLen)
				img.setPixel(0, 0, val);
			else {
				TestUtil.runExpect(IllegalArgumentException.class,
					() -> img.setPixel(0, 0, val));
			}
		}
	}
	
	
	@Test public void testSetPixelBad() {
		int[] CASES = {
			Integer.MIN_VALUE,
			-200,
			-1,
			8,
			9,
			256,
			1000,
			Integer.MAX_VALUE,
		};
		
		var img = new BufferedPaletteImage(3, 2, new int[]{8, 8, 8, 8}, new long[8]);
		for (int cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> img.setPixel(0, 0, cs));
		}
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
				var img = new BufferedPaletteImage(cs[0], cs[1], DEFAULT_BIT_DEPTHS, new long[1]);
				img.setPixel(cs[2], cs[3], 0);
			});
		}
	}
	
	
	@Test public void testCreateSetPaletteBitDepths() {
		record Case(long paletteEntry, int... bitDepths) {}
		Case[] CASES = {
			new Case(0x0080_0080_0080_0000L, 8, 8, 8, 0),
			new Case(0x00FF_00FF_00FF_0000L, 8, 8, 8, 0),
			new Case(0x0080_0080_0080_0080L, 8, 8, 8, 8),
			new Case(0x00FF_00FF_00FF_00FFL, 8, 8, 8, 8),
			new Case(0x0000_0002_0004_0000L, 1, 2, 3, 0),
			new Case(0x0001_0003_0007_0000L, 1, 2, 3, 0),
			new Case(0x000D_0080_0004_0080L, 5, 8, 4, 8),
			new Case(0x001F_00FF_000F_00FFL, 5, 8, 4, 8),
			new Case(0x0010_0004_0040_0000L, 6, 3, 7, 0),
			new Case(0x003F_0007_007F_0000L, 6, 3, 7, 0),
		};
		long[] GOOD_PALETTE_VALUES = {
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
			for (long val : GOOD_PALETTE_VALUES) {
				long[] pal = {val};
				var img = new BufferedPaletteImage(1, 1, cs.bitDepths, pal);
				assertArrayEquals(cs.bitDepths, img.getBitDepths());
				img.setPalette(new long[1]);
				img.setPalette(pal);
			}
			
			long[] pal = {cs.paletteEntry};
			var img = new BufferedPaletteImage(1, 1, cs.bitDepths, pal);
			assertArrayEquals(cs.bitDepths, img.getBitDepths());
			img.setPalette(new long[1]);
			img.setPalette(pal);
		}
	}
	
	
	@Test public void testCreateSetPaletteBitDepthBad() {
		record Case(long paletteEntry, int... bitDepths) {}
		Case[] CASES = {
			new Case(0x0100_0000_0000_0000L, 8, 8, 8, 0),
			new Case(0x0000_FFFF_0000_0000L, 8, 8, 8, 0),
			new Case(0x1234_0000_5678_0000L, 8, 8, 8, 0),
			new Case(0x0000_0000_0000_0001L, 8, 8, 8, 0),
			new Case(0xDEAD_0000_0000_0000L, 8, 8, 8, 8),
			new Case(0x0000_0BEF_CA00_0FE0L, 8, 8, 8, 8),
			new Case(0x0002_0000_0000_0000L, 1, 2, 3, 0),
			new Case(0x0000_0000_0003_0010L, 1, 2, 3, 0),
			new Case(0xFFFF_FFFF_FFFF_FFFFL, 1, 2, 3, 0),
			new Case(0x8000_0000_0000_0000L, 5, 8, 4, 8),
			new Case(0x0000_EE00_DD00_0000L, 5, 8, 4, 8),
			new Case(0x9000_A000_B000_C000L, 5, 8, 4, 8),
			new Case(0x0500_0000_0000_0000L, 6, 3, 7, 0),
			new Case(0x0000_0222_0777_0000L, 6, 3, 7, 0),
			new Case(0x1000_1000_1000_1000L, 6, 3, 7, 0),
		};
		
		for (Case cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class, () -> {
				var img = new BufferedPaletteImage(1, 1, cs.bitDepths, new long[1]);
				assertArrayEquals(cs.bitDepths, img.getBitDepths());
				img.setPalette(new long[]{cs.paletteEntry});
			});
		}
	}
	
	
	@Test public void testCreateSetPaletteBitDepthsRandom() {
		final int TRIALS = 100_000;
		for (int i = 0; i < TRIALS; i++) {
			int[] bitDepths = {
				rand.nextInt(8) + 1,
				rand.nextInt(8) + 1,
				rand.nextInt(8) + 1,
				rand.nextInt(2) * 8,
			};
			
			long r, g, b, a;
			if (rand.nextDouble() < 0.5) {
				r = rand.nextInt(1 << bitDepths[0]);
				g = rand.nextInt(1 << bitDepths[1]);
				b = rand.nextInt(1 << bitDepths[2]);
				a = rand.nextInt(1 << bitDepths[3]);
				// valid == true
			} else {
				r = rand.nextInt(1 << 8);
				g = rand.nextInt(1 << 8);
				b = rand.nextInt(1 << 8);
				a = rand.nextInt(1 << 8);
			}
			boolean valid =
				(r >>> bitDepths[0] == 0) &&
				(g >>> bitDepths[1] == 0) &&
				(b >>> bitDepths[2] == 0) &&
				(a >>> bitDepths[3] == 0);
			
			var img = new BufferedPaletteImage(1, 1, bitDepths, new long[1]);
			assertArrayEquals(bitDepths, img.getBitDepths());
			var pal = new long[]{r << 48 | g << 32 | b << 16 | a << 0};
			if (valid)
				img.setPalette(pal);
			else {
				TestUtil.runExpect(IllegalArgumentException.class,
					() -> img.setPalette(pal));
			}
		}
	}
	
	
	@Test public void testSetPaletteLengthShorter() {
		var img = new BufferedPaletteImage(10, 1, DEFAULT_BIT_DEPTHS, new long[2]);
		img.setPalette(new long[1]);
		img.setPalette(new long[2]);
		img.setPixel(0, 0, 0);
		img.setPixel(1, 0, 1);
		TestUtil.runExpect(IllegalArgumentException.class,
			() -> img.setPalette(new long[1]));
	}
	
	
	@Test public void testSetPaletteLengthSame() {
		var img = new BufferedPaletteImage(10, 1, DEFAULT_BIT_DEPTHS, new long[4]);
		img.setPalette(new long[4]);
		img.setPixel(0, 0, 3);
		img.setPixel(1, 0, 2);
		img.setPixel(2, 0, 1);
		img.setPixel(3, 0, 0);
		img.setPalette(new long[4]);
	}
	
	
	@Test public void testSetPaletteLengthLonger() {
		var img = new BufferedPaletteImage(10, 1, DEFAULT_BIT_DEPTHS, new long[7]);
		img.setPalette(new long[8]);
		img.setPalette(new long[7]);
		img.setPixel(7, 0, 3);
		img.setPixel(9, 0, 1);
		img.setPixel(4, 0, 4);
		img.setPixel(2, 0, 1);
		img.setPixel(0, 0, 5);
		img.setPixel(5, 0, 0);
		img.setPixel(3, 0, 2);
		img.setPixel(6, 0, 6);
		img.setPixel(8, 0, 5);
		img.setPixel(1, 0, 3);
		img.setPalette(new long[11]);
	}
	
	
	@Test public void testCreateCopy() {
		var img = new BufferedPaletteImage(new PaletteImage() {
			public int getWidth() { return 7; }
			public int getHeight() { return 4; }
			public int[] getBitDepths() { return new int[]{4, 3, 2, 0}; }
			public long[] getPalette() { return new long[getWidth() * getHeight()]; }
			public int getPixel(int x, int y) { return y * getWidth() + x; }
		});
		assertEquals(7, img.getWidth());
		assertEquals(4, img.getHeight());
		assertArrayEquals(new int[]{4, 3, 2, 0}, img.getBitDepths());
		assertEquals( 0, img.getPixel(0, 0));
		assertEquals( 1, img.getPixel(1, 0));
		assertEquals( 3, img.getPixel(3, 0));
		assertEquals( 7, img.getPixel(0, 1));
		assertEquals( 9, img.getPixel(2, 1));
		assertEquals(21, img.getPixel(0, 3));
		assertEquals( 6, img.getPixel(6, 0));
		assertEquals(27, img.getPixel(6, 3));
	}
	
	
	@Test public void testClone() {
		var img0 = new BufferedPaletteImage(2, 1, DEFAULT_BIT_DEPTHS, new long[4]);
		img0.setPixel(0, 0, 1);
		img0.setPixel(1, 0, 0);
		var img1 = img0.clone();
		assertEquals(1, img0.getPixel(0, 0));
		assertEquals(0, img0.getPixel(1, 0));
		assertEquals(1, img1.getPixel(0, 0));
		assertEquals(0, img1.getPixel(1, 0));
		
		img0.setPixel(0, 0, 2);
		assertEquals(2, img0.getPixel(0, 0));
		assertEquals(0, img0.getPixel(1, 0));
		assertEquals(1, img1.getPixel(0, 0));
		assertEquals(0, img1.getPixel(1, 0));
		
		img1.setPixel(1, 0, 3);
		assertEquals(2, img0.getPixel(0, 0));
		assertEquals(0, img0.getPixel(1, 0));
		assertEquals(1, img1.getPixel(0, 0));
		assertEquals(3, img1.getPixel(1, 0));
	}
	
	
	private static final int[] DEFAULT_BIT_DEPTHS = new int[]{8, 8, 8, 0};
	
	private static Random rand = new Random();
	
}
