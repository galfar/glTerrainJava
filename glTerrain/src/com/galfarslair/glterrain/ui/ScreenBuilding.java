package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;

public class ScreenBuilding extends UIScreen {

	private List<String> log;
		
	public ScreenBuilding() {
		super();
	}

	@Override
	protected void defineControls() {		
		controls.add(new Label("Building terrain...", skin)).pad(20, 0, 16, 0).left();		
		controls.row();
		controls.add().expandY();
		controls.row();
				
		log = new List<String>(skin, "listing");		
		controls.add(log).fillX().height(260);
		controls.row();
	}
	
	public void addToLog(String line) {
		log.getItems().add(line);	
	}
	
}
