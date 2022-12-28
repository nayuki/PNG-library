package io.nayuki.png.image;


public interface RgbaImage {
	
	public int getWidth();
	
	
	public int getHeight();
	
	
	public int[] getBitDepths();
	
	
	public long getPixel(int x, int y);
	
}
