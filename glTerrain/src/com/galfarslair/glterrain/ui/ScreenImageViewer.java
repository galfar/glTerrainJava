package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.util.Message;

public class ScreenImageViewer extends InputAdapter implements Screen {	
	
	private Image image;
	private Stage stage;
	private Message exitMessage;
	
	public ScreenImageViewer(String imagePath, Message exitMessage) {
		super();	
		this.exitMessage = exitMessage;
		stage = new Stage();		
		Texture texture = new Texture(Assets.getFile(imagePath));		
		image = new Image(texture);
		image.setScaling(Scaling.fit);		
		stage.addActor(image);				
	}
		
	public void show() {		
		UIScreen.inputManager.setPrimaryProcessor(this);
	}
		
	public void render(float delta) {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 0.8f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.draw();
	}
	
	public void resize(int width, int height) {
		image.setFillParent(true);		
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public boolean keyUp(int keycode) {	
		exitMessage.send();
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		exitMessage.send();
		return true;
	}

	public void pause() {		
	}

	public void resume() {		
	}

	public void hide() {		
	}

	public void dispose() {		
	}			
}
