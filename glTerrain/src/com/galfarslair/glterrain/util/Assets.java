package com.galfarslair.glterrain.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.jktx.KTXFile;
import nl.weeaboo.jktx.KTXFormatException;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.galfarslair.util.KtxTextureData;
import com.galfarslair.util.KtxTextureData.TextureFormatHelper;

public class Assets {

	public static FileHandle getFile(String dataPath) {
		return Gdx.files.internal("data/" + dataPath);
	}
	
	public static FileHandle getClasspathFile(String path) {
		return Gdx.files.classpath("com/galfarslair/glterrain/" + path);
	}
	
	public static FileHandle getFilePlatform(String dataPath) {
		String fullPath = null;
		if (Gdx.app.getType() == ApplicationType.Desktop) {
			fullPath = "data-desktop/" + dataPath;  
		} else if (Gdx.app.getType() == ApplicationType.Android) {
			fullPath = "data-android/" + dataPath;
		}
		
		FileHandle fh = Gdx.files.internal(fullPath);
		if (fh.exists()) {
			return fh;
		} else {
			return getFile(dataPath);
		}
	}
	
	public static DataInputStream getFileStream(String dataPath) {
		FileHandle handle = getFile(dataPath);
		return getFileStream(handle);
	}
	
	public static DataInputStream getFileStream(FileHandle handle) {		
		return new DataInputStream(new BufferedInputStream(handle.read()));
	}
	
	public static Texture loadKtxTexture(String dataPath) throws IOException {
		FileHandle fh = getFilePlatform(dataPath);
		InputStream in = getFileStream(fh);
		
		KTXFile ktx = new KTXFile();
		try {
			ktx.read(in);
		} catch (KTXFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean supported = TextureFormatHelper.isFormatSupported(ktx.getHeader().getGLFormat(), ktx.getHeader().getGLInternalFormat());
		if (!supported) {
			return null;
		}
		
		KtxTextureData data = new KtxTextureData(ktx); 
		return new Texture(data);
	}
	
}
