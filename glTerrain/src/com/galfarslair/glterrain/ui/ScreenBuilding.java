package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Array;
import com.galfarslair.glterrain.TerrainRunner;

public class ScreenBuilding extends UIScreen {

	private List log;
	private Array<String> lines;
	
	public ScreenBuilding() {
		super();		
		lines = new Array<String>();
	}

	@Override
	protected void defineControls() {		
		controls.add(new Label("Building terrain...", skin)).pad(20, 0, 16, 0);
		controls.row();
		controls.add().expandY();
		controls.row();
				
		log = new List(new Object[] { }, skin, "listing");		
		controls.add(log).fillX().height(260);
		controls.row();
	}
	
	public void addToLog(String line) {
		lines.add(line);
		log.setItems(lines.toArray());
	}
	
}
