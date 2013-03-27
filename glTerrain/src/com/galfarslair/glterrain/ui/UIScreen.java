package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.galfarslair.util.Assets;

public class UIScreen implements Screen {

	protected static Skin skin;
	
	protected Stage stage;
	protected Table root;
	
	public static void initStatic() {
		skin = new Skin(Assets.getFile("uiskin.json"));
	}
	
	public UIScreen() {
		stage = new Stage();
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);
		
		//root.debug();
				
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 0.8f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		stage.act(delta);
		stage.draw();
		Table.drawDebug(stage);
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void hide() {
	}
	
	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}	

}
