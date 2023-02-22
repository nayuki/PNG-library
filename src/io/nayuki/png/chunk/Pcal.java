/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import io.nayuki.png.Chunk;


/**
 * A calibration of pixel values (pCAL) chunk. This specifies how
 * pixel values are mapped to physical quantities. Instances should
 * be treated as immutable, but arrays are not copied defensively.
 * @see https://ftp-osl.osuosl.org/pub/libpng/documents/pngext-1.5.0.html#C.pCAL
 */
public record Pcal(
		String calibrationName,
		String unitName,
		int originalZero,
		int originalMax,
		EquationType equationType,
		String... parameters)
	implements Chunk {
	
	
	static final String TYPE = "pCAL";
	
	
	/*---- Constructor ----*/
	
	public Pcal {
		Util.checkKeyword(calibrationName, true);
		Util.checkIso8859_1(unitName, false);
		if (originalZero == Integer.MIN_VALUE || originalMax == Integer.MIN_VALUE)
			throw new IllegalArgumentException("Invalid int32 value");
		if (originalZero == originalMax)
			throw new IllegalArgumentException("Zero range");
		
		Objects.requireNonNull(equationType);
		if (equationType.numParameters != parameters.length)
			throw new IllegalArgumentException("Invalid number of parameters for equation type");
		for (String param : parameters) {
			if (Util.testAsciiFloat(param) == -1)
				throw new IllegalArgumentException("Invalid number string");
		}
		
		var params = new Object[parameters.length];
		System.arraycopy(parameters, 0, params, 0, params.length);
		Util.checkedLengthSum(calibrationName, 1, 2 * Integer.BYTES,
			2 * Byte.BYTES, unitName, params.length, Util.checkedLengthSum(params));
	}
	
	
	/**
	 * Reads the specified number of bytes from the specified input stream,
	 * parses the fields, and returns a new chunk object of this type.
	 * @param dataLen the expected number of bytes of chunk data (non-negative)
	 * @param in the input stream to read from (not {@code null})
	 * @return a new chunk object of this type (not {@code null})
	 * @throws NullPointerException if the input stream is {@code null}
	 * @throws IllegalArgumentException if {@code dataLen} is negative
	 * or the read data is invalid for this chunk type
	 * @throws IOException if an I/O exception occurs
	 */
	public static Pcal read(int dataLen, DataInput in) throws IOException {
		if (dataLen < 0)
			throw new IllegalArgumentException("Negative data length");
		Objects.requireNonNull(in);
		
		byte[][] parts = Util.readAndSplitByNul(dataLen, in, 2);
		var calibrationName = new String(parts[0], StandardCharsets.ISO_8859_1);
		if (parts[1].length < 10)
			throw new IllegalArgumentException("Missing fields");
		byte[] prefix = Arrays.copyOf(parts[1], 10);
		in = new DataInputStream(new ByteArrayInputStream(prefix));
		int originalZero = in.readInt();
		int originalMax = in.readInt();
		EquationType equationType = Util.indexInto(EquationType.values(), in.readUnsignedByte());
		int numParameters = in.readUnsignedByte();
		
		byte[] suffix = Arrays.copyOfRange(parts[1], 10, parts[1].length);
		parts = Util.splitByNul(suffix, numParameters + 1);
		var unitName = new String(parts[0], StandardCharsets.ISO_8859_1);
		var parameters = new String[numParameters];
		for (int i = 0; i < parameters.length; i++)
			parameters[i] = new String(parts[1 + i], StandardCharsets.US_ASCII);
		
		return new Pcal(calibrationName, unitName, originalZero, originalMax, equationType, parameters);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public int getDataLength() {
		var params = new Object[parameters.length];
		System.arraycopy(parameters, 0, params, 0, params.length);
		return Util.checkedLengthSum(calibrationName, 1, 2 * Integer.BYTES,
			2 * Byte.BYTES, unitName, params.length, Util.checkedLengthSum(params));
	}
	
	
	@Override public void writeData(DataOutput out) throws IOException {
		out.write(calibrationName.getBytes(StandardCharsets.ISO_8859_1));
		out.writeByte(0);
		out.writeInt(originalZero);
		out.writeInt(originalMax);
		out.writeByte(equationType.ordinal());
		out.writeByte(parameters.length);
		out.write(unitName.getBytes(StandardCharsets.ISO_8859_1));
		for (String param : parameters) {
			out.writeByte(0);
			out.write(param.getBytes(StandardCharsets.US_ASCII));
		}
	}
	
	
	
	/*---- Enumeration ----*/
	
	public enum EquationType {
		LINEAR(2),
		BASE_E_EXPONENTIAL(3),
		ARBITRARY_BASE_EXPONENTIAL(3),
		HYPERBOLIC(4);
		
		public final int numParameters;
		
		private EquationType(int numParams) {
			this.numParameters = numParams;
		}
	}
	
}
