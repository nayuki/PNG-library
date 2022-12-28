package io.nayuki.png;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Iend;
import io.nayuki.png.chunk.Ihdr;


public final class PngImage {
	
	public Optional<Ihdr> ihdr = Optional.empty();
	
	public List<Chunk> beforeIdats = new ArrayList<>();
	
	public List<Idat> idats = new ArrayList<>();
	
	public List<Chunk> afterIdats = new ArrayList<>();
	
	
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
		RawPng.write(chunks, out);
	}
	
}
