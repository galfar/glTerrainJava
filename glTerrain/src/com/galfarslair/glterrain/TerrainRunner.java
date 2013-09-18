package com.galfarslair.glterrain;

import java.io.IOException;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.galfarslair.glterrain.app.CameraController;
import com.galfarslair.glterrain.app.InputManager;
import com.galfarslair.glterrain.mipmap.MipMapMesh;
import com.galfarslair.glterrain.mipmap.MipMapRenderer;
import com.galfarslair.glterrain.soar.SoarMesh;
import com.galfarslair.glterrain.soar.SoarRenderer;
import com.galfarslair.glterrain.ui.ScreenBuilding;
import com.galfarslair.glterrain.ui.ScreenMainMenu;
import com.galfarslair.glterrain.ui.UIScreen;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.glterrain.util.PlatformSupport;
import com.galfarslair.glterrain.util.Requirements;
import com.galfarslair.glterrain.vtf.VtfMesh;
import com.galfarslair.glterrain.vtf.VtfRenderer;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.SequenceExecutor;
import com.galfarslair.util.SystemInfo;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class TerrainRunner extends InputAdapter implements ApplicationListener {
	
	public static final String VERSION = "0.32";
	
	public enum TerrainMethod {
		GeoMipMapping, BruteForce, SOAR, VTF
	}
	
	private enum State {
		MainMenu, TerrainBuild, TerrainRun, SettingsMenu 
	}
		
	private final Color skyColor = new Color(0.5f, 0.625f, 0.75f, 1.0f);	
	public static PlatformSupport platfromSupport;
		
	// starting camera props
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
	private boolean capturedMouse;
	private String methodName;
	
	private State state;	
	
	private UIScreen currentScreen;
	private ScreenMainMenu screenMainMenu;
	
	private boolean benchmarkMode;	
	private boolean wireOverlay;
		
	private final TerrainRunner instance = this;
	
	ShaderProgram mipmapShader;
	ShaderProgram mipmapShaderWire;
	
	private TerrainBuilder terrainBuilder;
	private InputManager input;
	private CameraController camController;
		
	public static SystemInfo systemInfo = new SystemInfo();
	public static Requirements requirements = new Requirements(systemInfo);
		
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
		
		input = new InputManager();
		UIScreen.initStatic(input);
		camController = new CameraController(input, cameraPos, cameraUp, cameraYaw, cameraPitch);
		
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
				wireOverlay = wireOverlayEnabled;
				
				camController.setAutoWalk(autoWalkEnabled);
				
				setScreen(new ScreenBuilding());
				terrainBuilder = new TerrainBuilder();
				state = State.TerrainBuild;
			}
		}, systemInfo, requirements);
	    
	    
	    state = State.MainMenu;
	    setScreen(screenMainMenu);
	    
	    
		
	    
	    // DEBUG
	    /*setScreen(new ScreenBuilding());
	    terrainMethod = TerrainMethod.VTF;
	    state = State.TerrainBuild;
	    terrainBuilder = new TerrainBuilder();
	    */
	    scratchpad();
	}

	private void scratchpad() {
//		platfromSupport.enableWireframe();
		
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
	
	private String statsString;
	
	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		if (state == State.TerrainBuild) {
			terrainBuilder.execute();
		}
		
		if (currentScreen != null) {
			currentScreen.render(deltaTime);
			return;
		}		
		
		if (deltaTime > 0.5) {
			// Usually after terrain build there's big delay which could mess up with input timing etc.
			deltaTime = 0;
		}		
		
		camController.update(deltaTime, capturedMouse);
		final PerspectiveCamera camera = camController.getCamera();
		
		Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1);
		Gdx.gl.glClearDepthf(1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		groundTexture.bind(0);
		detailTexture.bind(1);
		
		terrainMesh.update(camera, lodTolerance);
		terrainRenderer.render(camera);		
		
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0); // Needs to be done for sprites & fonts to work (they bind to current tex unit)
				
		batch.begin();		
		batch.enableBlending();
		
		statsString = 
				"FPS: " + Gdx.graphics.getFramesPerSecond() + "\n" + 
				String.format("CamPos: %.1f %.1f %.1f\n", camera.position.x, camera.position.y, camera.position.z) + 
				String.format("CamDir: %.2f %.2f\n", cameraYaw, cameraPitch) + 
				String.format("Tolerance: %.1fpx", lodTolerance);
		
		font.drawMultiLine(batch, statsString, 5, Gdx.graphics.getHeight());
		font.draw(batch, methodName, Gdx.graphics.getWidth() - font.getBounds(methodName).width, Gdx.graphics.getHeight());
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
		camController.onScreenResize(width, height);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	
	@Override
	public void dispose() {		
		if (terrainRenderer != null) {			
			terrainRenderer.dispose();
			groundTexture.dispose();
			detailTexture.dispose();
		}
		font.dispose();
		batch.dispose();
	}	
	
	@Override
	public boolean keyDown (int keycode) {
		switch (keycode) {
		case Keys.O:
			if (terrainMethod != TerrainMethod.SOAR) {
				wireOverlay = !wireOverlay;
				if (wireOverlay) {
					((MipMapRenderer)terrainRenderer).setShader(mipmapShaderWire);
				} else {
					((MipMapRenderer)terrainRenderer).setShader(mipmapShader);
				}
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
		case Keys.F1:
		case Keys.MENU:
			// TODO: show instructions etc.
			// What to do on devices with no menu button??
			return true;
		}
		return false;
	}
		
	private enum TerrainBuildState {
		Prepare, Textures, HeightMap, MeshGeo, MeshVisual, Finished
	}
	
	private class TerrainBuilder extends SequenceExecutor<TerrainBuildState> {

		private HeightMap heightMap;
		private ScreenBuilding screen;
		
		public TerrainBuilder() {
			super(TerrainBuildState.class);
			screen = (ScreenBuilding)currentScreen;
		}

		@Override
		protected void processState() {
			String vert, frag;
			
			timer = System.nanoTime();	
			
			switch (current) {
			case Prepare:
				// Terrain mesh & renderer 
			    switch (terrainMethod) {
			    case GeoMipMapping:
			    	methodName = "GeoMipMapping";
			    	terrainMesh = new MipMapMesh();
			    	
			    	vert = Assets.getClasspathFile("shaders/geo.vert").readString();
			    	frag = Assets.getClasspathFile("shaders/geo.frag").readString();
			    	mipmapShader = new ShaderProgram(vert, frag);
			    	Utils.logInfo(mipmapShader.getLog());
			    	
			    	vert = "#define DRAW_EDGES\r\n" + vert;
			    	frag = "#define DRAW_EDGES\r\n" + frag;
			    	mipmapShaderWire = new ShaderProgram(vert, frag);
			    	Utils.logInfo(mipmapShaderWire.getLog());
			    		    	
					ShaderProgram shaderSkirt = new ShaderProgram(
							Assets.getClasspathFile("shaders/geoSkirt.vert"),
							Assets.getClasspathFile("shaders/geoSkirt.frag"));		
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
			    	methodName = "SOAR";
			    	terrainMesh = new SoarMesh();
			    	terrainRenderer = new SoarRenderer(
			    			Assets.getClasspathFile("shaders/soar.vert"), 
			    			Assets.getClasspathFile("shaders/geo.frag"));
			    	heightMapFile = Assets.getFile("terrains/NewVolcanoes-HF513.hraw");	    	
			    	break;
			    case VTF:
			    	methodName = "VTF";
			    	
			    	vert = Assets.getClasspathFile("shaders/vtf.vert").readString();
			    	frag = Assets.getClasspathFile("shaders/vtf.frag").readString();
			    	ShaderProgram shader = new ShaderProgram(vert, frag);
			    	Utils.logInfo(shader.getLog());
			    	
			    	vert = "#define DRAW_EDGES\r\n" + vert;
			    	frag = "#define DRAW_EDGES\r\n" + frag;
			    	ShaderProgram shaderWire = new ShaderProgram(vert, frag);
			    	Utils.logInfo(shaderWire.getLog());
			    	
			    	terrainMesh = new VtfMesh();
			    	terrainRenderer = new VtfRenderer(shaderWire);
			    	heightMapFile = Assets.getFile("terrains/NewVolcanoes-HF1k.hraw");
			    	break;
			    }
			    
			    //heightMapFile = Assets.getFile("terrains/NewVolcanoes-HF8k.hraw");
			    screen.addToLog(Utils.formatElapsed("Preparation:", timer));
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
				
				screen.addToLog(Utils.formatElapsed("Textures loaded:", timer));
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
			    screen.addToLog(Utils.formatElapsed("Heightmap loaded:", timer));
				return;
				
			case MeshGeo:
				try {
			    	terrainMesh.build(heightMap);
				} catch (TerrainException e) {		
					e.printStackTrace();
				}		
				screen.addToLog(Utils.formatElapsed("Mesh built:", timer));
				return;
				
				
			case MeshVisual:
				try {
					terrainRenderer.assignMesh(terrainMesh);
				} catch (TerrainException e) {
					e.printStackTrace();
				}
				
				screen.addToLog(Utils.formatElapsed("GL buffers built:", timer));
				return;
						
			case Finished:
				state = State.TerrainRun;
			    setScreen(null);
			    capturedMouse = true;
				Gdx.input.setCursorCatched(capturedMouse);				
				
				// To build camera etc.
				resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
				
				input.setPrimaryProcessor(instance);
				
				if (Utils.elapsedTimeMs(timer) < 1000) {
					Utils.delay((int) (1000 - Utils.elapsedTimeMs(timer)));
				}
				
				camController.setSpeedFactor(terrainMesh.getSize());							
				return;
			}
		}
		
		
		
	}
	
	
}
