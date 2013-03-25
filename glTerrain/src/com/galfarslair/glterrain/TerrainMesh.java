package com.galfarslair.glterrain;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.galfarslair.util.Utils.TerrainException;

public interface TerrainMesh {
	final static float HEIGHT_SCALE = 0.15f;
	
	void build(Pixmap heightMap) throws TerrainException;
	void update(PerspectiveCamera camera);
	int getSize();	
	float getHeightAtPos(float x, float y);
}
