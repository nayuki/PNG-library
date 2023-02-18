/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.chunk.Sbit;
import io.nayuki.png.chunk.Trns;
import io.nayuki.png.image.BufferedGrayImage;
import io.nayuki.png.image.BufferedPaletteImage;
import io.nayuki.png.image.BufferedRgbaImage;


/**
 * Decodes a {@link PngImage} object to a buffered image
 * where pixels can be directly read. Not instantiable.
 * @see ImageEncoder
 */
public abstract sealed class ImageDecoder permits
		ImageDecoder.RgbaImageDecoder, ImageDecoder.GrayImageDecoder, ImageDecoder.PaletteImageDecoder {
	
	/*---- Public function ----*/
	
	/**
	 * Decodes the specified PNG image to a new mutable buffered image. If the
	 * PNG's color type is true color, then a {@link BufferedRgbaImage} is
	 * returned. Else if the PNG's color type is grayscale, then a {@link
	 * BufferedGrayImage} is returned. Else the color type is unsupported.
	 * @param png the PNG image to decode (not {@code null})
	 * @return a new buffered image (not {@code null})
	 * @throws NullPointerException if {@code png} is {@code null}
	 * @throws IllegalArgumentException if the PNG image is malformed
	 * @throws UnsupportedOperationException if the PNG image's color type is not supported
	 */
	public static Object toImage(PngImage png) {
		// Check header chunk
		Objects.requireNonNull(png);
		Ihdr ihdr = png.ihdr.orElseThrow(() -> new IllegalArgumentException("Missing IHDR chunk"));
		if (ihdr.compressionMethod() != Ihdr.CompressionMethod.ZLIB_DEFLATE)
			throw new IllegalArgumentException("Unsupported compression method");
		if (ihdr.filterMethod() != Ihdr.FilterMethod.ADAPTIVE)
			throw new IllegalArgumentException("Unsupported filter method");
		
		// Decode image by color type
		return (switch (ihdr.colorType()) {
			case TRUE_COLOR, TRUE_COLOR_WITH_ALPHA -> new RgbaImageDecoder(png);
			case GRAYSCALE , GRAYSCALE_WITH_ALPHA  -> new GrayImageDecoder(png);
			case INDEXED_COLOR                     -> new PaletteImageDecoder(png);
		}).decode();
	}
	
	
	/*---- ImageDecoder instance members ----*/
	
	final PngImage png;
	final Ihdr ihdr;
	final int inBitDepth;
	Optional<Sbit> sbit = Optional.empty();
	Optional<Trns> trns = Optional.empty();
	
	
	ImageDecoder(PngImage png) {
		this.png = png;
		ihdr = png.ihdr.get();
		inBitDepth = ihdr.bitDepth();
		
		// Find 0 or 1 significant bits chunk
		for (Chunk chunk : png.afterIhdr) {
			if (chunk instanceof Sbit chk) {
				if (sbit.isPresent())
					throw new IllegalArgumentException("Duplicate sBIT chunk");
				sbit = Optional.of(chk);
			}
		}
		
		// Find 0 or 1 transparency chunk
		for (Chunk chunk : png.afterPlte) {
			if (chunk instanceof Trns chk) {
				if (trns.isPresent())
					throw new IllegalArgumentException("Duplicate tRNS chunk");
				trns = Optional.of(chk);
			}
		}
	}
	
	
	final Object decode() {
		// Virtually concatenate bytes from all data chunks, then decompress
		List<InputStream> ins = png.idats.stream()
			.map(idat -> new ByteArrayInputStream(idat.data()))
			.collect(Collectors.toList());
		var in0 = new SequenceInputStream(Collections.enumeration(ins));
		var in1 = new InflaterInputStream(in0);
		try (var in2 = new DataInputStream(in1)) {
			
			// Handle progressive and interlaced images
			int xStep = switch (ihdr.interlaceMethod()) {
				case NONE  -> 1;
				case ADAM7 -> 8;
			};
			int yStep = xStep;
			decodeSubimage(in2, 0, 0, xStep, yStep);
			while (yStep > 1) {
				if (xStep == yStep) {
					decodeSubimage(in2, xStep / 2, 0, xStep, yStep);
					xStep /= 2;
				} else {
					assert xStep == yStep / 2;
					decodeSubimage(in2, 0, xStep, xStep, yStep);
					yStep = xStep;
				}
			}
			
			if (in2.read() != -1)
				throw new IllegalArgumentException("Extra decompressed data after all pixels");
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return getResult();
	}
	
	
	abstract void decodeSubimage(DataInput din, int xOffset, int yOffset, int xStep, int yStep) throws IOException;
	
	
	abstract Object getResult();
	
	
	
	/*---- Helper class ----*/
	
	private static final class RowDecoder {
		
		private DataInput input;
		private int filterStride;
		private byte[] previousRow;
		private byte[] currentRow;
		
		
		public RowDecoder(DataInput in, int filterStride, int rowSizeBytes) {
			input = Objects.requireNonNull(in);
			if (filterStride <= 0)
				throw new IllegalArgumentException("Non-positive filter stride");
			this.filterStride = filterStride;
			if (rowSizeBytes <= 0)
				throw new IllegalArgumentException("Non-positive row size");
			previousRow = new byte[Math.addExact(rowSizeBytes, filterStride)];
			currentRow = previousRow.clone();
		}
		
		
		public byte[] readRow() throws IOException {
			// Swap buffers
			byte[] temp = currentRow;
			currentRow = previousRow;
			previousRow = temp;
			
			// Read all the necessary bytes
			int filter = input.readUnsignedByte();
			input.readFully(currentRow, filterStride, currentRow.length - filterStride);
			
			// Do un-filtering
			switch (filter) {
				case 0 -> {  // None
				}
				case 1 -> {  // Sub
					for (int i = filterStride; i < currentRow.length; i++)
						currentRow[i] += currentRow[i - filterStride];
				}
				case 2 -> {  // Up
					for (int i = filterStride; i < currentRow.length; i++)
						currentRow[i] += previousRow[i];
				}
				case 3 -> {  // Average
					for (int i = filterStride; i < currentRow.length; i++)
						currentRow[i] += ((currentRow[i - filterStride] & 0xFF) + (previousRow[i] & 0xFF)) >>> 1;
				}
				case 4 -> {  // Paeth
					for (int i = filterStride; i < currentRow.length; i++) {
						int a = currentRow[i - filterStride] & 0xFF;  // Left
						int b = previousRow[i] & 0xFF;  // Up
						int c = previousRow[i - filterStride] & 0xFF;  // Up left
						int p = a + b - c;
						int pa = Math.abs(p - a);
						int pb = Math.abs(p - b);
						int pc = Math.abs(p - c);
						int pr;
						if (pa <= pb && pa <= pc) pr = a;
						else if (pb <= pc) pr = b;
						else pr = c;
						currentRow[i] += pr;
					}
				}
				default -> throw new IllegalArgumentException("Unsupported filter type: " + filter);
			}
			return currentRow;
		}
		
	}
	
	
	
	/*---- A decoder subclass ----*/
	
	static final class RgbaImageDecoder extends ImageDecoder {
		
		private final long transparentColor;  // Either -1 or 0xRRRRGGGGBBBB0000
		private BufferedRgbaImage result;
		
		
		public RgbaImageDecoder(PngImage png) {
			super(png);
			
			// Handle significant bits
			int outRBits = inBitDepth, outGBits = inBitDepth, outBBits = inBitDepth,
				outABits = ihdr.colorType() == Ihdr.ColorType.TRUE_COLOR ? 0 : inBitDepth;
			if (sbit.isPresent()) {
				byte[] sb = sbit.get().data();
				if (sb.length != (outABits > 0 ? 4 : 3))
					throw new IllegalArgumentException("Invalid sBIT data length");
				if (sb[0] > outRBits || sb[1] > outGBits || sb[2] > outBBits || outABits > 0 && sb[3] > outABits)
					throw new IllegalArgumentException("Number of significant bits exceeds bit depth");
				outRBits = sb[0];
				outGBits = sb[1];
				outBBits = sb[2];
				if (outABits > 0)
					outABits = sb[3];
			}
			
			// Handle transparent color
			if (trns.isEmpty())
				transparentColor = -1;
			else {
				if (outABits > 0)
					throw new IllegalArgumentException("tRNS chunk disallowed in image with alpha channel");
				byte[] tb = trns.get().data();
				if (tb.length != 6)
					throw new IllegalArgumentException("Invalid tRNS data length");
				long transpColor = 0;
				for (byte b : tb)
					transpColor = (transpColor << 8) | (b & 0xFF);
				transparentColor = transpColor << 16;
				if (outRBits == inBitDepth && outGBits == inBitDepth && outBBits == inBitDepth)
					outABits = inBitDepth;
				else
					outABits = 1;
			}
			
			result = new BufferedRgbaImage(ihdr.width(), ihdr.height(), new int[]{outRBits, outGBits, outBBits, outABits});
		}
		
		
		@Override void decodeSubimage(DataInput din, int xOffset, int yOffset, int xStep, int yStep) throws IOException {
			int width  = Math.ceilDiv(result.getWidth () - xOffset, xStep);
			int height = Math.ceilDiv(result.getHeight() - yOffset, yStep);
			if (width == 0 || height == 0)
				return;
			
			int[] outBitDepths = result.getBitDepths();
			int rShift = inBitDepth - outBitDepths[0];
			int gShift = inBitDepth - outBitDepths[1];
			int bShift = inBitDepth - outBitDepths[2];
			int aShift = inBitDepth - outBitDepths[3];
			boolean hasAlpha = outBitDepths[3] > 0 && transparentColor == -1;
			int mode = (inBitDepth / 8 - 1) * 2 + (hasAlpha ? 1 : 0);
			
			int filterStride = Math.ceilDiv(inBitDepth * (hasAlpha ? 4 : 3), 8);
			var dec = new RowDecoder(din, filterStride,
				Math.toIntExact(Math.ceilDiv((long)width * inBitDepth * (hasAlpha ? 4 : 3), 8)));
			for (int y = 0; y < height; y++) {
				byte[] row = dec.readRow();
				
				for (int x = 0, i = filterStride; x < width; x++, i += filterStride) {
					int r, g, b, a;
					long temp;
					switch (mode) {
						case 0 -> {
							r = row[i + 0] & 0xFF;
							g = row[i + 1] & 0xFF;
							b = row[i + 2] & 0xFF;
							temp = (long)r << 48 | (long)g << 32 | (long)b << 16;
							a = temp != transparentColor ? 0xFF : 0x00;
						}
						case 1 -> {
							r = row[i + 0] & 0xFF;
							g = row[i + 1] & 0xFF;
							b = row[i + 2] & 0xFF;
							a = row[i + 3] & 0xFF;
						}
						case 2 -> {
							r = (row[i + 0] & 0xFF) << 8 | (row[i + 1] & 0xFF) << 0;
							g = (row[i + 2] & 0xFF) << 8 | (row[i + 3] & 0xFF) << 0;
							b = (row[i + 4] & 0xFF) << 8 | (row[i + 5] & 0xFF) << 0;
							temp = (long)r << 48 | (long)g << 32 | (long)b << 16;
							a = temp != transparentColor ? 0xFFFF : 0x00;
						}
						case 3 -> {
							r = (row[i + 0] & 0xFF) << 8 | (row[i + 1] & 0xFF) << 0;
							g = (row[i + 2] & 0xFF) << 8 | (row[i + 3] & 0xFF) << 0;
							b = (row[i + 4] & 0xFF) << 8 | (row[i + 5] & 0xFF) << 0;
							a = (row[i + 6] & 0xFF) << 8 | (row[i + 7] & 0xFF) << 0;
						}
						default -> throw new AssertionError("Unreachable value");
					}
					r >>>= rShift;
					g >>>= gShift;
					b >>>= bShift;
					a >>>= aShift;
					result.setPixel(xOffset + x * xStep, yOffset + y * yStep,
						(long)r << 48 | (long)g << 32 | (long)b << 16 | (long)a << 0);
				}
			}
		}
		
		
		@Override BufferedRgbaImage getResult() {
			return result;
		}
		
	}
	
	
	
	/*---- A decoder subclass ----*/
	
	static final class GrayImageDecoder extends ImageDecoder {
		
		private final int transparentColor;  // Either -1 or 0xWWWW0000
		private BufferedGrayImage result;
		
		
		public GrayImageDecoder(PngImage png) {
			super(png);
			
			// Handle significant bits
			int outWBits = inBitDepth, outABits = ihdr.colorType() == Ihdr.ColorType.GRAYSCALE ? 0 : inBitDepth;
			if (sbit.isPresent()) {
				byte[] sb = sbit.get().data();
				if (sb.length != (outABits > 0 ? 2 : 1))
					throw new IllegalArgumentException("Invalid sBIT data length");
				if (sb[0] > outWBits || outABits > 0 && sb[1] > outABits)
					throw new IllegalArgumentException("Number of significant bits exceeds bit depth");
				outWBits = sb[0];
				if (outABits > 0)
					outABits = sb[1];
			}
			
			// Handle transparent color
			if (trns.isEmpty())
				transparentColor = -1;
			else {
				if (outABits > 0)
					throw new IllegalArgumentException("tRNS chunk disallowed in image with alpha channel");
				byte[] tb = trns.get().data();
				if (tb.length != 2)
					throw new IllegalArgumentException("Invalid tRNS data length");
				int transpColor = 0;
				for (byte b : tb)
					transpColor = (transpColor << 8) | (b & 0xFF);
				transparentColor = transpColor << 16;
				if (outWBits == inBitDepth)
					outABits = inBitDepth;
				else
					outABits = 1;
			}
			
			result = new BufferedGrayImage(ihdr.width(), ihdr.height(), new int[]{outWBits, outABits});
		}
		
		
		@Override void decodeSubimage(DataInput din, int xOffset, int yOffset, int xStep, int yStep) throws IOException {
			int width  = Math.ceilDiv(result.getWidth () - xOffset, xStep);
			int height = Math.ceilDiv(result.getHeight() - yOffset, yStep);
			if (width == 0 || height == 0)
				return;
			
			int[] outBitDepths = result.getBitDepths();
			int wShift = inBitDepth - outBitDepths[0];
			int aShift = inBitDepth - outBitDepths[1];
			boolean hasAlpha = outBitDepths[1] > 0;
			int mode = inBitDepth >= 8 ? (inBitDepth / 8 - 1) * 2 + (hasAlpha ? 1 : 0) : 4;
			
			int filterStride = Math.ceilDiv(inBitDepth * (hasAlpha ? 2 : 1), 8);
			var dec = new RowDecoder(din, filterStride,
				Math.toIntExact(Math.ceilDiv((long)width * inBitDepth * (hasAlpha ? 2 : 1), 8)));
			for (int y = 0; y < height; y++) {
				byte[] row = dec.readRow();
				
				if (mode < 4) {
					for (int x = 0, i = filterStride; x < width; x++, i += filterStride) {
						int w, a, temp;
						switch (mode) {
							case 0 -> {
								w = row[i + 0] & 0xFF;
								temp = w << 16;
								a = temp != transparentColor ? 0xFF : 0x00;
							}
							case 1 -> {
								w = row[i + 0] & 0xFF;
								a = row[i + 1] & 0xFF;
							}
							case 2 -> {
								w = (row[i + 0] & 0xFF) << 8 | (row[i + 1] & 0xFF) << 0;
								temp = w << 16;
								a = temp != transparentColor ? 0xFFFF : 0x00;
							}
							case 3 -> {
								w = (row[i + 0] & 0xFF) << 8 | (row[i + 1] & 0xFF) << 0;
								a = (row[i + 2] & 0xFF) << 8 | (row[i + 3] & 0xFF) << 0;
							}
							default -> throw new AssertionError("Unreachable value");
						}
						w >>>= wShift;
						a >>>= aShift;
						result.setPixel(xOffset + x * xStep, yOffset + y * yStep,
							w << 16 | a << 0);
					}
				} else {
					int xMask = 8 / inBitDepth - 1;
					int shift = 8 - inBitDepth;
					int opaque = (1 << inBitDepth) - 1;
					for (int x = 0, i = filterStride, b = 0; x < width; x++, b = (b << inBitDepth) & 0xFF) {
						if ((x & xMask) == 0) {
							b = row[i] & 0xFF;
							i++;
						}
						int w = b >>> shift;
						int temp = w << 16;
						int a = (temp != transparentColor ? opaque : 0) >>> aShift;
						w >>>= wShift;
						result.setPixel(xOffset + x * xStep, yOffset + y * yStep, w << 16 | a << 0);
					}
				}
			}
		}
		
		
		@Override BufferedGrayImage getResult() {
			return result;
		}
		
	}
	
	
	
	/*---- A decoder subclass ----*/
	
	static final class PaletteImageDecoder extends ImageDecoder {
		
		private BufferedPaletteImage result;
		
		
		public PaletteImageDecoder(PngImage png) {
			super(png);
			
			// Handle significant bits
			int outRBits = 8, outGBits = 8, outBBits = 8;
			if (sbit.isPresent()) {
				byte[] sb = sbit.get().data();
				if (sb.length != 3)
					throw new IllegalArgumentException("Invalid sBIT data length");
				if (sb[0] > outRBits || sb[1] > outGBits || sb[2] > outBBits)
					throw new IllegalArgumentException("Number of significant bits exceeds bit depth");
				outRBits = sb[0];
				outGBits = sb[1];
				outBBits = sb[2];
			}
			
			// Handle palette and transparency
			if (png.plte.isEmpty())
				throw new IllegalArgumentException("Missing PLTE chunk");
			byte[] paletteBytes = png.plte.get().data();
			var palette = new long[paletteBytes.length / 3];
			if (palette.length > (1 << inBitDepth))
				throw new IllegalArgumentException("Palette length exceeds bit depth");
			byte[] trnsBytes = trns.map(trns -> trns.data()).orElse(new byte[0]);
			if (trnsBytes.length > palette.length)
				throw new IllegalArgumentException("Transparency has more entries than palette");
			int outABits = trns.isPresent() ? 8 : 0;
			for (int i = 0; i < palette.length; i++) {
				int r = (paletteBytes[i * 3 + 0] & 0xFF) >>> (8 - outRBits);
				int g = (paletteBytes[i * 3 + 1] & 0xFF) >>> (8 - outGBits);
				int b = (paletteBytes[i * 3 + 2] & 0xFF) >>> (8 - outBBits);
				int a = outABits == 0 ? 0 :
					(i < trnsBytes.length ? trnsBytes[i] & 0xFF : 0xFF);
				palette[i] = (long)r << 48 | (long)g << 32 | (long)b << 16 | (long)a << 0;
			}
			
			result = new BufferedPaletteImage(ihdr.width(), ihdr.height(),
				new int[]{outRBits, outGBits, outBBits, outABits}, palette);
		}
		
		
		@Override void decodeSubimage(DataInput din, int xOffset, int yOffset, int xStep, int yStep) throws IOException {
			int width  = Math.ceilDiv(result.getWidth () - xOffset, xStep);
			int height = Math.ceilDiv(result.getHeight() - yOffset, yStep);
			if (width == 0 || height == 0)
				return;
			
			int filterStride = 1;  // Equal to ceil(inBitDepth / 8)
			var dec = new RowDecoder(din, filterStride,
				Math.toIntExact(Math.ceilDiv((long)width * inBitDepth, 8)));
			for (int y = 0; y < height; y++) {
				byte[] row = dec.readRow();
				
				switch (inBitDepth) {
					case 1, 2, 4 -> {
						int xMask = 8 / inBitDepth - 1;
						int shift = 8 - inBitDepth;
						for (int x = 0, i = filterStride, b = 0; x < width; x++, b = (b << inBitDepth) & 0xFF) {
							if ((x & xMask) == 0) {
								b = row[i] & 0xFF;
								i++;
							}
							result.setPixel(xOffset + x * xStep, yOffset + y * yStep, b >>> shift);
						}
					}
					case 8 -> {
						for (int x = 0, i = filterStride; x < width; x++, i += filterStride)
							result.setPixel(xOffset + x * xStep, yOffset + y * yStep, row[i] & 0xFF);
					}
					default -> throw new AssertionError("Unreachable value");
				}
			}
		}
		
		
		@Override BufferedPaletteImage getResult() {
			return result;
		}
		
	}
	
}
