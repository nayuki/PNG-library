/* 
 * PNG library (Java)
 * 
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/png-library
 */

package io.nayuki.png.chunk;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


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
	
	
	/*---- Constructor and factory ----*/
	
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
		Util.checkedLengthSum(calibrationName, Byte.BYTES, 2 * Integer.BYTES,
			2 * Byte.BYTES, unitName, params.length * Byte.BYTES, Util.checkedLengthSum(params));
	}
	
	
	static Pcal read(ChunkReader in) throws IOException {
		Objects.requireNonNull(in);
		String calibName = in.readString(ChunkReader.Until.NUL, StandardCharsets.ISO_8859_1);
		int originalZero = in.readInt32();
		int originalMax = in.readInt32();
		EquationType equationType = in.readEnum(EquationType.values());
		int numParameters = in.readUint8();
		String unitName = in.readString(ChunkReader.Until.NUL, StandardCharsets.ISO_8859_1);
		var parameters = new String[numParameters];
		for (int i = 0; i < parameters.length; i++)
			parameters[i] = in.readString((i < parameters.length - 1 ? ChunkReader.Until.NUL : ChunkReader.Until.END), StandardCharsets.US_ASCII);
		return new Pcal(calibName, unitName, originalZero, originalMax, equationType, parameters);
	}
	
	
	/*---- Methods ----*/
	
	@Override public String getType() {
		return TYPE;
	}
	
	
	@Override public void writeChunk(OutputStream out) throws IOException {
		var params = new Object[parameters.length];
		System.arraycopy(parameters, 0, params, 0, params.length);
		int dataLen = Util.checkedLengthSum(calibrationName, 1, 2 * Integer.BYTES,
			2 * Byte.BYTES, unitName, params.length, Util.checkedLengthSum(params));
		
		var cout = new ChunkWriter(dataLen, TYPE, out);
		cout.writeString(calibrationName, StandardCharsets.ISO_8859_1, true);
		cout.writeInt32(originalZero);
		cout.writeInt32(originalMax);
		cout.writeUint8(equationType);
		cout.writeUint8(parameters.length);
		cout.writeString(unitName, StandardCharsets.ISO_8859_1, true);
		for (int i = 0; i < parameters.length; i++)
			cout.writeString(parameters[i], StandardCharsets.US_ASCII, i < parameters.length - 1);
		cout.finish();
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
