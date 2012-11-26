package com.galfarslair.util;

import java.util.Arrays;

import com.badlogic.gdx.math.MathUtils;

/** A resizable, ordered or unordered short array. Same as FloatArray, ShortArray, etc. in libGDX but for shorts (I needed that for index buffers). */
public class ShortArray {
	public short[] items;
	public int size;
	public boolean ordered;

	public ShortArray () {
		this(true, 16);
	}

	public ShortArray (int capacity) {
		this(true, capacity);
	}

	public ShortArray (boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new short[capacity];
	}

	public ShortArray (ShortArray array) {
		this.ordered = array.ordered;
		size = array.size;
		items = new short[size];
		System.arraycopy(array.items, 0, items, 0, size);
	}
	
	public ShortArray (short[] array) {
		this(true, array);
	}
	
	public ShortArray (boolean ordered, short[] array) {
		this(ordered, array.length);
		size = array.length;
		System.arraycopy(array, 0, items, 0, size);
	}

	public void add (short value) {
		short[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		items[size++] = value;
	}
	
	public void add (int value) {
		add((short)value);		
	}

	public void addAll (ShortArray array) {
		addAll(array, 0, array.size);
	}

	public void addAll (ShortArray array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (short[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll (short[] array, int offset, int length) {
		short[] items = this.items;
		int sizeNeeded = size + length - offset;
		if (sizeNeeded >= items.length) items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
		System.arraycopy(array, offset, items, size, length);
		size += length;
	}

	public short get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public void set (int index, short value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}

	public void insert (int index, short value) {
		short[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		if (ordered)
			System.arraycopy(items, index, items, index + 1, size - index);
		else
			items[size] = items[index];
		size++;
		items[index] = value;
	}

	public void swap (int first, int second) {
		if (first >= size) throw new IndexOutOfBoundsException(String.valueOf(first));
		if (second >= size) throw new IndexOutOfBoundsException(String.valueOf(second));
		short[] items = this.items;
		short firstValue = items[first];
		items[first] = items[second];
		items[second] = firstValue;
	}

	public boolean contains (int value) {
		int i = size - 1;
		short[] items = this.items;
		while (i >= 0)
			if (items[i--] == value) return true;
		return false;
	}

	public int indexOf (int value) {
		short[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			if (items[i] == value) return i;
		return -1;
	}

	public int lastIndexOf (int value) {
		short[] items = this.items;
		for (int i = size - 1; i >= 0; i--)
			if (items[i] == value) return i;
		return -1;
	}

	public boolean removeValue (short value) {
		short[] items = this.items;
		for (int i = 0, n = size; i < n; i++) {
			if (items[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	public short removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		short[] items = this.items;
		short value = items[index];
		size--;
		if (ordered)
			System.arraycopy(items, index + 1, items, index, size - index);
		else
			items[index] = items[size];
		return value;
	}

	public short pop () {
		return items[--size];
	}

	public short peek () {
		return items[size - 1];
	}

	public short first () {
		return items[0];
	}

	public void clear () {
		size = 0;
	}

	public void shrink () {
		resize(size);
	}

	public short[] ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}

	protected short[] resize (int newSize) {
		short[] newItems = new short[newSize];
		short[] items = this.items;
		System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
		this.items = newItems;
		return newItems;
	}

	public void sort () {
		Arrays.sort(items, 0, size);
	}

	public void reverse () {
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			short temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public void shuffle () {
		for (int i = size - 1; i >= 0; i--) {
			int ii = MathUtils.random(i);
			short temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public void truncate (int newSize) {
		if (size > newSize) size = newSize;
	}

	public int random () {
		if (size == 0) return 0;
		return items[MathUtils.random(0, size - 1)];
	}

	public short[] toArray () {
		short[] array = new short[size];
		System.arraycopy(items, 0, array, 0, size);
		return array;
	}

	public boolean equals (Object object) {
		if (object == this) return true;
		if (!(object instanceof ShortArray)) return false;
		ShortArray array = (ShortArray)object;
		int n = size;
		if (n != array.size) return false;
		for (int i = 0; i < n; i++)
			if (items[i] != array.items[i]) return false;
		return true;
	}

	public String toString () {
		if (size == 0) return "[]";
		short[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public String toString (String separator) {
		if (size == 0) return "";
		short[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(separator);
			buffer.append(items[i]);
		}
		return buffer.toString();
	}
}
