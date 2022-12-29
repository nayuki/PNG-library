/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.image;


/**
 * A mutable RGBA image where all pixels are stored in memory.
 */
public final class BufferedRgbaImage implements RgbaImage {
	
	private final int width;
	private final int height;
	private final int[] bitDepths;
	private final long illegalOnes;
	private long[] pixels;
	
	
	public BufferedRgbaImage(int width, int height, int[] bitDepths) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException();
		this.width = width;
		this.height = height;
		
		bitDepths = bitDepths.clone();
		if (bitDepths.length != 4)
			throw new IllegalArgumentException();
		for (int i = 0; i < bitDepths.length; i++) {
			if (!((i == 3 ? 0 : 1) <= bitDepths[i] && bitDepths[i] <= 16))
				throw new IllegalArgumentException();
		}
		this.bitDepths = bitDepths;
		
		long temp = 0;
		for (int numBits : bitDepths) {
			temp <<= 16;
			temp |= ((1 << numBits) - 1) ^ 0xFFFF;
		}
		illegalOnes = temp;
		
		pixels = new long[Math.multiplyExact(width, height)];
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
	
	
	@Override public long getPixel(int x, int y) {
		return pixels[getIndex(x, y)];
	}
	
	
	public void setPixel(int x, int y, long val) {
		if ((val & illegalOnes) != 0)
			throw new IllegalArgumentException();
		pixels[getIndex(x, y)] = val;
	}
	
	
	private int getIndex(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height)
			return y * width + x;
		else
			throw new IndexOutOfBoundsException();
	}
	
}
