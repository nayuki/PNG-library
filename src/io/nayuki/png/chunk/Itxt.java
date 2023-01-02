/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/
 */

package io.nayuki.png.chunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A international textual data (iTXt) chunk. This contains a keyword (ISO 8859-1), compression parameters,
 * language tag (ISO 8859-1), translated keyword (UTF-8), and text (UTF-8 with optional compression).
 * Instances should be treated as immutable, but arrays are not copied defensively.
 * @see https://www.w3.org/TR/2003/REC-PNG-20031110/#11iTXt
 */
public record Itxt(
		String keyword,
		boolean compressionFlag,
		CompressionMethod compressionMethod,
		String languageTag,
		String translatedKeyword,
		byte[] text)
	implements Chunk {
	
	
	static final String TYPE = "iTXt";
	
	
	/*---- Constructor ----*/
	
	public Itxt {
		checkString(keyword);
		if (!(1 <= keyword.length() && keyword.length() <= 79) ||
				keyword.startsWith(" ") || keyword.endsWith(" ") || keyword.contains("  ") || keyword.contains("\n"))
			throw new IllegalArgumentException();
		
		checkString(languageTag);
		
		Objects.requireNonNull(translatedKeyword);
		for (int i = 0; i < translatedKeyword.length(); i++) {
			if (translatedKeyword.charAt(i) == '\0')
				throw new IllegalArgumentException();
		}
		
		if (compressionFlag) {
			switch (compressionMethod) {
				case DEFLATE:
					try {
						Util.decompressZlibDeflate(text);
					} catch (IOException e) {
						throw new IllegalArgumentException(e);
					}
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else if (compressionMethod != CompressionMethod.DEFLATE)
			throw new IllegalArgumentException();
		
		if (5L + keyword.length() + languageTag.length() + text.length +
				translatedKeyword.getBytes(StandardCharsets.UTF_8).length > Integer.MAX_VALUE)
			throw new IllegalArgumentException();
	}
	
	
	private static void checkString(String s) {
		Objects.requireNonNull(s);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!(c == '\n' || 32 <= c && c <= 126 || 161 <= c && c <= 255))
				throw new IllegalArgumentException();
		}
	}
	
	
	public static Itxt read(int dataLen, DataInput in) throws IOException {
		var data = new byte[dataLen];
		in.readFully(data);
		
		int start = 0;
		int nextNull = nextNull(data, start);
		String keyword = new String(Arrays.copyOfRange(data, start, nextNull), StandardCharsets.ISO_8859_1);
		start = nextNull + 1;
		
		if (data.length - start < 2)
			throw new IllegalArgumentException();
		boolean compFlag = switch (data[start + 0]) {
			case 0 -> false;
			case 1 -> true;
			default -> throw new IllegalArgumentException();
		};
		CompressionMethod compMethod = Util.indexInto(CompressionMethod.values(), data[start + 1]);
		start += 2;
		
		nextNull = nextNull(data, start);
		String languageTag = new String(Arrays.copyOfRange(data, start, nextNull), StandardCharsets.ISO_8859_1);
		start = nextNull + 1;
		
		nextNull = nextNull(data, start);
		String translatedKeyword = new String(Arrays.copyOfRange(data, start, nextNull), StandardCharsets.UTF_8);
		start = nextNull + 1;
		
		byte[] text = Arrays.copyOfRange(data, start, data.length);
		
		return new Itxt(keyword, compFlag, compMethod, languageTag, translatedKeyword, text);
	}
	
	
	private static int nextNull(byte[] data, int start) {
		while (true) {
			if (start >= data.length)
				throw new IllegalArgumentException();
			else if (data[start] == 0)
				return start;
			else
				start++;
		}
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		return 5 + keyword.length() + languageTag.length() + text.length
			+ translatedKeyword.getBytes(StandardCharsets.UTF_8).length;
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(keyword.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeByte(compressionFlag ? 1 : 0);
		out.writeByte(compressionMethod.ordinal());
		out.write(languageTag.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.write(translatedKeyword.getBytes(StandardCharsets.UTF_8));
		out.writeByte(0);
		out.write(text);
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum CompressionMethod {
		DEFLATE,
	}
	
}
