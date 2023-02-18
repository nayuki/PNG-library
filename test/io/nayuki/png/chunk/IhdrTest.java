/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import static io.nayuki.png.Chunk.CompressionMethod.ZLIB_DEFLATE;
import static io.nayuki.png.chunk.Ihdr.FilterMethod.ADAPTIVE;
import static io.nayuki.png.chunk.Ihdr.InterlaceMethod.ADAM7;
import static io.nayuki.png.chunk.Ihdr.InterlaceMethod.NONE;
import static io.nayuki.png.chunk.TestUtil.hexToBytes;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import io.nayuki.png.chunk.Ihdr.ColorType;


public final class IhdrTest {
	
	@Test public void testCreateDimensionsBad() {
		int[][] CASES = {
			{0, 0},
			{-1, 0},
			{-1, 2},
			{0, -1},
			{3, -1},
			{-1, -1},
			{Integer.MIN_VALUE, 4},
			{5, Integer.MIN_VALUE},
			{Integer.MIN_VALUE, Integer.MIN_VALUE},
		};
		
		for (int[] cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Ihdr(cs[0], cs[1], 8, ColorType.TRUE_COLOR, ZLIB_DEFLATE, ADAPTIVE, NONE));
		}
	}
	
	
	@Test public void testCreateBitDepthBad() {
		record Case(int bitDepth, ColorType colorType) {}
		Case[] CASES = {
			new Case(Integer.MIN_VALUE, ColorType.TRUE_COLOR),
			new Case(               -1, ColorType.TRUE_COLOR),
			new Case(                0, ColorType.TRUE_COLOR),
			new Case(                1, ColorType.TRUE_COLOR),
			new Case(                2, ColorType.TRUE_COLOR),
			new Case(                5, ColorType.TRUE_COLOR),
			new Case(               12, ColorType.TRUE_COLOR),
			new Case(               17, ColorType.TRUE_COLOR),
			new Case(         55442233, ColorType.TRUE_COLOR),
			new Case(Integer.MAX_VALUE, ColorType.TRUE_COLOR),
			new Case(Integer.MIN_VALUE, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(        -12345678, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(                0, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(                1, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(                3, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(                6, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(               11, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(               17, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(            79068, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(Integer.MAX_VALUE, ColorType.TRUE_COLOR_WITH_ALPHA),
			new Case(Integer.MIN_VALUE, ColorType.GRAYSCALE),
			new Case(           -31415, ColorType.GRAYSCALE),
			new Case(                0, ColorType.GRAYSCALE),
			new Case(                3, ColorType.GRAYSCALE),
			new Case(                5, ColorType.GRAYSCALE),
			new Case(                7, ColorType.GRAYSCALE),
			new Case(               13, ColorType.GRAYSCALE),
			new Case(               17, ColorType.GRAYSCALE),
			new Case(           271828, ColorType.GRAYSCALE),
			new Case(Integer.MAX_VALUE, ColorType.GRAYSCALE),
			new Case(Integer.MIN_VALUE, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(           -14142, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(                0, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(                3, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(                6, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(                9, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(               15, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(               17, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(             1618, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(Integer.MAX_VALUE, ColorType.GRAYSCALE_WITH_ALPHA),
			new Case(Integer.MIN_VALUE, ColorType.INDEXED_COLOR),
			new Case(         -5772156, ColorType.INDEXED_COLOR),
			new Case(                0, ColorType.INDEXED_COLOR),
			new Case(                3, ColorType.INDEXED_COLOR),
			new Case(                5, ColorType.INDEXED_COLOR),
			new Case(                6, ColorType.INDEXED_COLOR),
			new Case(                7, ColorType.INDEXED_COLOR),
			new Case(               10, ColorType.INDEXED_COLOR),
			new Case(               14, ColorType.INDEXED_COLOR),
			new Case(               17, ColorType.INDEXED_COLOR),
			new Case(          4669201, ColorType.INDEXED_COLOR),
			new Case(Integer.MAX_VALUE, ColorType.INDEXED_COLOR),
		};
		
		for (Case cs : CASES) {
			TestUtil.runExpect(IllegalArgumentException.class,
				() -> new Ihdr(1, 1, cs.bitDepth, cs.colorType, ZLIB_DEFLATE, ADAPTIVE, NONE));
		}
	}
	
	
	@Test public void testGetData() {
		assertArrayEquals(hexToBytes("00000001 00000001 08 02 00 00 00"),  new Ihdr(         1,          1,  8, ColorType.TRUE_COLOR           , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("00010000 00001000 10 02 00 00 01"),  new Ihdr(     65536,       4096, 16, ColorType.TRUE_COLOR           , ZLIB_DEFLATE, ADAPTIVE, ADAM7).getData());
		assertArrayEquals(hexToBytes("00000003 00000002 08 06 00 00 01"),  new Ihdr(         3,          2,  8, ColorType.TRUE_COLOR_WITH_ALPHA, ZLIB_DEFLATE, ADAPTIVE, ADAM7).getData());
		assertArrayEquals(hexToBytes("0000010F 00000300 10 06 00 00 00"),  new Ihdr(       271,        768, 16, ColorType.TRUE_COLOR_WITH_ALPHA, ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("00000009 00000004 01 00 00 00 00"),  new Ihdr(         9,          4,  1, ColorType.GRAYSCALE            , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("00000009 00000004 02 00 00 00 01"),  new Ihdr(         9,          4,  2, ColorType.GRAYSCALE            , ZLIB_DEFLATE, ADAPTIVE, ADAM7).getData());
		assertArrayEquals(hexToBytes("00000009 00000004 04 00 00 00 00"),  new Ihdr(         9,          4,  4, ColorType.GRAYSCALE            , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("00000009 00000004 08 00 00 00 00"),  new Ihdr(         9,          4,  8, ColorType.GRAYSCALE            , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("00000009 00000004 10 00 00 00 00"),  new Ihdr(         9,          4, 16, ColorType.GRAYSCALE            , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("7FFFFFFF 7FFFFFFF 08 04 00 00 00"),  new Ihdr(2147483647, 2147483647,  8, ColorType.GRAYSCALE_WITH_ALPHA , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("7FFFFFFE 00000007 10 04 00 00 01"),  new Ihdr(2147483646,          7, 16, ColorType.GRAYSCALE_WITH_ALPHA , ZLIB_DEFLATE, ADAPTIVE, ADAM7).getData());
		assertArrayEquals(hexToBytes("00000005 00000005 01 03 00 00 01"),  new Ihdr(         5,          5,  1, ColorType.INDEXED_COLOR        , ZLIB_DEFLATE, ADAPTIVE, ADAM7).getData());
		assertArrayEquals(hexToBytes("00000002 00000008 02 03 00 00 00"),  new Ihdr(         2,          8,  2, ColorType.INDEXED_COLOR        , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
		assertArrayEquals(hexToBytes("40000000 01000000 04 03 00 00 01"),  new Ihdr(1073741824,   16777216,  4, ColorType.INDEXED_COLOR        , ZLIB_DEFLATE, ADAPTIVE, ADAM7).getData());
		assertArrayEquals(hexToBytes("00000007 7FFFFFFF 08 03 00 00 00"),  new Ihdr(         7, 2147483647,  8, ColorType.INDEXED_COLOR        , ZLIB_DEFLATE, ADAPTIVE, NONE ).getData());
	}
	
}
