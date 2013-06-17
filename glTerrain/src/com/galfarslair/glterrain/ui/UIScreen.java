package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.glterrain.util.Assets;

public abstract class UIScreen implements Screen {

	public interface FinishedNotifier {
		void onFinished();
	}
	
	protected static Skin skin;
	public static BitmapFont consoleFont;
	public static Texture launcherTexture;
	public static TextureRegion launcherRegion;
	
	protected Stage stage;
	protected Table root;
	protected Table controls;
	
	public static void initStatic() {
		skin = new Skin(Assets.getFile("uiSkin.json"));
		consoleFont = new BitmapFont(Assets.getFile("Consolas15.fnt"), skin.getRegion("Consolas15"), false);
		consoleFont.setColor(1f, 1f, 0.8f, 1f);
		launcherTexture = new Texture(Assets.getFile("Launcher.png"));
		launcherRegion = new TextureRegion(launcherTexture);
	}
	
	public UIScreen() {
		stage = new Stage();
		root = new Table();
		root.setFillParent(true);		
		stage.addActor(root);
				
		Gdx.input.setInputProcessor(stage);
		
		Label label = new Label("glTerrain Demo", skin);
		label.setFontScale(2);		
		
		controls = new Table();
		
		root.debug();
		controls.debug();
		
		controls.add(label).fillX();
		controls.row();
				
		defineControls();
		
		controls.pack();
		
		root.add(new Image(launcherRegion));
		root.add(controls).pad(-8, 8, 0, 0).minWidth(420).fillY();
		root.row();
		
		root.add(new Label("v" + TerrainRunner.VERSION, skin, "small")).colspan(2).right();
		root.pack();
	}
	
	protected abstract void defineControls();
	
	protected void showMessageDlg(String text) {
		Dialog dlg = new Dialog("Message", skin);
					
		Label label = new Label(text, skin);
		label.setWrap(true);
		label.setAlignment(Align.left | Align.top);
			
		dlg.getContentTable().add(label).minWidth(500).minHeight(200).pad(4).fillX();		
		
		dlg.button("OK");
		dlg.key(Keys.ENTER, null).key(Keys.ESCAPE, null).key(Keys.BACK, null);
		
		dlg.show(stage);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 0.8f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		stage.act(delta);
		stage.draw();
		//Table.drawDebug(stage);
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
