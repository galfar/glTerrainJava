package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

public class ScreenMainMenu extends UIScreen {

	
	
	public ScreenMainMenu() {
		super();
		
		root.add(new TextButton("Text Button", skin)).expand();
		root.row();
		root.pack();
				
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
