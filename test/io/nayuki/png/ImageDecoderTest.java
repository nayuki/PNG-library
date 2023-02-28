package io.nayuki.png;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;
import org.junit.Test;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.chunk.Plte;
import io.nayuki.png.image.GrayImage;
import io.nayuki.png.image.PaletteImage;
import io.nayuki.png.image.RgbaImage;


public final class ImageDecoderTest {
	
	@Test public void testFiltersGray() throws IOException {
		var png = new PngImage();
		png.ihdr = Optional.of(new Ihdr(8, 5, 8, Ihdr.ColorType.GRAYSCALE,
			Ihdr.CompressionMethod.ZLIB_DEFLATE, Ihdr.FilterMethod.ADAPTIVE, Ihdr.InterlaceMethod.NONE));
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(TestUtil.hexToBytes(
				"00 A9 81 FD A3 D0 57 8C 52" +
				"01 9D 00 01 FF 2B 3A 6C 6A" +
				"02 46 CD 1F FF 00 01 3F 79" +
				"03 FF 74 01 BA 80 30 86 00" +
				"04 00 E5 DE 25 FF 24 CA 01"));
		}
		png.idats.add(new Idat(bout.toByteArray()));
		
		var img = (GrayImage)ImageDecoder.toImage(png);
		int[] expect = {
			0xA9, 0x81, 0xFD, 0xA3, 0xD0, 0x57, 0x8C, 0x52,
			0x9D, 0x9D, 0x9E, 0x9D, 0xC8, 0x02, 0x6E, 0xD8,
			0xE3, 0x6A, 0xBD, 0x9C, 0xC8, 0x03, 0xAD, 0x51,
			0x70, 0xE1, 0xD0, 0x70, 0x1C, 0x3F, 0xFC, 0xA6,
			0x70, 0xC6, 0xA4, 0x95, 0x1B, 0x63, 0xC6, 0xA7,
		};
		for (int y = 0, i = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++, i++)
				assertEquals(expect[i] << 16, img.getPixel(x, y));
		}
	}
	
	
	@Test public void testFiltersRgba() throws IOException {
		var png = new PngImage();
		png.ihdr = Optional.of(new Ihdr(3, 5, 8, Ihdr.ColorType.TRUE_COLOR_WITH_ALPHA,
			Ihdr.CompressionMethod.ZLIB_DEFLATE, Ihdr.FilterMethod.ADAPTIVE, Ihdr.InterlaceMethod.NONE));
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(TestUtil.hexToBytes(
				"04 E2 FB 10 91 D2 F8 98 A9 CE 08 A0 4C" +
				"02 ED 4A CA 9E A8 8E 20 79 1A 81 9F 77" +
				"03 84 39 95 55 52 49 C0 EB 26 CC 3B 44" +
				"00 95 19 9E B1 9D FF EB D2 0E BF 22 EB" +
				"01 62 32 98 0D BA B7 70 E4 0B ED D1 2B"));
		}
		png.idats.add(new Idat(bout.toByteArray()));
		
		var img = (RgbaImage)ImageDecoder.toImage(png);
		int[] expect = {
			0xE2FB1091, 0xB4F3A83A, 0x82FB4886,
			0xCF45DA2F, 0x5C81C8B3, 0x9C7CE7FD,
			0xEB5B026C, 0xF5B7257A, 0xEE65C1FF,
			0x95199EB1, 0x9DFFEBD2, 0x0EBF22EB,
			0x6232980D, 0x1CE908F1, 0x27D6D91C,
		};
		for (int y = 0, i = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++, i++) {
				long temp =
					((expect[i] >>> 24) & 0xFFL) << 48 |
					((expect[i] >>> 16) & 0xFFL) << 32 |
					((expect[i] >>>  8) & 0xFFL) << 16 |
					((expect[i] >>>  0) & 0xFFL) <<  0;
				assertEquals(temp, img.getPixel(x, y));
			}
		}
	}
	
	
	@Test public void testFiltersPalette() throws IOException {
		var png = new PngImage();
		png.ihdr = Optional.of(new Ihdr(8, 5, 4, Ihdr.ColorType.INDEXED_COLOR,
			Ihdr.CompressionMethod.ZLIB_DEFLATE, Ihdr.FilterMethod.ADAPTIVE, Ihdr.InterlaceMethod.NONE));
		png.afterIhdr.add(new Plte(new byte[16 * 3]));
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(TestUtil.hexToBytes(
				"03 AF A2 EE 34" +
				"00 FC A1 E5 F3" +
				"04 3E A3 28 31" +
				"02 59 B6 E5 1F" +
				"01 BA 69 89 B2"));
		}
		png.idats.add(new Idat(bout.toByteArray()));
		
		var img = (PaletteImage)ImageDecoder.toImage(png);
		int[] expect = {
			0xA, 0xF, 0xF, 0x9, 0x6, 0xA, 0x6, 0x9,
			0xF, 0xC, 0xA, 0x1, 0xE, 0x5, 0xF, 0x3,
			0x3, 0xA, 0xD, 0xD, 0x0, 0xD, 0x3, 0xE,
			0x9, 0x3, 0x9, 0x3, 0xF, 0x2, 0x5, 0xD,
			0xB, 0xA, 0x2, 0x3, 0xA, 0xC, 0x5, 0xE,
		};
		for (int y = 0, i = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++, i++)
				assertEquals(expect[i], img.getPixel(x, y));
		}
	}
	
	
	@Test public void testInterlaceGray() throws IOException {
		var png = new PngImage();
		png.ihdr = Optional.of(new Ihdr(7, 11, 8, Ihdr.ColorType.GRAYSCALE,
			Ihdr.CompressionMethod.ZLIB_DEFLATE, Ihdr.FilterMethod.ADAPTIVE, Ihdr.InterlaceMethod.ADAM7));
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(TestUtil.hexToBytes(
				"00 AB  00 3B" +
				"00 C5  00 65" +
				"00 88 F5" +
				"00 1D 70  00 03 29  00 CD 04" +
				"00 EF 78 A8 AD  00 62 3F 17 BC  00 88 38 CC A5" +
				"00 5F EB 31  00 9E AC E0  00 E7 63 00  00 4E CE A7  00 ED 54 B0  00 82 CC F9" +
				"00 E5 4A 18 F9 7F F6 39  00 E3 61 25 98 89 FB C1  00 09 3B 30 A0 D3 E1 87  00 42 68 4F 55 D8 D1 23  00 89 49 2B 77 EA B8 8C"));
		}
		png.idats.add(new Idat(bout.toByteArray()));
		
		var img = (GrayImage)ImageDecoder.toImage(png);
		int[] expect = {
			0xAB, 0x5F, 0x1D, 0xEB, 0xC5, 0x31, 0x70,
			0xE5, 0x4A, 0x18, 0xF9, 0x7F, 0xF6, 0x39,
			0xEF, 0x9E, 0x78, 0xAC, 0xA8, 0xE0, 0xAD,
			0xE3, 0x61, 0x25, 0x98, 0x89, 0xFB, 0xC1,
			0x88, 0xE7, 0x03, 0x63, 0xF5, 0x00, 0x29,
			0x09, 0x3B, 0x30, 0xA0, 0xD3, 0xE1, 0x87,
			0x62, 0x4E, 0x3F, 0xCE, 0x17, 0xA7, 0xBC,
			0x42, 0x68, 0x4F, 0x55, 0xD8, 0xD1, 0x23,
			0x3B, 0xED, 0xCD, 0x54, 0x65, 0xB0, 0x04,
			0x89, 0x49, 0x2B, 0x77, 0xEA, 0xB8, 0x8C,
			0x88, 0x82, 0x38, 0xCC, 0xCC, 0xF9, 0xA5,
		};
		for (int y = 0, i = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++, i++)
				assertEquals(expect[i] << 16, img.getPixel(x, y));
		}
	}
	
	
	@Test public void testInterlaceRgba() throws IOException {
		var png = new PngImage();
		png.ihdr = Optional.of(new Ihdr(4, 3, 8, Ihdr.ColorType.TRUE_COLOR,
			Ihdr.CompressionMethod.ZLIB_DEFLATE, Ihdr.FilterMethod.ADAPTIVE, Ihdr.InterlaceMethod.ADAM7));
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(TestUtil.hexToBytes(
				"00 72EA2F" +
				"" +
				"" +
				"00 D852A9" +
				"00 ADB170 BC4A2E" +
				"00 ACD46C E8E3F6  00 429EFF 7A9255" +
				"00 1D2399 A5608B 212B4A 3F8283"));
		}
		png.idats.add(new Idat(bout.toByteArray()));
		
		var img = (RgbaImage)ImageDecoder.toImage(png);
		int[] expect = {
			0x72EA2F, 0xACD46C, 0xD852A9, 0xE8E3F6,
			0x1D2399, 0xA5608B, 0x212B4A, 0x3F8283,
			0xADB170, 0x429EFF, 0xBC4A2E, 0x7A9255,
		};
		for (int y = 0, i = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++, i++) {
				long temp =
					((expect[i] >>> 16) & 0xFFL) << 48 |
					((expect[i] >>>  8) & 0xFFL) << 32 |
					((expect[i] >>>  0) & 0xFFL) << 16;
				assertEquals(temp, img.getPixel(x, y));
			}
		}
	}
	
	
	@Test public void testInterlacePalette() throws IOException {
		var png = new PngImage();
		png.ihdr = Optional.of(new Ihdr(24, 18, 2, Ihdr.ColorType.INDEXED_COLOR,
			Ihdr.CompressionMethod.ZLIB_DEFLATE, Ihdr.FilterMethod.ADAPTIVE, Ihdr.InterlaceMethod.ADAM7));
		png.afterIhdr.add(new Plte(new byte[4 * 3]));
		
		var bout = new ByteArrayOutputStream();
		try (var dout = new DeflaterOutputStream(bout)) {
			dout.write(TestUtil.hexToBytes(
				"00 30  00 C4  00 74" +
				"00 BC  00 F4  00 20" +
				"00 06 B0  00 3F A0" +
				"00 57 00  00 C8 50  00 F6 30  00 9D 40  00 58 00" +
				"00 57 F7 1E  00 03 44 3C  00 44 E5 29  00 33 05 8F" +
				"00 8A 1E E4  00 30 09 91  00 BF 9D 3F  00 98 97 9A  00 E4 62 D4  00 CE 37 0E  00 0D 0F 12  00 01 2B 24  00 42 29 70" +
				"00 BC 0F E5 00 B1 62  00 97 51 1E 8C F7 2C  00 A6 8B 0A F8 81 73  00 1D 67 1D 1D 72 9B  00 7D 68 91 C8 4B AE  00 73 49 D5 D1 2B B1  00 73 5D 9E F1 9B 96  00 10 C6 B6 1E 89 C8  00 9A 4C 51 93 1A 06"));
		}
		png.idats.add(new Idat(bout.toByteArray()));
		
		var img = (PaletteImage)ImageDecoder.toImage(png);
		int[] expect = {
			0, 2, 1, 0, 2, 2, 1, 2, 3, 0, 1, 1, 3, 3, 3, 2, 0, 3, 0, 2, 3, 1, 0, 0,
			2, 3, 3, 0, 0, 0, 3, 3, 3, 2, 1, 1, 0, 0, 0, 0, 2, 3, 0, 1, 1, 2, 0, 2,
			1, 0, 1, 3, 1, 0, 3, 0, 3, 0, 3, 0, 1, 2, 3, 1, 0, 2, 1, 1, 3, 0, 2, 1,
			2, 1, 1, 3, 1, 1, 0, 1, 0, 1, 3, 2, 2, 0, 3, 0, 3, 3, 1, 3, 0, 2, 3, 0,
			0, 2, 3, 3, 0, 3, 0, 3, 1, 2, 2, 1, 2, 3, 0, 1, 2, 0, 1, 3, 3, 3, 1, 3,
			2, 2, 1, 2, 2, 0, 2, 3, 0, 0, 2, 2, 3, 3, 2, 0, 2, 0, 0, 1, 1, 3, 0, 3,
			0, 2, 0, 1, 0, 2, 3, 0, 1, 2, 0, 1, 1, 1, 0, 3, 0, 2, 3, 1, 3, 2, 0, 2,
			0, 1, 3, 1, 1, 2, 1, 3, 0, 1, 3, 1, 0, 1, 3, 1, 1, 3, 0, 2, 2, 1, 2, 3,
			3, 3, 3, 2, 3, 1, 3, 0, 0, 1, 1, 2, 3, 0, 2, 2, 1, 3, 0, 1, 1, 1, 3, 0,
			1, 3, 3, 1, 1, 2, 2, 0, 2, 1, 0, 1, 3, 0, 2, 0, 1, 0, 2, 3, 2, 2, 3, 2,
			1, 3, 0, 0, 1, 3, 0, 2, 3, 0, 2, 3, 1, 1, 1, 3, 0, 0, 2, 0, 2, 3, 1, 2,
			1, 3, 0, 3, 1, 0, 2, 1, 3, 1, 1, 1, 3, 1, 0, 1, 0, 2, 2, 3, 2, 3, 0, 1,
			0, 0, 2, 0, 3, 3, 1, 1, 3, 0, 3, 0, 3, 3, 1, 3, 2, 0, 1, 1, 2, 0, 0, 2,
			1, 3, 0, 3, 1, 1, 3, 1, 2, 1, 3, 2, 3, 3, 0, 1, 2, 1, 2, 3, 2, 1, 1, 2,
			0, 0, 3, 0, 0, 0, 3, 1, 0, 0, 0, 2, 1, 2, 1, 3, 2, 0, 0, 2, 3, 1, 3, 0,
			0, 1, 0, 0, 3, 0, 1, 2, 2, 3, 1, 2, 0, 1, 3, 2, 2, 0, 2, 1, 3, 0, 2, 0,
			1, 1, 1, 0, 0, 0, 1, 2, 3, 0, 2, 2, 2, 2, 0, 1, 1, 1, 0, 3, 0, 0, 0, 0,
			2, 1, 2, 2, 1, 0, 3, 0, 1, 1, 0, 1, 2, 1, 0, 3, 0, 1, 2, 2, 0, 0, 1, 2,
		};
		for (int y = 0, i = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++, i++)
				assertEquals(expect[i], img.getPixel(x, y));
		}
	}
	
}
