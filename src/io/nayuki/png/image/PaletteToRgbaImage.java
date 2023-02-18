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
 * Adapts a paletted image to a red-green-blue-alpha image, read-only.
 */
public final class PaletteToRgbaImage implements RgbaImage {
	
	private final PaletteImage image;
	private final long[] palette;
	
	
	public PaletteToRgbaImage(PaletteImage img) {
		image = Objects.requireNonNull(img);
		palette = image.getPalette();
	}
	
	
	@Override public int getWidth() {
		return image.getWidth();
	}
	
	
	@Override public int getHeight() {
		return image.getHeight();
	}
	
	
	@Override public int[] getBitDepths() {
		return image.getBitDepths();
	}
	
	
	@Override public long getPixel(int x, int y) {
		return palette[image.getPixel(x, y)];
	}
	
}
