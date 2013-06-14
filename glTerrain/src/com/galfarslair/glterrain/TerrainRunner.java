package com.galfarslair.glterrain;

import java.io.IOException;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.galfarslair.glterrain.ui.ScreenBuilding;
import com.galfarslair.glterrain.ui.ScreenMainMenu;
import com.galfarslair.glterrain.ui.UIScreen;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.glterrain.util.PlatformSupport;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.SequenceExecutor;
import com.galfarslair.util.SystemInfo;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class TerrainRunner extends InputAdapter implements ApplicationListener {
	
	public static final String VERSION = "0.26";
	
	public enum TerrainMethod {
		GeoMipMapping, BruteForce, SOAR
	}
	
	private enum State {
		MainMenu, SettingsMenu, TerrainPrepare, TerrainBuild, TerrainRun 
	}
	
	private final float fieldOfView = 45;
	private final Color skyColor = new Color(0.5f, 0.625f, 0.75f, 1.0f);	
	private final float observerHeight = 1.8f;
	
	public static PlatformSupport platfromSupport;
	
	private PerspectiveCamera camera;
	private float cameraYaw = 135;
	private float cameraPitch = -15;	
	private final Vector3 cameraPos = new Vector3(0, 0, 60);
	private final Vector3 cameraUp = new Vector3(0, 0, 1);
	private float lodTolerance = 1.5f;
		
	private TerrainMethod terrainMethod = /*TerrainMethod.SOAR;//*/TerrainMethod.GeoMipMapping;
	private TerrainMesh terrainMesh;
	private TerrainRenderer terrainRenderer;
	private FileHandle heightMapFile;
	
	private Texture groundTexture;
	private Texture detailTexture;
	
	private FPSLogger fpsLogger;
	private BitmapFont font;
	private SpriteBatch batch;	
	private long timer;
	
	private State state;	
	
	private UIScreen currentScreen;
	private ScreenMainMenu screenMainMenu;
	
	private boolean benchmarkMode;
	private boolean autoWalk;
	private boolean wireOverlay;
	private boolean capturedMouse;
	
	private final TerrainRunner instance = this;
	
	ShaderProgram mipmapShader;
	ShaderProgram mipmapShaderWire;
	
	private TerrainBuilder terrainBuilder;
	
	public static SystemInfo systemInfo = new SystemInfo();
	
	public interface TerrainStarter {
		void start(TerrainMethod method, boolean autoWalk, boolean wireOverlay, float tolerance);
	}	
	
	public TerrainRunner(PlatformSupport platfromSupport) {
		this.platfromSupport = platfromSupport;
	}
	
	@Override
	public void create() {
		systemInfo.gather();
		
		// useful for benchmark mode 
		Gdx.graphics.setVSync(false);
		//Gdx.input.setCursorCatched(true);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		ShaderProgram.pedantic = false;
		
		UIScreen.initStatic();
		fpsLogger = new FPSLogger();		
		font = UIScreen.consoleFont;		
		batch = new SpriteBatch();		
						
		Gdx.gl.glDisable(GL10.GL_LIGHTING);
		Gdx.gl.glDisable(GL10.GL_BLEND);
	    Gdx.gl.glEnable(GL10.GL_CULL_FACE);
		Gdx.gl.glEnable(GL10.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL10.GL_TEXTURE_2D);
		
		Gdx.gl.glFrontFace(GL10.GL_CW);
		Gdx.gl.glCullFace(GL10.GL_BACK);		
		Gdx.gl.glDepthFunc(GL10.GL_LEQUAL);
		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			    
	    screenMainMenu = new ScreenMainMenu(new TerrainStarter() {
			@Override
			public void start(TerrainMethod method, boolean autoWalkEnabled, boolean wireOverlayEnabled, float tolerance) {				
				terrainMethod = method;
				lodTolerance = tolerance;
				autoWalk = autoWalkEnabled;
				wireOverlay = wireOverlayEnabled;
				
				setScreen(new ScreenBuilding());
				terrainBuilder = new TerrainBuilder();
				state = State.TerrainBuild;
			}
		});
	    
	    state = State.MainMenu;
	    setScreen(screenMainMenu);
	    
	    scratchpad();
	}

	private void scratchpad() {
		//platfromSupport.enableWireframe();
		
	}
	
	public void setScreen(UIScreen screen) {
		if (currentScreen != null) {
			currentScreen.hide();
		}
		currentScreen = screen;		
		if (currentScreen != null) {
			currentScreen.show();
			currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}
	
	@Override
	public void render() {
		if (state == State.TerrainBuild) {
			terrainBuilder.execute();	
		}
		
		if (currentScreen != null) {
			currentScreen.render(Gdx.graphics.getDeltaTime());
			return;
		}
		
		checkInput();
		
		Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1);
		Gdx.gl.glClearDepthf(1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	
		camera.update();					
		
		groundTexture.bind(0);
		detailTexture.bind(1);
		
		terrainMesh.update(camera, lodTolerance);
		terrainRenderer.render(camera);		
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0); // Needs to be done for sprites & fonts to work (they bind to current tex unit)
				
		batch.begin();		
		batch.enableBlending();		
		font.drawMultiLine(batch, 
				"FPS: " + Gdx.graphics.getFramesPerSecond() + "\n" + 
				String.format("CamPos: %.1f %.1f %.1f\n", camera.position.x, camera.position.y, camera.position.z) + 
				String.format("CamDir: %.2f %.2f\n", cameraYaw, cameraPitch) + 
				String.format("Tolerance: %.1fpx", lodTolerance),
				5, Gdx.graphics.getHeight());
		batch.end();
		
		fpsLogger.log();
	}

	@Override
	public void resize(int width, int height) {		
		if (currentScreen != null) {
			currentScreen.resize(width, height);
			return;
		}
		
		Gdx.gl.glViewport(0, 0, width, height);
		if (camera != null) {
			cameraPos.set(camera.position);
		}
		camera = new PerspectiveCamera(fieldOfView, width, height);
		camera.near = 0.4f;
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
	
	@Override
	public boolean keyDown (int keycode) {
		switch (keycode) {
		case Keys.O: 
			wireOverlay = !wireOverlay;
			if (wireOverlay) {
				((MipMapRenderer)terrainRenderer).setShader(mipmapShaderWire);
			} else {
				((MipMapRenderer)terrainRenderer).setShader(mipmapShader);
			}
			return true;
		case Keys.PLUS:
			lodTolerance = Math.min(lodTolerance + 0.5f, 15f);
			return true;
		case Keys.MINUS:
			lodTolerance = Math.max(lodTolerance - 0.5f, 0.5f);
			return true;
		case Keys.L: 
			capturedMouse = !capturedMouse;
			Gdx.input.setCursorCatched(capturedMouse);
			return true;
		}
		return false;
	}
	
	private void checkInput() {
		final float mouseLookSensitivity = 0.1f;
		final float walkSpeed = 4f;		
		final float superMove = 1/20f;
		
		int dx = Gdx.input.getDeltaX();
		int dy = Gdx.input.getDeltaY();
		
		if (capturedMouse && ((dx != 0) || (dy != 0))) {
			updateCameraDirection(dx * mouseLookSensitivity, -dy * mouseLookSensitivity);
		}		
		
		float moveSpeed = 0;
		if (autoWalk) {
			moveSpeed = terrainMesh.getSize() / 100.0f;
		}
		
		if (hasKeyboard()) {
			if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
				Gdx.app.exit();
			}
						
			if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {				
				if (!isShiftPressed()) {
					moveSpeed = walkSpeed;
				} else {
				}					
			} else if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
				if (!isShiftPressed()) {
					moveSpeed = -walkSpeed;
				} else {
				} 
			}
			
			if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
				moveSpeed *= (terrainMesh.getSize() * superMove);
			}			
		} else {
			
		}
		
		moveSpeed *= Gdx.graphics.getDeltaTime();		
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
	
	private enum TerrainBuildState {
		Prepare, Textures, HeightMap, MeshGeo, MeshVisual, Finished
	}
	
	private class TerrainBuilder extends SequenceExecutor<TerrainBuildState> {

		private HeightMap heightMap;
		
		public TerrainBuilder() {
			super(TerrainBuildState.class);
		}

		@Override
		protected void processState() {
			switch (current) {
			case Prepare:
				timer = System.nanoTime();
				
				// Terrain mesh & renderer 
			    switch (terrainMethod) {
			    case GeoMipMapping:
			    	terrainMesh = new MipMapMesh();
			    	
			    	String vert = Gdx.files.internal("data/shaders/geo.vert").readString();
			    	String frag = Gdx.files.internal("data/shaders/geo.frag").readString();
			    	mipmapShader = new ShaderProgram(vert, frag);
			    	Utils.logInfo(mipmapShader.getLog());
			    	
			    	vert = "#define DRAW_EDGES\r\n" + vert;
			    	frag = "#define DRAW_EDGES\r\n" + frag;
			    	mipmapShaderWire = new ShaderProgram(vert, frag);
			    	Utils.logInfo(mipmapShaderWire.getLog());
			    		    	
					ShaderProgram shaderSkirt = new ShaderProgram(Gdx.files.internal("data/shaders/geoSkirt.vert"), Gdx.files.internal("data/shaders/geoSkirt.frag"));		
					Utils.logInfo(shaderSkirt.getLog());
			    	
			    	terrainRenderer = new MipMapRenderer(mipmapShader, shaderSkirt);
			    	
			    	if (wireOverlay) {
						((MipMapRenderer)terrainRenderer).setShader(mipmapShaderWire);
					}					
			    	
			    	heightMapFile = Assets.getFile("terrains/NewVolcanoes-HF1k.hraw");	    	
			    	break;
			    case BruteForce:
			    	
			    	break;
			    case SOAR:
			    	terrainMesh = new SoarMesh();
			    	terrainRenderer = new SoarRenderer(Gdx.files.internal("data/shaders/soar.vert"), Gdx.files.internal("data/shaders/geo.frag"));
			    	heightMapFile = Assets.getFile("terrains/NewVolcanoes-HF513.hraw");	    	
			    	break;
			    }
			    
			    //heightMapFile = Assets.getFile("terrains/NewVolcanoes-HF8k.hraw");				
				return;
				
			case Textures:
				String groundTexPath = null;
				if (Gdx.app.getType() == ApplicationType.Desktop) {
					groundTexPath = "terrains/NewVolcanoes-DXT1.ktx";
				} else if (Gdx.app.getType() == ApplicationType.Android) {
					groundTexPath = "terrains/NewVolcanoes-ETC1.ktx";
				}		
				
				try {
					groundTexture = Assets.loadKtxTexture(groundTexPath);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();			
				}  
				Utils.logElapsed("Ground tex loaded in: ", timer);
				groundTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
				
				detailTexture = new Texture(Assets.getFile("terrains/Detail.jpg"), true);
				detailTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
				detailTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
				return;
				
			case HeightMap:
				heightMap = new HeightMap();
			    try {
					heightMap.loadFromRaw(heightMapFile);
				} catch (HeightMap.HeightmapException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
				
			case MeshGeo:
				try {
			    	terrainMesh.build(heightMap);
				} catch (TerrainException e) {		
					e.printStackTrace();
				}					    
				return;
				
				
			case MeshVisual:
				try {
					terrainRenderer.assignMesh(terrainMesh);
				} catch (TerrainException e) {
					e.printStackTrace();
				}
				
				if (Utils.elapsedTimeMs(timer) < 1000) {
					Utils.delay(500);
				}
				return;
						
			case Finished:
				state = State.TerrainRun;
			    setScreen(null);
			    capturedMouse = true;
				Gdx.input.setCursorCatched(capturedMouse);				
				
				// To build camera etc.
				resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				
				Gdx.input.setInputProcessor(instance);
				return;
			}
		}
		
	}
	
	
}
