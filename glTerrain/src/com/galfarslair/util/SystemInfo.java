package com.galfarslair.util;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;

public class SystemInfo {
	public enum GLType {
		OpenGL, GLES
	}
	
	private int resolutionWidth;
	private int resolutionHeight;
		
	private String glVersionString;
	private String glRenderer;
	private String glVendor;
	
	private Set<String> glExtensions;
	
	private int maxTextureSize;
	private int maxVertexTextureUnits;
	
	public SystemInfo() {
		glExtensions = new HashSet<String>();
	}
	
	public long getJavaHeapMemory() {
		return Gdx.app.getJavaHeap();
	}
	
	public long getNativeHeapMemory() {
		return Gdx.app.getNativeHeap();
	}
	
	public long getMaxRuntimeMemory() {
		return Runtime.getRuntime().maxMemory();
	}
	
	public int getResolutionWidth() {
		return resolutionWidth;
	}
	
	public int getResolutionHeight() {
		return resolutionHeight;
	}
	
	public String getGLVersionString() {
		return glVersionString;
	}
	
	public String getGLRenderer() {
		return glRenderer;
	}
	
	public String getGLVendor() {
		return glVendor;
	}
	
	public int getMaxVertexTextureImageUnits() {
		return maxVertexTextureUnits;
	}
	
	public Set<String> getGLExtensions() {
		return Collections.unmodifiableSet(glExtensions);
	}
	
	public int getMaxTextureSize() {
		return maxTextureSize;
	}
	
	public boolean hasDesktopGpu() {
		return (Gdx.app.getType() == ApplicationType.Desktop) || (Gdx.app.getType() == ApplicationType.Applet);
	}
	
	public void gather() {
		GL20 gl = Gdx.gl;
		
		resolutionWidth = Gdx.graphics.getWidth();
	    resolutionHeight = Gdx.graphics.getHeight();
		
		glVersionString = gl.glGetString(GL20.GL_VERSION);   
	    glRenderer = gl.glGetString(GL20.GL_RENDERER);
	    glVendor = gl.glGetString(GL20.GL_VENDOR);
	    	    
	    String extsString = gl.glGetString(GL20.GL_EXTENSIONS);
	    String[] exts = extsString.split(" ");
	    glExtensions.clear();
	    for (String e : exts) {
	    	glExtensions.add(e);
	    }		
		
	    maxTextureSize = getGLInteger(GL20.GL_MAX_TEXTURE_SIZE);
	    maxVertexTextureUnits = getGLInteger(GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
	}
	
	private int getGLInteger(int pname) {
		IntBuffer buff = BufferUtils.newIntBuffer(16);			
		Gdx.gl.glGetIntegerv(pname, buff);
		return buff.get(0);	
	}
	

}
