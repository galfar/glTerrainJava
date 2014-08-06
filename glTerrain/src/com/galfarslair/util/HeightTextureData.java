package com.galfarslair.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class HeightTextureData implements TextureData {

	private HeightMap heightMap;
	private boolean isPrepared = false;
	
	public HeightTextureData(HeightMap heightMap) {
		this.heightMap = heightMap;
	}
	
	@Override
	public TextureDataType getType() {		 
		return TextureDataType.Custom; 
	}

	@Override
	public boolean isPrepared() {		
		return isPrepared;
	}

	@Override
	public void prepare() {
		if (isPrepared) throw new GdxRuntimeException("Already prepared");		
		isPrepared = true;
	}
	
	@Override
	public void consumeCustomData(int target) {
		Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
		Gdx.gl.glTexImage2D(target, 0, GL20.GL_LUMINANCE, getWidth(), getHeight(), 0,
				GL20.GL_LUMINANCE, GL20.GL_UNSIGNED_SHORT, heightMap.getSamples());		
	}

	@Override
	public Pixmap consumePixmap() {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");		
	}

	@Override
	public boolean disposePixmap() {
		throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
	}

	@Override
	public int getWidth() {		
		return heightMap.getWidth();
	}

	@Override
	public int getHeight() {		 
		return heightMap.getHeight();
	}

	@Override
	public Format getFormat() {		
		return Format.Intensity;
	}

	@Override
	public boolean useMipMaps() {		
		return false;
	}

	@Override
	public boolean isManaged() {		
		return false;
	}

}
