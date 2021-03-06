package com.galfarslair.glterrain;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Disposable;
import com.galfarslair.util.Utils.TerrainException;

public interface TerrainRenderer  extends Disposable {
	void assignMesh(TerrainMesh mesh) throws TerrainException;
	void render(Camera camera);	
	void setWireFrameOverlay(boolean enabled);
}
