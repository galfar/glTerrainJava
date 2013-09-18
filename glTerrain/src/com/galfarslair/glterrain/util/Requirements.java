package com.galfarslair.glterrain.util;

import java.util.Set;

import com.galfarslair.util.SystemInfo;

public class Requirements {
	
	public static final int REQUIRED_MEMORY_MB = 32;
	public static final int REQUIRED_TEXTURE_SIZE = 2048;
	
	private SystemInfo info;
	private Set<String> exts;
	
	public Requirements(SystemInfo info) {
		this.info = info;
		this.exts = this.info.getGLExtensions();
	}
	
	public SystemInfo getSystemInfo() {
		return info;
	}
	
	public boolean wireframeOverlayAvailable() {
		return exts.contains("GL_OES_standard_derivatives") || info.hasDesktopGpu();
	}
	
	public boolean soarAvailable() {
		return exts.contains("GL_OES_element_index_uint") ||  info.hasDesktopGpu();
	}
	
	public boolean memoryAvaiable() {
		return info.getMaxRuntimeMemory() >= REQUIRED_MEMORY_MB * 1024 * 1024; 
	}
	
	public boolean vtfAvaiable() {
		return info.getMaxVertexTextureImageUnits() > 0; 
	}	
	
	public boolean textureSizeOk() {
		return info.getMaxTextureSize() >= REQUIRED_TEXTURE_SIZE;
	}

}
