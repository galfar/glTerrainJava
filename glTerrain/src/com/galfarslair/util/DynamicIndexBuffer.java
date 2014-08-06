package com.galfarslair.util;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DynamicIndexBuffer {
	private final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);
	private final int usage = GL20.GL_DYNAMIC_DRAW;
		
	private IntBuffer buffer;	
	private int bufferHandle;	
	private boolean isDirty = true;
	private boolean isBound = false;
	
	private GL20 gl;	
	
	public DynamicIndexBuffer(int capacity) {
		assert Gdx.gl20 != null : "Needs GLES 2.0";
		gl = Gdx.gl20;
		buffer = BufferUtils.newIntBuffer(capacity);		
		bufferHandle = createBufferObject();		
	}
	
	public int getNumIndices () {
		return buffer.limit();
	}
	
	public int getCapacity() {
		return buffer.capacity();
	}
	
	private int createBufferObject () {
		gl.glGenBuffers(1, tmpHandle);
		gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, tmpHandle.get(0));
		// TODO: report this, can set empty buffer with data=null, LWJGL  backend wont call it!
		// It's actually LWJGL's fault, it checks the buffer! 
		gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.capacity() * 4, buffer, usage);  
		gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);		
		return tmpHandle.get(0);
	}
	
	private void updateSubData(int offset, int numIndices) {
		assert isDirty;
		gl.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, offset * 4, numIndices * 4, buffer);
		isDirty = false;
	}
	
	public void setIndices(int[] indices, int offset, int numIndices) {
		isDirty = true;
		BufferUtils.copy(indices, offset, buffer, numIndices);
		buffer.position(0);
		buffer.limit(numIndices);
		
		if (isBound) {
			updateSubData(0, buffer.limit());
		}
	}
	
	public void bind() {
		if (bufferHandle == 0)
			throw new GdxRuntimeException("No buffer allocated!");

		gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
		if (isDirty) {
			updateSubData(0, buffer.limit());
		}
		isBound = true;
	}
	
	public void unbind () {
		gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		isBound = false;
	}
	
	public void dispose() {
		tmpHandle.clear();
		tmpHandle.put(bufferHandle);
		tmpHandle.flip();
		gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glDeleteBuffers(1, tmpHandle);
		bufferHandle = 0;		
	}

}
