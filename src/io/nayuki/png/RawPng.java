package io.nayuki.png;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import io.nayuki.png.chunk.Bkgd;
import io.nayuki.png.chunk.Chrm;
import io.nayuki.png.chunk.Custom;
import io.nayuki.png.chunk.Gama;
import io.nayuki.png.chunk.Hist;
import io.nayuki.png.chunk.Iccp;
import io.nayuki.png.chunk.Idat;
import io.nayuki.png.chunk.Iend;
import io.nayuki.png.chunk.Ihdr;
import io.nayuki.png.chunk.Phys;
import io.nayuki.png.chunk.Plte;
import io.nayuki.png.chunk.Sbit;
import io.nayuki.png.chunk.Srgb;
import io.nayuki.png.chunk.Text;
import io.nayuki.png.chunk.Time;
import io.nayuki.png.chunk.Trns;


public final class RawPng {
	
	public static List<Chunk> read(InputStream in) throws IOException {
		var din0 = new DataInputStream(in);
		
		var sig = new byte[SIGNATURE.length];
		din0.readFully(sig);
		if (!Arrays.equals(sig, SIGNATURE))
			throw new IllegalArgumentException();
		
		List<Chunk> result = new ArrayList<>();
		while (true) {
			int b = din0.read();
			if (b == -1)
				break;
			int dataLen = b;
			for (int i = 0; i < 3; i++)
				dataLen = (dataLen << 8) | din0.readUnsignedByte();
			if (dataLen < 0)
				throw new IllegalArgumentException();
			
			var cin = new CheckedInputStream(in, new CRC32());
			DataInput din1 = new DataInputStream(cin);
			var sb = new StringBuilder();
			for (int i = 0; i < 4; i++)
				sb.append((char)din1.readUnsignedByte());
			String type = sb.toString();
			Chunk.checkType(type);
			
			var bin = new BoundedInputStream(cin, dataLen);
			din1 = new DataInputStream(bin);
			result.add(switch (type) {
				case "bKGD" -> Bkgd.read(dataLen, din1);
				case "cHRM" -> Chrm.read(         din1);
				case "gAMA" -> Gama.read(         din1);
				case "hIST" -> Hist.read(dataLen, din1);
				case "iCCP" -> Iccp.read(dataLen, din1);
				case "IDAT" -> Idat.read(dataLen, din1);
				case "IEND" -> Iend.SINGLETON;
				case "IHDR" -> Ihdr.read(         din1);
				case "pHYs" -> Phys.read(         din1);
				case "PLTE" -> Plte.read(dataLen, din1);
				case "sBIT" -> Sbit.read(dataLen, din1);
				case "sRGB" -> Srgb.read(         din1);
				case "tEXt" -> Text.read(dataLen, din1);
				case "tIME" -> Time.read(         din1);
				case "tRNS" -> Trns.read(dataLen, din1);
				default -> Custom.read(type, dataLen, din1);
			});
			bin.finish();
			
			long crc = cin.getChecksum().getValue();
			if (crc >>> 32 != 0)
				throw new AssertionError();
			if (din0.readInt() != (int)crc)
				throw new IllegalArgumentException();
		}
		return result;
	}
	
	
	public static void write(List<Chunk> chunks, OutputStream out) throws IOException {
		out.write(SIGNATURE);
		DataOutput dout = new DataOutputStream(out);
		for (Chunk chunk : chunks) {
			int dataLen = chunk.getDataLength();
			if (dataLen < 0)
				throw new IllegalArgumentException();
			dout.writeInt(dataLen);
			
			String type = chunk.getType();
			Chunk.checkType(type);
			var cout = new CheckedOutputStream(out, new CRC32());
			cout.write(type.getBytes(StandardCharsets.US_ASCII));
			
			var bout = new BoundedOutputStream(cout, dataLen);
			chunk.writeData(new DataOutputStream(bout));
			bout.finish();
			
			long crc = cout.getChecksum().getValue();
			if (crc >>> 32 != 0)
				throw new AssertionError();
			dout.writeInt((int)crc);
		}
	}
	
	
	private static final byte[] SIGNATURE = {(byte)0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
	
}
