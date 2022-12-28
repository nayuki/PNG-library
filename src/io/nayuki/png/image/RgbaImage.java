/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.image;


public interface RgbaImage {
	
	public int getWidth();
	
	
	public int getHeight();
	
	
	public int[] getBitDepths();
	
	
	public long getPixel(int x, int y);
	
}
