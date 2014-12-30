package com.galfarslair.glterrain.ui;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.glterrain.util.Requirements;
import com.galfarslair.util.SystemInfo;

public class ScreenMainMenu extends UIScreen {

	private TerrainRunner.TerrainStarter terrainStarter;
	private Requirements requirements;
	private SystemInfo systemInfo;
	private Array<String> systemInfoLines = new Array<String>();
	
	public ScreenMainMenu(TerrainRunner.TerrainStarter terrainStarter, SystemInfo systemInfo, Requirements requirements) {		
		super();		
		this.terrainStarter = terrainStarter;
		this.requirements = requirements;
		this.systemInfo = systemInfo;
		buildSystemInfo();
	}
	
	protected void defineControls() {
		final TextButton btnGeoMip = new TextButton("GeoMipMapping", skin, "toggle");		
		final TextButton btnSoar = new TextButton("SOAR", skin, "toggle");
		
		final CheckBox checkWire = new CheckBox("Wireframe overlay", skin);
		final CheckBox checkAutowalk = new CheckBox("Autowalking", skin);
		
		final Label labTolerance = new Label("", skin);		
		final Slider sliderTolerance = new Slider(0.5f, 15.0f, 0.5f, false, skin);		
		sliderTolerance.setValue(2.0f);
		sliderTolerance.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				labTolerance.setText(String.format("LOD tolerance in pixels: %.1f", sliderTolerance.getValue()));
			}
		});
		
		if (Gdx.app.getType() == ApplicationType.Android) {			
			sliderTolerance.setValue(4.0f);
		}
		labTolerance.setText(String.format("LOD tolerance in pixels: %.1f", sliderTolerance.getValue()));
		
		TextButton btnStart = new TextButton("Start!", skin);
				
		btnStart.addListener(new ChangeListener() {			
			public void changed(ChangeEvent event, Actor actor) {
				TerrainRunner.TerrainMethod method = TerrainRunner.TerrainMethod.GeoMipMapping;
				if (btnSoar.isChecked()) {
					method = TerrainRunner.TerrainMethod.SOAR; 
				}
				
				if ((method == TerrainRunner.TerrainMethod.SOAR) && !requirements.soarAvailable()) {					
					showMessageDlg("Sorry, your GPU does not support all the features needed for running SOAR terrain rendering method.");
					return;
				}
				
				if ((method == TerrainRunner.TerrainMethod.GeoMipMapping) && checkWire.isChecked() && !requirements.wireframeOverlayAvailable()) {					
					showMessageDlg("Sorry, your GPU does not support all the features needed for wireframe overlay.");
					return;
				}
				
				if (!requirements.memoryAvaiable()) {
					showMessageDlg(String.format("Sorry, your device does not have enough RAM to run the terrain. " + 
				        "At least %dMiB app limit is needed.", Requirements.REQUIRED_MEMORY_MB));
					return;
				}
				
				if (!requirements.textureSizeOk()) {
					showMessageDlg(String.format("Sorry, your GPU does not support textures big enough for the terrain. " + 
					        "Textures at least %1$dx%1$d in size are needed.", Requirements.REQUIRED_TEXTURE_SIZE));
				    return;
				}
				
				terrainStarter.start(method,
						checkAutowalk.isChecked(), checkWire.isChecked(), sliderTolerance.getValue());
			}
		});
		
		TextButton btnSysInfo = new TextButton("System Info", skin);
		btnSysInfo.addListener(new ChangeListener() {			
			public void changed(ChangeEvent event, Actor actor) {
				Dialog dlg = new Dialog("System Info", skin);
				dlg.setSize(500, 400);				
				
				String info = 
				    "Version: " + systemInfo.getGLVersionString() + "\n" +   
				    "Renderer: " + systemInfo.getGLRenderer() + "\n" +
			      	"Details: ";				
				List<String> list =	new List<String>(skin, "small");
				list.setItems(systemInfoLines);
				ScrollPane scrollPane = new ScrollPane(list, skin);
				scrollPane.setScrollingDisabled(true, false);
				scrollPane.setFadeScrollBars(false);
				
				dlg.getContentTable().add(new Label(info, skin, "small")).fillX();
				dlg.getContentTable().row();
				dlg.getContentTable().add(scrollPane).height(240).pad(4).padTop(0).fillX();				
				
				dlg.row();
				dlg.button("  OK  ");
				dlg.key(Keys.ENTER, null).key(Keys.ESCAPE, null).key(Keys.BACK, null);
								
				dlg.show(stage);
			}
		});
		
		btnSoar.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				checkWire.setVisible(!btnSoar.isChecked());
			}
		});
		
		TextButton btnControls = new TextButton("Controls", skin);
		
		
		
		
		@SuppressWarnings("unused")
		ButtonGroup btnGroupMethod = new ButtonGroup(btnGeoMip, btnSoar);		
		btnGeoMip.setChecked(true);
				
		controls.add().expandY();
		controls.row();
		
		Table group = new Table();
		group.defaults().space(8).minWidth(160);		
		group.add(btnGeoMip).left();
		group.add(btnSoar).right();
		group.row();		
						
		controls.add(group).left();		
		controls.row();
				
		controls.add(labTolerance).left();
		controls.row();
		controls.add(sliderTolerance).fillX().height(40);		
		controls.row();
		controls.add(checkWire).left();
		controls.row();
		controls.add(checkAutowalk).left();
		controls.row();
		
		Table group2 = new Table();
		group2.defaults().space(8).minWidth(160);		
		group2.add(btnControls).left();
		group2.add(btnSysInfo).right();
		group2.row();	
		controls.add(group2).left().padTop(8);		
		controls.row();
						
		controls.add().expandY();
		controls.row();
								
		controls.add(btnStart).colspan(2).minHeight(60).fillX();
		controls.row();		
	}
	
	private void buildSystemInfo() {
		systemInfoLines.add("Display Resolution: " + String.format("%dx%d px", systemInfo.getDisplayWidthPx(), systemInfo.getDisplayHeightPx()));
		systemInfoLines.add("Display Size: " + String.format("%.2fx%.2f cm", systemInfo.getDisplayWidthCm(), systemInfo.getDisplayHeightCm()));
		systemInfoLines.add("Mem Info: " + String.format("%d/%d/%d MiB", systemInfo.getJavaHeapMemory() / 1024, systemInfo.getNativeHeapMemory() / 1024, systemInfo.getMaxRuntimeMemory() / 1024));
		systemInfoLines.add("GPU Vendor: " + systemInfo.getGLVendor());
		systemInfoLines.add("Max Texture Size: " + systemInfo.getMaxTextureSize());
		systemInfoLines.add("Max Vertex Textures: " + systemInfo.getMaxVertexTextureImageUnits());
		
		systemInfoLines.add("");
		systemInfoLines.add("Open GL Extensions:");
	    systemInfoLines.addAll(systemInfo.getGLExtensions().orderedItems());
	    
	    systemInfoLines.add("");
	    systemInfoLines.add("Misc Info:");
	    systemInfoLines.add("Pixels per cm: " + String.format("%.2fx%.2f", Gdx.graphics.getPpcX(), Gdx.graphics.getPpcY()));
	    
	    systemInfoLines.shrink();
	}

}
