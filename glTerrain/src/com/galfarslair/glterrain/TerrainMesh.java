package com.galfarslair.glterrain;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.galfarslair.util.Utils.TerrainException;

public interface TerrainMesh {
	void build(Pixmap heightMap) throws TerrainException;
	void update(PerspectiveCamera camera);
	int getSize();
	
}
