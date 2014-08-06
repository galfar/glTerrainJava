package com.galfarslair.glterrain.soar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.glterrain.TerrainRenderer;
import com.galfarslair.util.DynamicIndexBuffer;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class SoarRenderer implements TerrainRenderer {
	
	private SoarMesh mesh;	
	private VertexBufferObject vbo;
	private DynamicIndexBuffer ibo;
	public ShaderProgram shader;
	
	public SoarRenderer(FileHandle vertexShader, FileHandle fragmentShader) {		
		shader = new ShaderProgram(vertexShader, fragmentShader);		
		Utils.logInfo(shader.getLog());
	}
	
	@Override
	public void setWireFrameOverlay(boolean enabled) {
		// no wire overlay support
	}
	
	@Override
	public void assignMesh(TerrainMesh mesh) throws TerrainException {
		assert mesh instanceof SoarMesh;
		this.mesh = (SoarMesh)mesh;		
		buildBuffers();		
	}
	
	@Override
	public void render(Camera camera) {		
		ibo.setIndices(mesh.getIndices(), 0, mesh.getNumIndices());
		
		vbo.bind(shader);
		ibo.bind();		
		shader.begin();
		
		shader.setUniformMatrix("matProjView", camera.combined);
		shader.setUniformi("texGround", 0);
		shader.setUniformi("texDetail", 1);
		shader.setUniformf("terrainSize", mesh.getSize());
		Gdx.gl20.glDrawElements(GL20.GL_TRIANGLE_STRIP, ibo.getNumIndices(), GL20.GL_UNSIGNED_INT, 0);				
		
		shader.end();
		ibo.unbind();
		vbo.unbind(shader);		
	}
	
	private void buildBuffers() {
		vbo = new VertexBufferObject(true, mesh.getNumVertices(), new VertexAttribute(Usage.Position, 3, "position"));
		ibo = new DynamicIndexBuffer(mesh.getNumIndices());		
		vbo.setVertices(mesh.getVertices(), 0, mesh.getNumVertices() * 3);
		ibo.setIndices(mesh.getIndices(), 0, mesh.getNumIndices());
	}

	@Override
	public void dispose() {
		vbo.dispose();
		ibo.dispose();
		shader.dispose();
	}
}
