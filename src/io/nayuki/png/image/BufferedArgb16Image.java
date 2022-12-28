package io.nayuki.png.image;


public final class BufferedArgb16Image implements Argb16Image {
	
	private final int width;
	private final int height;
	private long[] pixels;
	
	
	public BufferedArgb16Image(int width, int height) {
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException();
		this.width = width;
		this.height = height;
		pixels = new long[Math.multiplyExact(width, height)];
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
		pixels[getIndex(x, y)] = val;
	}
	
	
	private int getIndex(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height)
			return y * width + x;
		else
			throw new IndexOutOfBoundsException();
	}
	
}
