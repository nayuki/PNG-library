/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.image;


/**
 * A red-green-blue-alpha image that can be read. Implementations can be mutable or immutable.
 * Implementations can explicitly store pixel values in memory or implicitly compute them when requested.
 */
public interface RgbaImage {
	
	public int getWidth();
	
	
	public int getHeight();
	
	
	public int[] getBitDepths();
	
	
	public long getPixel(int x, int y);
	
}
