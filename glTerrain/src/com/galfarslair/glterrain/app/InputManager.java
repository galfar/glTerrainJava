package com.galfarslair.glterrain.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;

public class InputManager extends InputAdapter {
		
	private InputMultiplexer multiplexer;
	private InputProcessor primaryProcessor;
	
	public InputManager() {
		multiplexer = new InputMultiplexer();		
		multiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(multiplexer);
	}
	
	public void setPrimaryProcessor(InputProcessor primaryProcessor) {
		if (this.primaryProcessor != null) {
			multiplexer.removeProcessor(this.primaryProcessor);
		}
		this.primaryProcessor = primaryProcessor;		
		multiplexer.addProcessor(0, this.primaryProcessor);		
	}
	
	public void addSupportProcessor(InputProcessor supportProcessor) {
		multiplexer.addProcessor(supportProcessor);		
	}
	
	public boolean hasKeyboard() {
		return Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard); 
	}
	
	public boolean hasMultitouch() {
		return Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen); 
	}
	
	public boolean hasAccelerometer() {
		return Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer); 
	}
	
	public boolean isShiftPressed() {
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);  
	}
	
	@Override 
	public boolean keyUp (int keycode) {
		switch (keycode) {
		case Keys.ESCAPE:
			Gdx.app.exit();
			return true;
		}		
		return false;
	}

	
}
