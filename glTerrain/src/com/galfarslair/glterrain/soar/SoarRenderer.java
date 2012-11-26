package com.galfarslair.glterrain.soar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.math.Matrix4;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.glterrain.TerrainRenderer;
import com.galfarslair.util.DynamicIndexBuffer;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class SoarRenderer implements TerrainRenderer {
	
	private SoarMesh mesh;	
	private VertexBufferObject vertexBuffer;
	private DynamicIndexBuffer indexBuffer;
	public ShaderProgram shader;
	
	public SoarRenderer(FileHandle vertexShader, FileHandle fragmentShader) {		
		shader = new ShaderProgram(vertexShader, fragmentShader);		
		Utils.logInfo(shader.getLog());
	}
	
	public void assignMesh(TerrainMesh mesh) throws TerrainException {
		assert mesh instanceof SoarMesh;
		this.mesh = (SoarMesh)mesh;		
		buildBuffers();		
	}
	
	public void render(Camera camera) {		
		indexBuffer.setIndices(mesh.getIndices(), 0, mesh.getNumIndices());
		
		vertexBuffer.bind(shader);
		indexBuffer.bind();
		
		shader.begin();
		
		shader.setUniformMatrix("u_projectionView", camera.combined);
		shader.setUniformf("t_ground", 0);		
		shader.setUniformf("u_size", mesh.getSize());
		
		boolean wireframe = false;
		shader.setUniformi("wire", wireframe ? 1 : 0);
		
		
		Gdx.gl20.glDrawElements(GL20.GL_TRIANGLE_STRIP, indexBuffer.getNumIndices(), GL20.GL_UNSIGNED_INT, 0);
		
		
		shader.end();
	}
	
	private void buildBuffers() {
		vertexBuffer = new VertexBufferObject(true, mesh.getNumVertices(), VertexAttribute.Position());
		indexBuffer = new DynamicIndexBuffer(mesh.getNumIndices());		
		vertexBuffer.setVertices(mesh.getVertices(), 0, mesh.getNumVertices() * 3);
		indexBuffer.setIndices(mesh.getIndices(), 0, mesh.getNumIndices());
	}

}
