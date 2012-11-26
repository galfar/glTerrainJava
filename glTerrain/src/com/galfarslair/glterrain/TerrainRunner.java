package com.galfarslair.glterrain;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
import com.galfarslair.util.Utils.TerrainException;

public class TerrainRunner implements ApplicationListener {
	private final float fieldOfView = 45;
	private final Color skyColor = new Color(0.5f, 0.625f, 0.75f, 1.0f);	
	private final float observerHeight = 1.8f;
	
	private float cameraYaw = 135;
	private float cameraPitch = -15;	
	private final Vector3 cameraPos = new Vector3(0, 0, 60);
	private final Vector3 cameraUp = new Vector3(0, 0, 1);
		
	private SoarMesh soarMesh;
	private SoarRenderer soarRenderer;
	
	private PerspectiveCamera camera;
	private FPSLogger fpsLogger;
	private BitmapFont font;
	private SpriteBatch batch;
	
	private Texture groundTexture;
	private Texture detailTexture;
			
	private MipMapMesh geoMesh;
	private MipMapRenderer geoRenderer;
	
	private TerrainMesh terrainMesh;
	
	private boolean isFlying = true;
	
	private boolean benchmarkMode;
		
	private Pixmap heightMap;
	
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
		
		soarMesh = new SoarMesh();
		
		//heightMap = new Pixmap(Gdx.files.internal("data/Volcanoes/Volcanoes1025.png"));
		heightMap = new Pixmap(Gdx.files.internal("data/Terrains/Volcanoes/VolcanoesMini.png"));
				
		/*try {
			soarMesh.build(heightMap);
		} catch (TerrainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		geoMesh = new MipMapMesh();
		try {
			geoMesh.build(heightMap);
		} catch (TerrainException e) {		
			e.printStackTrace();
		}
		
		
		heightMap.dispose();
				
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
		detailTexture = new Texture(Gdx.files.internal("data/Terrains/Detail.jpg"), true);
		detailTexture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
		detailTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		
	    soarRenderer = new SoarRenderer(Gdx.files.internal("data/Shaders/soar.vert"), Gdx.files.internal("data/Shaders/textured.frag"));
	    /*try {
			soarRenderer.assignMesh(soarMesh);
		} catch (TerrainException e) {
			e.printStackTrace();
		}*/
	    
	    geoRenderer = new MipMapRenderer(Gdx.files.internal("data/Shaders/geo.vert"), Gdx.files.internal("data/Shaders/textured.frag"));
	    try {
			geoRenderer.assignMesh(geoMesh);
		} catch (TerrainException e) {
			e.printStackTrace();
		}
	    
	    terrainMesh = geoMesh;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void render() {	
		checkInput();
		
		Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1);
		Gdx.gl.glClearDepthf(1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	
		camera.update();					
		
		groundTexture.bind(0);
		detailTexture.bind(1);
		
		//soarMesh.update(camera);
		//soarRenderer.render(camera);
		
		geoMesh.update(camera);
		geoRenderer.render(camera);
			
		font.getRegion().getTexture().bind(0);
		batch.begin();		
		batch.enableBlending();		
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 5, Gdx.graphics.getHeight());
		font.draw(batch, "Camera: " + cameraYaw + " " + cameraPitch, 5, Gdx.graphics.getHeight() - 20);
		batch.end();
		
		fpsLogger.log();
	}

	@Override
	public void resize(int width, int height) {		
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
	
	private void checkInput() {		
		
		final float mouseLookSensitivity = 0.1f;
		final float walkSpeed = 2f;		
		final float superMove = 5f;
		
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}
		
		int dx = Gdx.input.getDeltaX();
		int dy = Gdx.input.getDeltaY();
		
		if ((dx != 0) || (dy != 0)) {
			updateCameraDirection(dx * mouseLookSensitivity, -dy * mouseLookSensitivity);
		}		
		
		float speed = walkSpeed * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
			speed = (terrainMesh.getSize() / superMove) * Gdx.graphics.getDeltaTime();
		} 
		
		camera.position.add(camera.direction.x * speed, camera.direction.y * speed, 0);
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
