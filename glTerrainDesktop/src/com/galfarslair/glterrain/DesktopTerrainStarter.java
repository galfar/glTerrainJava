package com.galfarslair.glterrain;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.glterrain.util.PlatformSupport;

public class DesktopTerrainStarter {
		
	public static void main(String[] args) {
		LwjglApplicationConfiguration.disableAudio = true;
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "glTerrain";
		cfg.foregroundFPS = 0;	
		
		cfg.r = 8;
		cfg.g = 8;
		cfg.b = 8;
		cfg.depth = 24;
		
		//cfg.samples = 4;
		cfg.vSyncEnabled = false;		
		cfg.fullscreen = false;
		
		cfg.width = 1400;
		cfg.height = 1000;		
	    /*cfg.width = 800;
		cfg.height = 480;*/		
		
		new LwjglApplication(new TerrainRunner(new DesktopPlatformSupport()), cfg);
	}
	
	private static class DesktopPlatformSupport implements PlatformSupport {
		@Override
		public void enableWireframe() {			
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glLineWidth(2.5f);			
			GL11.glEnable(GL11.GL_LINE_SMOOTH);			
			GL11.glEnable(GL11.GL_POLYGON_SMOOTH );
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}

		@Override
		public void updateDisplay() {
			Display.update();			
		}		
	}

}
