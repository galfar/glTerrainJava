package com.galfarslair.glterrain;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.Utils.TerrainException;

public interface TerrainMesh {
	final static float HEIGHT_SCALE = 0.13f;
	
	void build(HeightMap heightMap) throws TerrainException;
	void update(PerspectiveCamera camera, float tolerance);
	int getSize();	
	float getHeightAtPos(float x, float y);
}
