/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.image;

import java.util.Objects;


/**
 * A mutable paletted image where all pixels are stored in memory.
 */
public final class BufferedPaletteImage implements PaletteImage, Cloneable {
	
	private final int width;
	private final int height;
	private int[] bitDepths;
	private final long illegalOnes;
	private long[] palette;
	private byte[] pixels;
	
	
	/**
	 * Constructs an all-zero image with the specified dimensions, channel bit depths
	 * of the palette, and initial palette. {@code bitDepths} is a length-4 array:
	 * <ul>
	 *   <li>Index 0: Red channel bit depth, in the range [1, 8]</li>
	 *   <li>Index 1: Green channel bit depth, in the range [1, 8]</li>
	 *   <li>Index 2: Blue channel bit depth, in the range [1, 8]</li>
	 *   <li>Index 3: Alpha channel bit depth, either 0 or 8, where 0 means all pixels are opaque</li>
	 * </ul>
	 * <p>The dimensions and bit depths are immutable after construction;
	 * only the pixel values and palette can be modified.</p>
	 * @param width the width of the image, a positive number
	 * @param height the height of the image, a positive number
	 * @param bitDepths the bit depths of the channels of the palette (not {@code null})
	 * @param pal the initial palette of the image (not {@code null})
	 * @throws NullPointerException if {@code pal} is {@code null}
	 * @throws IllegalArgumentException if the width, height, or palette length is out of range,
	 * or any of the channel sample values in the palette are outside of their bit depth
	 * @throws ArithmeticException if {@code width * height > Integer.MAX_VALUE}
	 */
	public BufferedPaletteImage(int width, int height, int[] bitDepths, long[] pal) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Non-positive dimensions");
		this.width = width;
		this.height = height;
		
		Objects.requireNonNull(bitDepths);
		bitDepths = bitDepths.clone();
		if (bitDepths.length != 4)
			throw new IllegalArgumentException("Invalid bit depth array length");
		for (int i = 0; i < bitDepths.length; i++) {
			int bits = bitDepths[i];
			if (i < bitDepths.length - 1 && 1 <= bits && bits <= 8);
			else if (i == bitDepths.length - 1 && (bits == 0 || bits == 8));
			else  throw new IllegalArgumentException("Invalid bit depths");
		}
		this.bitDepths = bitDepths;
		
		long temp = 0;
		for (int numBits : bitDepths) {
			temp <<= 16;
			temp |= 0x10000 - (1 << numBits);
		}
		illegalOnes = temp;
		
		setPalette(pal);
		pixels = new byte[Math.multiplyExact(width, height)];
	}
	
	
	@Override public int getWidth() {
		return width;
	}
	
	
	@Override public int getHeight() {
		return height;
	}
	
	
	@Override public long[] getPalette() {
		return palette.clone();
	}
	
	
	@Override public int[] getBitDepths() {
		return bitDepths.clone();
	}
	
	
	/**
	 * Sets this image's palette to the specified array. Each entry equals {@code (red << 48 | green
	 * << 32 | blue << 16 | alpha << 0)}, where {@code red} is in the range [0, 2<sup>bitDepths[0]</sup>),
	 * etc. The color indexes of existing pixels will not be changed, so the effective colors of pixels
	 * will change according to the new palette. If the new palette is shorter than the existing
	 * one and any pixel has an out-of-bounds index for the new palette, an exception is thrown.
	 * @param pal the new palette to set to (not {@code null})
	 * @throws NullPointerException if the new palette is {@code null}
	 * @throws IllegalArgumentException if {@code pal.length} is not in the range
	 * [1, 256], or any pixel is not in the range [0, {@code pal.length}), or any
	 * of the channel sample values in {@code pal} are outside of their bit depth
	 */
	public void setPalette(long[] pal) {
		Objects.requireNonNull(pal);
		if (!(1 <= pal.length && pal.length <= 256))
			throw new IllegalArgumentException("Invalid palette size");
		for (long val : pal) {
			if ((val & illegalOnes) != 0)
				throw new IllegalArgumentException("Invalid palette value");
		}
		if (palette != null && pal.length < palette.length) {
			for (byte val : pixels) {
				if ((val & 0xFF) >= pal.length)
					throw new IllegalArgumentException("A pixel's palette index exceeds bounds of new palette");
			}
		}
		palette = pal.clone();
	}
	
	
	@Override public int getPixel(int x, int y) {
		return pixels[getIndex(x, y)] & 0xFF;
	}
	
	
	/**
	 * Sets the pixel at the specified coordinates to the specified value.
	 * @param x x the <var>x</var> coordinate of the pixel to set, in the range [0, {@code getWidth()})
	 * @param y y the <var>y</var> coordinate of the pixel to set, in the range [0, {@code getHeight()})
	 * @param val the new palette index of the pixel
	 * @throws IndexOutOfBoundsException if the (<var>x</var>, <var>y</var>) coordinates are out
	 * of bounds, or the new palette index is not in the range [0, {@code getPalette().length})
	 */
	public void setPixel(int x, int y, int val) {
		if (!(0 <= val && val < palette.length))
			throw new IllegalArgumentException("Invalid sample value");
		pixels[getIndex(x, y)] = (byte)val;
	}
	
	
	private int getIndex(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height)
			return y * width + x;
		else {
			throw new IndexOutOfBoundsException(String.format(
				"(x,y) = (%d,%d); (width,height) = (%d,%d)", x, y, width, height));
		}
	}
	
	
	@Override public BufferedPaletteImage clone() {
		try {
			var result = (BufferedPaletteImage)super.clone();
			result.palette = result.palette.clone();
			result.pixels = result.pixels.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("Caught impossible exception", e);
		}
	}
	
}
