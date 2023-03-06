/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.chunk.Ihdr;


abstract class Interlacer {
	
	protected final Ihdr ihdr;
	
	
	protected Interlacer(Ihdr ihdr) {
		this.ihdr = Objects.requireNonNull(ihdr);
	}
	
	
	protected void doInterlace() throws IOException {
		int xStep = switch (ihdr.interlaceMethod()) {
			case NONE  -> 1;
			case ADAM7 -> 8;
		};
		int yStep = xStep;
		handleSubimage(0, 0, xStep, yStep);
		while (yStep > 1) {
			if (xStep == yStep) {
				handleSubimage(xStep / 2, 0, xStep, yStep);
				xStep /= 2;
			} else if (xStep == yStep / 2) {
				handleSubimage(0, xStep, xStep, yStep);
				yStep = xStep;
			} else
				throw new AssertionError("Unreachable value");
		}
	}
	
	
	private void handleSubimage(int xOffset, int yOffset, int xStep, int yStep) throws IOException {
		int subwidth  = Math.ceilDiv(ihdr.width () - xOffset, xStep);
		int subheight = Math.ceilDiv(ihdr.height() - yOffset, yStep);
		if (subwidth > 0 && subheight > 0)
			handleSubimage(xOffset, yOffset, xStep, yStep, subwidth, subheight);
	}
	
	
	protected abstract void handleSubimage(int xOffset, int yOffset, int xStep, int yStep, int subwidth, int subheight) throws IOException;
	
}
