package io.nayuki.png.chunk;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import io.nayuki.png.Chunk;


public record Srgb(RenderingIntent renderingIntent) implements Chunk {
	
	/*---- Constructor ----*/
	
	public Srgb {
		Objects.requireNonNull(renderingIntent);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return "sRGB";
	}
	
	
	@Override public int getDataLength() {
		return 1;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.writeByte(renderingIntent.ordinal());
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum RenderingIntent {
		PERCEPTUAL,
		RELITAVIE_COLORIMETRIC,
		SATURATION,
		ABSOLUTE_COLORIMETRIC,
	}
	
}
