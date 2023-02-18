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
 * Adapts a grayscale-alpha image to a red-green-blue-alpha image, read-only.
 */
public final class GrayToRgbaImage implements RgbaImage {
	
	/*---- Fields ----*/
	
	private final GrayImage image;
	
	
	/*---- Constructor ----*/
	
	public GrayToRgbaImage(GrayImage img) {
		image = Objects.requireNonNull(img);
	}
	
	
	/*---- Methods ----*/
	
	@Override public int getWidth() {
		return image.getWidth();
	}
	
	
	@Override public int getHeight() {
		return image.getHeight();
	}
	
	
	@Override public int[] getBitDepths() {
		int[] temp = image.getBitDepths();
		return new int[]{temp[0], temp[0], temp[0], temp[1]};
	}
	
	
	@Override public long getPixel(int x, int y) {
		int temp = image.getPixel(x, y);
		return ((temp >>> 16) * 0x0001000100010000L) | (temp & 0xFFFF);
	}
	
}
