package com.galfarslair.glterrain;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.galfarslair.glterrain.TerrainRunner;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "glTerrain";
		cfg.useGL20 = true;
		cfg.width = 1024;
		cfg.height = 768;
						
		new LwjglApplication(new TerrainRunner(), cfg);
	}
}
