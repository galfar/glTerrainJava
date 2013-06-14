package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.galfarslair.glterrain.TerrainRunner;

public class ScreenBuilding extends UIScreen {

	public ScreenBuilding() {
		super();
		
		root.add(new Label("Loading...........", skin, "small")).colspan(2).right();
		root.pack();
	}
	
}
