/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.image;


/**
 * A paletted (indexed-color) image that can be read. Implementations can be mutable or immutable.
 * Implementations can explicitly store pixel values in memory or implicitly compute them when requested.
 */
public interface PaletteImage {
	
	/**
	 * Returns the width of this image, a positive number.
	 * @return the width of this image
	 */
	public int getWidth();
	
	
	/**
	 * Returns the height of this image, a positive number.
	 * @return the height of this image
	 */
	public int getHeight();
	
	
	/**
	 * Returns the palette of this image, with length between 1 and 256 (inclusive).
	 * Each entry equals {@code (red << 48 | green << 32 | blue << 16 | alpha << 0)},
	 * where {@code red} is in the range [0, 2<sup>8</sup>), etc.
	 * @return the palette of this image (not {@code null})
	 */
	public long[] getPalette();
	
	
	/**
	 * Returns the palette index of the pixel at the specified coordinates. The top left corner
	 * has the coordinates (0, 0), positive {@code x} is right, and positive {@code y} is down.
	 * The returned value is in the range [0, <code>getPalette().length</code>).
	 * @param x the <var>x</var> coordinate of the pixel to get, in the range [0, {@code getWidth()})
	 * @param y the <var>y</var> coordinate of the pixel to get, in the range [0, {@code getHeight()})
	 * @return the palette index of the pixel
	 * @throws IndexOutOfBoundsException if the (<var>x</var>, <var>y</var>) coordinates are out of bounds
	 */
	public int getPixel(int x, int y);
	
}
