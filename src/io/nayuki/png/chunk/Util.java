package io.nayuki.png.chunk;


final class Util {
	
	public static <E> E indexInto(E[] array, int index) {
		if (0 <= index && index < array.length)
			return array[index];
		else
			return null;
	}
	
}
