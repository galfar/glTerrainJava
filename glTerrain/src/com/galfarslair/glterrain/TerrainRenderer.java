package com.galfarslair.glterrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.galfarslair.util.Utils.TerrainException;

public interface TerrainRenderer {
	void assignMesh(TerrainMesh mesh) throws TerrainException;
	void render(Camera camera);
}
