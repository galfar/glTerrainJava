package com.galfarslair.glterrain;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.galfarslair.glterrain.mipmap.MipMapMesh;
import com.galfarslair.glterrain.mipmap.MipMapRenderer;
import com.galfarslair.glterrain.soar.SoarMesh;
import com.galfarslair.glterrain.soar.SoarRenderer;
import com.galfarslair.glterrain.ui.ScreenMainMenu;
import com.galfarslair.glterrain.ui.UIScreen;
import com.galfarslair.util.Utils;
import com.galfarslair.util.FeatureSupport;
import com.galfarslair.util.Utils.TerrainException;

public class TerrainRunner implements ApplicationListener {
	
	private enum TerrainMethod {
		GeoMipMapping, BruteForce, SOAR
	}
	
	private enum State {
		MainMenu, SettingsMenu, TerrainBuild, TerrainRun 
	}
	
	private final float fieldOfView = 45;
	private final Color skyColor = new Color(0.5f, 0.625f, 0.75f, 1.0f);	
	private final float observerHeight = 1.8f;
	
	private PerspectiveCamera camera;
	private float cameraYaw = 135;
	private float cameraPitch = -15;	
	private final Vector3 cameraPos = new Vector3(0, 0, 60);
	private final Vector3 cameraUp = new Vector3(0, 0, 1);
		
	private TerrainMethod method = TerrainMethod.GeoMipMapping;
	private TerrainMesh terrainMesh;
	private TerrainRenderer terrainRenderer;
	
	private Texture groundTexture;
	private Texture detailTexture;
	
	private FPSLogger fpsLogger;
	private BitmapFont font;
	private SpriteBatch batch;
	private FeatureSupport wireframeSupport; 
	
	private UIScreen currentScreen;
	private ScreenMainMenu screenMainMenu;
		
	private boolean isFlying = true;
	
	private boolean benchmarkMode;
		
	private Pixmap heightMap;
	
	public TerrainRunner(FeatureSupport wireframeSupport) {
		this.wireframeSupport = wireframeSupport;
	}
	
	@Override
	public void create() {
		// useful for benchmark mode 
		Gdx.graphics.setVSync(false);
		Gdx.input.setCursorCatched(true);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		ShaderProgram.pedantic = false;
				
		fpsLogger = new FPSLogger();
		font = new BitmapFont(Gdx.files.internal("data/Consolas15.fnt"), false);
		batch = new SpriteBatch();
		
		UIScreen.initStatic();
		screenMainMenu = new ScreenMainMenu();
						
		Gdx.gl.glDisable(GL10.GL_LIGHTING);
		Gdx.gl.glDisable(GL10.GL_BLEND);
	    Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		
		Gdx.gl.glFrontFace(GL10.GL_CW);
		Gdx.gl.glCullFace(GL10.GL_BACK);		
		Gdx.gl.glDepthFunc(GL10.GL_LEQUAL);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// Textures
		groundTexture = new Texture(Gdx.files.internal("data/Terrains/Volcanoes/VolcanoesTX.jpg"), true);
		groundTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
		//groundTexture.setWrap(TextureWrap.MirroredRepeat, TextureWrap.MirroredRepeat);
		detailTexture = new Texture(Gdx.files.internal("data/Terrains/Detail.jpg"), true);
		detailTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
		detailTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
		
	    switch (method) {
	    case GeoMipMapping:
	    	terrainMesh = new MipMapMesh();
	    	
	    	ShaderProgram shader = new ShaderProgram(Gdx.files.internal("data/Shaders/geo.vert"), Gdx.files.internal("data/Shaders/geo.frag"));		
			Utils.logInfo(shader.getLog());
			ShaderProgram shaderSkirt = new ShaderProgram(Gdx.files.internal("data/Shaders/geoSkirt.vert"), Gdx.files.internal("data/Shaders/geoSkirt.frag"));		
			Utils.logInfo(shaderSkirt.getLog());
	    	
	    	terrainRenderer = new MipMapRenderer(shader, shaderSkirt);
	    	break;
	    case BruteForce:
	    	
	    	break;
	    case SOAR:
	    	terrainMesh = new SoarMesh();
	    	terrainRenderer = new SoarRenderer(Gdx.files.internal("data/Shaders/soar.vert"), Gdx.files.internal("data/Shaders/textured.frag"));
	    	break;
	    }
	    
	    heightMap = new Pixmap(Gdx.files.internal("data/Terrains/Volcanoes/Volcanoes1025.png"));
	    //heightMap = new Pixmap(Gdx.files.internal("data/Terrains/Volcanoes/VolcanoesMini.png"));
	  			    
	    try {
	    	terrainMesh.build(heightMap);
		} catch (TerrainException e) {		
			e.printStackTrace();
		}
	    heightMap.dispose();	    
			    
	    try {
			terrainRenderer.assignMesh(terrainMesh);
		} catch (TerrainException e) {
			e.printStackTrace();
		}
	    
	    if (wireframeSupport != null) {
	    	//wireframeSupport.enable();
	    }
	    
	    currentScreen = screenMainMenu;	    
	}

	@Override
	public void render() {
		/*if (currentScreen != null) {
			currentScreen.render(Gdx.graphics.getDeltaTime());
			return;
		}*/
		
		checkInput();
		
		Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1);
		Gdx.gl.glClearDepthf(1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	
		camera.update();					
		
		groundTexture.bind(0);
		detailTexture.bind(1);
		
		terrainMesh.update(camera);
		terrainRenderer.render(camera);		
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0); // Needs to be done for sprites & fonts to work (they bind to current tex unit)
				
		batch.begin();		
		batch.enableBlending();		
		font.drawMultiLine(batch, 
				"FPS: " + Gdx.graphics.getFramesPerSecond() + "\n" + 
				String.format("CamPos: %.1f %.1f %.1f\n", camera.position.x, camera.position.y, camera.position.z) + 
				String.format("CamDir: %.2f %.2f\n", cameraYaw, cameraPitch), 
				5, Gdx.graphics.getHeight());
		batch.end();
		
		fpsLogger.log();
	}

	@Override
	public void resize(int width, int height) {		
		/*if (currentScreen != null) {
			currentScreen.resize(width, height);
			return;
		}*/
		
		Gdx.gl.glViewport(0, 0, width, height);
		if (camera != null) {
			cameraPos.set(camera.position);
		}
		camera = new PerspectiveCamera(fieldOfView, width, height);
		camera.near = 0.1f;
		camera.far = 1e04f;		
		camera.up.set(cameraUp);		
		camera.position.set(cameraPos);
		updateCameraDirection(0, 0);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	@Override
	public void dispose() {
		terrainRenderer.dispose();
		groundTexture.dispose();
		detailTexture.dispose();
		font.dispose();
		batch.dispose();
	}
	
	boolean hasKeyboard() {
		return Gdx.input.isPeripheralAvailable(Peripheral.HardwareKeyboard); 
	}
	
	boolean isShiftPressed() {
		return Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);  
	}
	
	private void checkInput() {
		final float mouseLookSensitivity = 0.1f;
		final float walkSpeed = 2f;		
		final float superMove = 1/20f;
		
		int dx = Gdx.input.getDeltaX();
		int dy = Gdx.input.getDeltaY();
		
		if ((dx != 0) || (dy != 0)) {
			updateCameraDirection(dx * mouseLookSensitivity, -dy * mouseLookSensitivity);
		}		
		
		float moveSpeed = 0;
		float flySpeed = 0;
		
		if (hasKeyboard()) {
			if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
				Gdx.app.exit();
			}
						
			if (Gdx.input.isKeyPressed(Keys.UP)) {				
				if (!isShiftPressed()) {
					moveSpeed = walkSpeed;
				} else {
					flySpeed = walkSpeed;
				}					
			} else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				if (!isShiftPressed()) {
					moveSpeed = -walkSpeed;
				} else {
					flySpeed = -walkSpeed;
				} 
			}
			
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
				moveSpeed *= (terrainMesh.getSize() * superMove);
				flySpeed *= (terrainMesh.getSize() * superMove);
			}
			
			moveSpeed *= Gdx.graphics.getDeltaTime();
			flySpeed *= Gdx.graphics.getDeltaTime();			
		} else {
			
		}
				
		camera.position.add(camera.direction.x * moveSpeed, camera.direction.y * moveSpeed, camera.direction.z * moveSpeed);
	}
	
	private void updateCameraDirection(float yawChange, float pitchChange) {
		cameraYaw += yawChange;
		cameraPitch += pitchChange;
		cameraYaw = cameraYaw > 360 ? cameraYaw - 360 : cameraYaw;
		cameraYaw = cameraYaw < 0 ? cameraYaw + 360 : cameraYaw;
		cameraPitch = MathUtils.clamp(cameraPitch, -89, 89);
		camera.direction.set(
				-MathUtils.cosDeg(cameraYaw) * MathUtils.cosDeg(cameraPitch), 
				MathUtils.sinDeg(cameraYaw) * MathUtils.cosDeg(cameraPitch), 
				MathUtils.sinDeg(cameraPitch));		
	}
	
}
