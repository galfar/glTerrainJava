package com.galfarslair.glterrain.util;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.galfarslair.glterrain.TerrainRenderer;

public abstract class BaseTileTerrainRenderer implements TerrainRenderer {

	protected ShaderProgram shader;
	protected ShaderProgram shaderDefault;
	protected ShaderProgram shaderWire;
	
	public BaseTileTerrainRenderer(ShaderProgram shaderDefault, ShaderProgram shaderWire) {
		assert shaderDefault != null;		
		assert shaderWire != null;
		this.shaderDefault = shaderDefault;		
		this.shaderWire = shaderWire;
		this.shader = shaderDefault;
	}
	
	@Override
	public void setWireFrameOverlay(boolean enabled) {
		shader = enabled ? shaderWire : shaderDefault;
	}

}
