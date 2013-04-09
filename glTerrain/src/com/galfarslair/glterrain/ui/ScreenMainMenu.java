package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.util.SystemInfo;

public class ScreenMainMenu extends UIScreen {

	//private Table container
	//private TerrainRunner.TerrainStarter terrainStarter;
	
	public ScreenMainMenu(TerrainRunner.TerrainStarter terrainStarter) {
		super();
		final TerrainRunner.TerrainStarter starter = terrainStarter;
						
		Label label = new Label("glTerrain Demo", skin);
		label.setFontScale(2);
		
		Texture texture = new Texture(Assets.getFile("Launcher.png"));
		TextureRegion region = new TextureRegion(texture);
		
		final CheckBox checkWire = new CheckBox("Wireframe overlay", skin);
		final CheckBox checkAutowalk = new CheckBox("Autowalk (devices w/o HW keys)", skin);
		
		final Label labTolerance = new Label("", skin);		
		final Slider sliderTolerance = new Slider(0.5f, 15.0f, 0.5f, false, skin);		
		sliderTolerance.setValue(2.0f);
		sliderTolerance.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				labTolerance.setText(String.format("LOD tolerance in pixels: %.1f", sliderTolerance.getValue()));
			}
		});
		
		if (Gdx.app.getType() == ApplicationType.Android) {
			checkAutowalk.setChecked(true);
			sliderTolerance.setValue(4.0f);
		}
		labTolerance.setText(String.format("LOD tolerance in pixels: %.1f", sliderTolerance.getValue()));
		
		TextButton btnStart = new TextButton("Start!", skin);
		btnStart.addListener(new ChangeListener() {			
			public void changed(ChangeEvent event, Actor actor) {
				starter.start(checkAutowalk.isChecked(), checkWire.isChecked(), sliderTolerance.getValue());
			}
		});
		
		TextButton btnGLInfo = new TextButton("Show GL Info", skin);
		btnGLInfo.addListener(new ChangeListener() {			
			public void changed(ChangeEvent event, Actor actor) {
				SystemInfo si = TerrainRunner.systemInfo;
				
				Dialog dlg = new Dialog("GL Info", skin);
				dlg.setSize(400, 360);				
				
				String info = 
				    "Version: " + si.getGLVersionString() + "\n" +   
				    "Renderer: " + si.getGLRenderer() + "\n" +
					"Resolution: " + String.format("%dx%d", si.getResolutionWidth(), si.getResolutionHeight()) + "\n" +
					"Mem Info: " + String.format("%d/%d/%d", si.getJavaHeapMemory() / 1024, si.getNativeHeapMemory() / 1024, si.getMaxRuntimeMemory() / 1024) + "\n" +
			      	"Extensions: ";				
				List list =	new List(si.getGLExtensions().toArray(), skin, "small");
				ScrollPane scrollPane = new ScrollPane(list, skin);
				scrollPane.setScrollingDisabled(true, false);
				scrollPane.setFadeScrollBars(false);
												
				dlg.getContentTable().add(new Label(info, skin, "small")).fillX();
				dlg.getContentTable().row();
				dlg.getContentTable().add(scrollPane).height(200).pad(4).fillX();				
				
				dlg.row();
				dlg.button("OK");
				dlg.key(Keys.ENTER, null).key(Keys.ESCAPE, null).key(Keys.BACK, null);
								
				dlg.show(stage);
			}
		});
		
		Table controls = new Table();
		//controls.debug();
		
		controls.add(label).fillX();
		controls.row();
		
		controls.add().expandY();
		controls.row();
		controls.add(checkWire).left();
		controls.row();
		controls.add(labTolerance).left();
		controls.row();
		controls.add(sliderTolerance).fillX().height(40);		
		controls.row();
		controls.add(checkAutowalk).left();
		controls.row();
		controls.add(btnGLInfo).right();
		controls.row();
		controls.add().expandY();
		controls.row();
								
		controls.add(btnStart).colspan(2).minHeight(60).fillX();
		controls.row();
		
		root.add(new Image(region));
		root.add(controls).pad(8).fillY();
		root.row();
		
		root.add(new Label("v" + TerrainRunner.VERSION, skin, "small")).colspan(2).right();		
		
		root.pack();
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);

	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, false);

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
