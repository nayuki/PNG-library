/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Iend;
import io.nayuki.png.chunk.Ihdr;


/**
 * A structured representation of chunks that form a PNG file.
 * These objects are mutable. There is some degree of validation and exclusion of invalid data.
 */
public final class PngImage {
	
	public static PngImage read(File inFile) throws IOException {
		try (var in = new BufferedInputStream(new FileInputStream(inFile))) {
			return read(in);
		}
	}
	
	
	public static PngImage read(InputStream in) throws IOException {
		XngFile xng = XngFile.read(in, true);
		if (xng.type() != XngFile.Type.PNG)
			throw new IllegalArgumentException();
		return new PngImage(xng.chunks());
	}
	
	
	public Optional<Ihdr> ihdr = Optional.empty();
	
	public List<Chunk> beforeIdats = new ArrayList<>();
	
	public List<Idat> idats = new ArrayList<>();
	
	public List<Chunk> afterIdats = new ArrayList<>();
	
	
	public PngImage() {}
	
	
	private PngImage(List<Chunk> chunks) {
		boolean hasIend = false;
		for (Chunk chunk : chunks) {
			if (hasIend)
				throw new IllegalArgumentException();
			else if (chunk instanceof Iend)
				hasIend = true;
			else if (ihdr.isEmpty()) {
				if (chunk instanceof Ihdr chk)
					ihdr = Optional.of(chk);
				else
					throw new IllegalArgumentException();
			} else if (chunk instanceof Ihdr)
				throw new IllegalArgumentException();
			else if (chunk instanceof Idat chk) {
				if (afterIdats.isEmpty())
					idats.add(chk);
				else
					throw new IllegalArgumentException();
			} else if (idats.isEmpty())
				beforeIdats.add(chunk);
			else
				afterIdats.add(chunk);
		}
		if (ihdr.isEmpty() || idats.isEmpty() || !hasIend)
			throw new IllegalArgumentException();
	}
	
	
	public void write(File outFile) throws IOException {
		try (var out = new BufferedOutputStream(new FileOutputStream(outFile))) {
			write(out);
		}
	}
	
	
	public void write(OutputStream out) throws IOException {
		List<Chunk> chunks = new ArrayList<>();
		chunks.add(ihdr.get());
		chunks.addAll(beforeIdats);
		chunks.addAll(idats);
		chunks.addAll(afterIdats);
		chunks.add(Iend.SINGLETON);
		new XngFile(XngFile.Type.PNG, chunks).write(out);
	}
	
}
