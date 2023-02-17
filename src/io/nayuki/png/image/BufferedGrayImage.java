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
 * A mutable grayscale-alpha image where all pixels are stored in memory.
 */
public final class BufferedGrayImage implements GrayImage, Cloneable {
	
	private final int width;
	private final int height;
	private int[] bitDepths;
	private final int illegalOnes;
	private int[] pixels;
	
	
	/**
	 * Constructs an all-zero image with the specified dimensions and
	 * channel bit depths. {@code bitDepths} is a length-2 array:
	 * <ul>
	 *   <li>Index 0: White channel bit depth, in the range [1, 16]</li>
	 *   <li>Index 1: Alpha channel bit depth, in the range [0, 16], where 0 means all pixels are opaque</li>
	 * </ul>
	 * <p>The dimensions and bit depths are immutable after
	 * construction; only the pixel values can be modified.</p>
	 * @param width the width of the image, a positive number
	 * @param height the height of the image, a positive number
	 * @param bitDepths the bit depths of the channels of the image (not {@code null})
	 * @throws NullPointerException if {@code bitDepths} is {@code null}
	 * @throws IllegalArgumentException if the width, height, or bit depths are out of range
	 * @throws ArithmeticException if {@code width * height > Integer.MAX_VALUE}
	 */
	public BufferedGrayImage(int width, int height, int[] bitDepths) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException("Non-positive dimensions");
		this.width = width;
		this.height = height;
		
		Objects.requireNonNull(bitDepths);
		bitDepths = bitDepths.clone();
		if (bitDepths.length != 2)
			throw new IllegalArgumentException("Invalid bit depth array length");
		for (int i = 0; i < bitDepths.length; i++) {
			if (!((i == bitDepths.length - 1 ? 0 : 1) <= bitDepths[i] && bitDepths[i] <= 16))
				throw new IllegalArgumentException("Invalid bit depths");
		}
		this.bitDepths = bitDepths;
		
		int temp = 0;
		for (int numBits : bitDepths) {
			temp <<= 16;
			temp |= 0x10000 - (1 << numBits);
		}
		illegalOnes = temp;
		
		pixels = new int[Math.multiplyExact(width, height)];
	}
	
	
	@Override public int[] getBitDepths() {
		return bitDepths.clone();
	}
	
	
	@Override public int getWidth() {
		return width;
	}
	
	
	@Override public int getHeight() {
		return height;
	}
	
	
	@Override public int getPixel(int x, int y) {
		return pixels[getIndex(x, y)];
	}
	
	
	/**
	 * Sets the pixel at the specified coordinates to the specified value.
	 * @param x x the <var>x</var> coordinate of the pixel to set, in the range [0, {@code getWidth()})
	 * @param y y the <var>y</var> coordinate of the pixel to set, in the range [0, {@code getHeight()})
	 * @param val the new channel sample values of the pixel
	 * @throws IndexOutOfBoundsException if the (<var>x</var>, <var>y</var>) coordinates are out of bounds
	 * @throws IllegalArgumentException if any of the channel sample values are outside of their bit depth
	 */
	public void setPixel(int x, int y, int val) {
		if ((val & illegalOnes) != 0)
			throw new IllegalArgumentException("Invalid sample value");
		pixels[getIndex(x, y)] = val;
	}
	
	
	private int getIndex(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height)
			return y * width + x;
		else {
			throw new IndexOutOfBoundsException(String.format(
				"(x,y) = (%d,%d); (width,height) = (%d,%d)", x, y, width, height));
		}
	}
	
	
	@Override public BufferedGrayImage clone() {
		try {
			var result = (BufferedGrayImage)super.clone();
			result.bitDepths = result.bitDepths.clone();
			result.pixels = result.pixels.clone();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError("Caught impossible exception", e);
		}
	}
	
}
