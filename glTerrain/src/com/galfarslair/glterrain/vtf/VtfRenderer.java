package com.galfarslair.glterrain.vtf;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.glterrain.TerrainRenderer;
import com.galfarslair.glterrain.mipmap.MipMapMesh;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.ShortArray;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class VtfRenderer implements TerrainRenderer {

	private VtfMesh mesh;
	private ShaderProgram shader;
	
	private Texture heightTexture;
	private VertexBufferObject vboGrid;
	private IndexBufferObject iboGrid;
	
	int numVerticesPerLine;
	int numVertices;
	int numIndices;
	
	public VtfRenderer(ShaderProgram shader) {
		this.shader = shader;
	}
	
	@Override
	public void assignMesh(TerrainMesh mesh) throws TerrainException {
		assert mesh instanceof VtfMesh;
		this.mesh = (VtfMesh)mesh;
		buildData();
	}

	@Override
	public void render(Camera camera) {
		shader.begin();
		vboGrid.bind(shader);
		iboGrid.bind();

		heightTexture.bind(2);
		
		shader.setUniformMatrix("matProjView", camera.combined);
		shader.setUniformi("texGround", 0);
		shader.setUniformi("texDetail", 1);
		shader.setUniformi("texHeight", 2);
		shader.setUniformf("terrainSize", mesh.getSize());		
		shader.setUniformf("heightSampleScale", mesh.getSize() * TerrainMesh.HEIGHT_SCALE);
		shader.setUniformf("nodeSize", mesh.getTileSize());
				
		for (VtfMesh.Tile tile : mesh.getTiles()) {
			
			shader.setUniformf("nodePos", tile.x, tile.y);
			
			Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, numIndices, GL20.GL_UNSIGNED_SHORT, 0);
		}
		
		iboGrid.unbind();
		vboGrid.unbind(shader);
		shader.end();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
	
	private void buildData() {
		buildHeighTexture();
		buildGridVertexData();
		buildIndexData();		
	}
	
	private void buildGridVertexData() {
		numVerticesPerLine = mesh.getTileSize() + 1;
		numVertices = Utils.sqr(numVerticesPerLine);
		
		vboGrid = new VertexBufferObject(true, numVertices, 
				new VertexAttribute(Usage.Position, 2, "position"),
				new VertexAttribute(Usage.Generic, 3, "baryAttribs"));			
		
		int vertexElems = 5;
		FloatArray verts = new FloatArray(); 
		
		/* Generate barycentric coordinates for grid triangles for wireframe display in shader.
		 * Based on http://codeflow.org/entries/2012/aug/02/easy-wireframe-display-with-barycentric-coordinates/
		 * Distribution of coords across the grid base on drawing stuff on paper, works for all lod levels :) 
		 */
		int baryIdx = 0;
		Vector3[] baryCoords = new Vector3[] {
				new Vector3(1, 0, 0),
				new Vector3(0, 1, 0),
				new Vector3(0, 0, 1)};			
		
		for (int y = 0; y < numVerticesPerLine; y++) {				
			for (int x = 0; x < numVerticesPerLine; x++) {
				verts.add(x);
				verts.add(y);
									
				Vector3 bary = baryCoords[baryIdx];
				verts.add(bary.x);
				verts.add(bary.y);
				verts.add(bary.z);
				
				if ((x < numVerticesPerLine - 1) || (numVerticesPerLine % 3 != 0)) {
					baryIdx++;
					if (baryIdx >= 3) {
						baryIdx = 0;
					}
				}
			}				
		}
		
		assert verts.size == numVertices * vertexElems;
		vboGrid.setVertices(verts.toArray(), 0, verts.size);
	}
	
	private void buildIndexData() {
		numIndices = Utils.sqr(numVerticesPerLine - 1) * 6;				
		ShortArray indices = new ShortArray();
		
		for (int y = 0; y < numVerticesPerLine - 1; y++) {
			int startLineIdx = y * numVerticesPerLine;
			
			for (int x = 0; x < numVerticesPerLine - 1; x++) {
				indices.add(startLineIdx + x);
				indices.add(startLineIdx + numVerticesPerLine + x);
				indices.add(startLineIdx + x + 1);
				
				indices.add(startLineIdx + x + 1);
				indices.add(startLineIdx + numVerticesPerLine + x);
				indices.add(startLineIdx + numVerticesPerLine + x + 1);
			}				
		}	
			
		assert indices.size == numIndices;
		iboGrid = new IndexBufferObject(numIndices);
		iboGrid.setIndices(indices.toArray(), 0, numIndices);
	}

	private void buildHeighTexture() {
		HeightMap hm = mesh.getHeightMap();
		ShortBuffer samples = hm.getSamples();
		
		int width = hm.getWidth();
		int height = hm.getHeight();
		
		Pixmap pixmap = new Pixmap(width - 1, height - 1, Format.Intensity);
		ByteBuffer pixels = pixmap.getPixels();
		
		for (int y = 0; y < height - 1; y++) {
			for (int x = 0; x < width - 1; x++) {
				int index = y * width + x; 
				short sample = samples.get(index);
				
				index = y * (width - 1) + x;
				byte pixel = (byte)(sample >> 8);
				pixels.put(index, pixel);				
			}
		}		
		
		heightTexture = new Texture(pixmap);
		heightTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

}
