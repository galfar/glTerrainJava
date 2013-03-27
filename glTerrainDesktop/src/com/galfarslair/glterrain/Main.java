package com.galfarslair.glterrain;

import org.lwjgl.opengl.GL11;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.util.FeatureSupport;

public class Main {
	
	private static class WireframeSupport implements FeatureSupport {
		public void enable() {			
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glLineWidth(2.5f);			
			GL11.glEnable(GL11.GL_LINE_SMOOTH);			
			GL11.glEnable(GL11.GL_POLYGON_SMOOTH );
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
	}
	
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "glTerrain";
		cfg.useGL20 = true;
		cfg.width = 1400;
		cfg.height = 1000;
		/*cfg.width = 800;
		cfg.height = 480;*/
						
		new LwjglApplication(new TerrainRunner(new WireframeSupport()), cfg);
	}
}
