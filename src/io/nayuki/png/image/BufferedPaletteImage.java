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
	private long[] palette;
	private byte[] pixels;
	
	
	/**
	 * Constructs an all-zero image with the specified dimensions and initial palette.
	 * @param width the width of the image, a positive number
	 * @param height the height of the image, a positive number
	 * @param pal the initial palette of the image (not {@code null})
	 * @throws NullPointerException if {@code pal} is {@code null}
	 * @throws IllegalArgumentException if the width, height, or palette length is out of range
	 * @throws ArithmeticException if {@code width * height > Integer.MAX_VALUE}
	 */
	public BufferedPaletteImage(int width, int height, long[] pal) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Non-positive dimensions");
		this.width = width;
		this.height = height;
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
	
	
	/**
	 * Sets this image's palette to the specified array. Each entry equals {@code (red << 24 | green
	 * << 16 | blue << 8 | alpha << 0)}, where {@code red} is in the range [0, 2<sup>8</sup>), etc.
	 * The color indexes of existing pixels will not be changed, so the effective colors of pixels
	 * will change according to the new palette. If the new palette is shorter than the existing
	 * one and any pixel has an out-of-bounds index for the new palette, an exception is thrown.
	 * @param pal the new palette to set to (not {@code null})
	 * @throws NullPointerException if the new palette is {@code null}
	 * @throws IllegalArgumentException if the new palette's length is not in the
	 * range [1, 256], or any pixel is not in the range [0, {@code pal.length})
	 */
	public void setPalette(long[] pal) {
		Objects.requireNonNull(pal);
		if (!(1 <= pal.length && pal.length <= 256))
			throw new IllegalArgumentException("Invalid palette size");
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
