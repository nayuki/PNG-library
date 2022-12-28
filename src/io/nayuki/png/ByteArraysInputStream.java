package io.nayuki.png;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


final class ByteArraysInputStream extends InputStream {
	
	private Iterator<byte[]> iterator;
	private byte[] array;
	private int index;
	
	
	public ByteArraysInputStream(List<byte[]> arrays) {
		Objects.requireNonNull(arrays);
		iterator = arrays.iterator();
		nextArray();
	}
	
	
	@Override public int read() {
		while (true) {
			if (array == null)
				return -1;
			else if (index < array.length) {
				int result = Byte.toUnsignedInt(array[index]);
				index++;
				return result;
			} else
				nextArray();
		}
	}
	
	
	@Override public int read(byte[] b, int off, int len) {
		while (true) {
			if (array == null)
				return -1;
			else if (index < array.length) {
				int n = Math.min(len, array.length - index);
				System.arraycopy(array, index, b, off, n);
				index += n;
				return n;
			} else
				nextArray();
		}
	}
	
	
	private void nextArray() {
		if (iterator.hasNext()) {
			array = iterator.next();
			index = 0;
		} else {
			array = null;
			index = -1;
		}
	}
	
}
