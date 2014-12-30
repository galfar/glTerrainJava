package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.glterrain.app.InputManager;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.util.Utils;

public abstract class UIScreen implements Screen {

	public interface FinishedNotifier {
		void onFinished();
	}
	
	protected static InputManager inputManager;
	
	protected static Skin skin;
	
	public static BitmapFont consoleFont;
	public static Texture launcherTexture;
	public static TextureRegion launcherRegion;
	
	public static final int UI_WIDTH_PX = 800;
	public static final int UI_HEIGHT_PX = 480;
	public static final int UI_WIDTH_TRESHOLD_CM = 8;	
	
	protected Stage stage;
	protected Table root;
	protected Table controls;
	
	private ScreenViewport screenViewport;
	private FitViewport fitViewport;
	private float uiWidthInCm;
	
	public static void initStatic(InputManager input) {
		inputManager = input;
		skin = new UISkin(Assets.getFile("uiSkin.json"));		
		consoleFont = new BitmapFont(Assets.getFile("Consolas20.fnt"));
		consoleFont.setColor(1f, 1f, 0.8f, 1f);
		launcherTexture = new Texture(Assets.getFile("Launcher.png"));
		launcherRegion = new TextureRegion(launcherTexture);
	}
	
	public UIScreen() {
		fitViewport = new FitViewport(UI_WIDTH_PX, UI_HEIGHT_PX);
		screenViewport = new ScreenViewport();
		uiWidthInCm = Utils.calcPhysicalSizeInCm(UI_WIDTH_PX);
				
		stage = new Stage(screenViewport);		
		root = new Table();
		root.setFillParent(true);		
		stage.addActor(root);		
				
		inputManager.setPrimaryProcessor(stage);
		
		Label label = new Label("glTerrain Demo", skin);
		label.setFontScale(2);		
		
		controls = new Table();
		
		controls.add(label).fillX();
		controls.row();
				
		defineControls();
		
		controls.pack();
		
		root.add(new Image(launcherRegion));
		root.add(controls).pad(-8, 8, 0, 0).minWidth(420).fillY();
		root.row();
		
		root.add(new Label("v" + TerrainRunner.VERSION, skin, "small")).colspan(2).right();
		root.pack();
		
		stage.setDebugUnderMouse(!true);
		stage.setDebugParentUnderMouse(!true);
		stage.setDebugTableUnderMouse(!true);
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
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act(delta);
		stage.draw();		
	}

	@Override
	public void resize(int width, int height) {
		float displayWidthInCm = Utils.calcPhysicalSizeInCm(width);
		if ((uiWidthInCm < UI_WIDTH_TRESHOLD_CM) || (displayWidthInCm < uiWidthInCm)) {
			// Fit UI to display size if:
			// - UI is smaller than 8 cm
			// - display is smaller than UI size in cm 
			// Even better would be: fix the size to 8cm if display is 8+cm, or if smaller fit ui to display
			stage.setViewport(fitViewport);
		} else {
			stage.setViewport(screenViewport);
		}
		
		stage.getViewport().update(width, height, true);
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
	
	static class UISkin extends Skin {
		public UISkin(FileHandle file) {			
			super(file);
		}

		@Override
		public Drawable getDrawable (String name) {
			if (name.equalsIgnoreCase("empty")) {
				// No-op drawable that can be used by skin elements.
				// No support in GDX to add it directly to skin file.
				return new BaseDrawable();
			}			
			return super.getDrawable(name);		
		}
	}

}
