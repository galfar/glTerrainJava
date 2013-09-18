package com.galfarslair.glterrain.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CameraController {
	
	private PerspectiveCamera camera;
	private float yaw;
	private float pitch;
	
	private boolean autoWalk;
	private float speedFactor;
	
	private InputManager input;
	private TouchInputAdapter touchInputAdapter;
	
	public CameraController(InputManager input, 
			Vector3 position, Vector3 up, float yaw, float pitch) {
		this.input = input;	
		this.yaw = yaw;
		this.pitch = pitch;		
		
		camera = new PerspectiveCamera();
		camera.near = 0.4f;
		camera.far = 1e04f;		
		camera.up.set(position);		
		camera.position.set(up);		
		updateCameraDirection(0, 0);
		
		if (input.hasMultitouch()) {
			touchInputAdapter = new TouchInputAdapter();
			input.addSupportProcessor(touchInputAdapter);
		}
	}
	
	public PerspectiveCamera getCamera() {
		return camera;
	}
	
	public void setAutoWalk(boolean autoWalk) {
		this.autoWalk = autoWalk;
	}
	
	public void setSpeedFactor(float factor) {
		this.speedFactor = factor;
	}
	
	public void onScreenResize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
	}
	
	public void update(float deltaTime, boolean capturedMouse) {
		final float mouseLookSensitivity = 0.1f;
		final float walkSpeed = 4f;		
		final float superMove = 1 / 20f;
		
		int dx = Gdx.input.getDeltaX();
		int dy = Gdx.input.getDeltaY();
		
		if (capturedMouse && ((dx != 0) || (dy != 0))) {
			updateCameraDirection(dx * mouseLookSensitivity, -dy * mouseLookSensitivity);
		}		
		
		float moveSpeed = 0;
		if (autoWalk) {
			moveSpeed = speedFactor / 100.0f;
		}
		
		if (input.hasKeyboard()) {			
						
			if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {				
				if (!input.isShiftPressed()) {
					moveSpeed = walkSpeed;
				} else {
				}					
			} else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
				if (!input.isShiftPressed()) {
					moveSpeed = -walkSpeed;
				} else {
				} 
			}
			
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
				moveSpeed *= (speedFactor * superMove);
			}			
		} else {
		
			if (touchInputAdapter.leftPointer >= 0) {
				float pos = touchInputAdapter.leftTouchPos.y;
				int height = Gdx.graphics.getHeight();
				
				if (pos > height * 0.75) {
					moveSpeed = -walkSpeed;
				} else if (pos > height * 0.25) {
					moveSpeed = walkSpeed;
				} else {
					moveSpeed = walkSpeed * (speedFactor * superMove);
				}
			}			
		}
		
		moveSpeed *= deltaTime;		
		camera.position.add(camera.direction.x * moveSpeed, camera.direction.y * moveSpeed, camera.direction.z * moveSpeed);
		
		camera.update();
	}
	
	private void updateCameraDirection(float yawChange, float pitchChange) {
		yaw += yawChange;
		pitch += pitchChange;
		yaw = yaw > 360 ? yaw - 360 : yaw;
		yaw = yaw < 0 ? yaw + 360 : yaw;
		pitch = MathUtils.clamp(pitch, -89, 89);
		camera.direction.set(
				-MathUtils.cosDeg(yaw) * MathUtils.cosDeg(pitch), 
				MathUtils.sinDeg(yaw) * MathUtils.cosDeg(pitch), 
				MathUtils.sinDeg(pitch));		
	}
	
	private class TouchInputAdapter extends InputAdapter {
		
		static final float LEFT_PART_PERCENTAGE = 0.33f;
		
		int leftPointer = -1;
		int rightPointer = -1;
		final Vector2 leftTouchPos = new Vector2();
		
		public boolean touchDown (int screenX, int screenY, int pointer, int button) {			
			if (screenX < (Gdx.graphics.getWidth() * LEFT_PART_PERCENTAGE)) {
				leftPointer = pointer;
				leftTouchPos.set(screenX, screenY);
			} else {
				rightPointer = pointer;
			}						
			return true;
		}

		public boolean touchUp (int screenX, int screenY, int pointer, int button) {			
			if (pointer == leftPointer) {
				leftPointer = -1;
			}
			if (pointer == rightPointer) {
				rightPointer = -1;
			}					
			return true;
		}		
	}

}
