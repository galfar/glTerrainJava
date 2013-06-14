package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.galfarslair.glterrain.util.Assets;

public class UIScreen implements Screen {

	public interface FinishedNotifier {
		void onFinished();
	}
	
	protected static Skin skin;
	public static BitmapFont consoleFont;
	
	protected Stage stage;
	protected Table root;
	
	public static void initStatic() {
		skin = new Skin(Assets.getFile("uiSkin.json"));
		consoleFont = new BitmapFont(Assets.getFile("Consolas15.fnt"), skin.getRegion("Consolas15"), false);
		consoleFont.setColor(1f, 1f, 0.8f, 1f);
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
	}	

}
