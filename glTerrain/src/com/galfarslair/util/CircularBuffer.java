package com.galfarslair.util;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class CircularBuffer<T> {
	private T[] buffer;
	private int tail;
	private int head;

	@SuppressWarnings("unchecked")
	public CircularBuffer(int capacity) {
		buffer = (T[]) new Object[capacity];
		tail = 0;
		head = 0;
	}

	public CircularBuffer(T[] items) {
		this(items.length);
		System.arraycopy(items, 0, buffer, 0, items.length);
		head = buffer.length - 1;
	}

	public void add(T toAdd) {
		if (head != (tail - 1)) {
			buffer[head++] = toAdd;
		} else {
			throw new BufferOverflowException();
		}
		head = head % buffer.length;
	}

	public T get() {
		T t = null;
		int adjTail = tail > head ? tail - buffer.length : tail;
		if (adjTail < head) {
			t = (T) buffer[tail++];
			tail = tail % buffer.length;
		} else {
			throw new BufferUnderflowException();
		}
		return t;
	}
}