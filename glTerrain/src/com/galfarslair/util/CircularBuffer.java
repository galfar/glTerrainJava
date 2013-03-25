package com.galfarslair.util;

public class CircularBuffer<T> {
	private T[] buffer;
	private int index;	

	@SuppressWarnings("unchecked")
	public CircularBuffer(int capacity) {
		buffer = (T[]) new Object[capacity];
		reset();
	}

	public CircularBuffer(T[] items) {
		this(items.length);
		System.arraycopy(items, 0, buffer, 0, items.length);		
	}
	
	public void reset() {
	  index = -1;	
	}
	
	public T get() {
		return buffer[index];
	}
	
	public T next() {		
		index++;
		if (index >= buffer.length) {
			index = 0;
		}		
     	return get();			
	}
}