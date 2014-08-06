package com.galfarslair.glterrain.vtf;

import static com.galfarslair.util.Utils.sqr;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.glterrain.util.BaseTileTerrainRenderer;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class VtfRenderer extends BaseTileTerrainRenderer {

	private VtfMesh mesh;
			
	private Texture heightTexture;
	private VertexBufferObject vboGrid;	
	private IndexBufferObject iboGrid;
	private VertexBufferObject vboGridWire;
		
	int tileSize;
	int numVerticesPerLine;
	int numVertices;
	int numIndices;
	int numVerticesWire;
	private boolean wireOverlay;
	
	public VtfRenderer(ShaderProgram shaderDefault, ShaderProgram shaderWire) {
		super(shaderDefault, shaderWire);		
	}
	
	@Override
	public void setWireFrameOverlay(boolean enabled) {
		super.setWireFrameOverlay(enabled);
		//shader = shaderDefault;
		wireOverlay = enabled;
	}
		
	@Override
	public void assignMesh(TerrainMesh mesh) throws TerrainException {
		assert mesh instanceof VtfMesh;
		this.mesh = (VtfMesh)mesh;
		this.tileSize = this.mesh.getTileSize();
		buildData();
	}

	@Override
	public void render(Camera camera) {
		heightTexture.bind(2);		
		Array<VtfMesh.Node> nodes = mesh.getActiveNodes();
		
		//wireOverlay = false;
		
		shader.begin();		
		/*if (!wireOverlay) {
			vboGrid.bind(shader);
			iboGrid.bind();		  
		} else*/ {
			vboGridWire.bind(shader);						
		}		
		
		shader.setUniformMatrix("matProjView", camera.combined);
		shader.setUniformi("texGround", 0);
		shader.setUniformi("texDetail", 1);
		shader.setUniformi("texHeight", 2);
		shader.setUniformf("terrainSize", mesh.getSize());		
		shader.setUniformf("heightSampleScale", mesh.getSize() * TerrainMesh.HEIGHT_SCALE);
		shader.setUniformf("tileSize", tileSize);
							
		int count = 0;
		
		for (VtfMesh.Node node : nodes) {
			
			int[] neighborLods = mesh.lookupNeghborLods(node);
			float[] neighorScalings = new float[] { 1, 1, 1, 1 };
			for (int i = 0; i < 4; i++) {
				if (neighborLods[i] < node.level) {
					neighorScalings[i] = Utils.pow2(node.level - neighborLods[i]);
				}
			}
			
			shader.setUniformf("nodePos", node.x, node.y);
			shader.setUniformf("nodeScale", node.size / tileSize);			
			shader.setUniform4fv("lodScales", neighorScalings, 0, 4);
			//shader.setUniformf("lodScales", 2, 4, 8, 2);
			
			/*if (!wireOverlay) {
				Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, numIndices, GL20.GL_UNSIGNED_SHORT, 0);
			} else*/ {
				Gdx.gl20.glDrawArrays(GL20.GL_TRIANGLES, 0, numVerticesWire);
			}
			
			count++;
			//break;
			/*if (count == 4)
				break;*/
		}
		
		/*if (!wireOverlay) {
			vboGrid.unbind(shader);
			iboGrid.unbind();		  
		} else*/ {
			vboGridWire.unbind(shader);						
		}
				
		shader.end();
	}
	
	@Override
	public void dispose() {
		vboGrid.dispose();
		iboGrid.dispose();
		vboGridWire.dispose();
		heightTexture.dispose();
	}
	
	private void buildData() {			
		buildHeighTexture();
		buildGridVertexData();
		buildIndexData();		
		buildGridWireVertexData();		
	}
	
	private void buildGridWireVertexData() {
		numVerticesWire = sqr(tileSize) * 2 * 3;		
		vboGridWire = new VertexBufferObject(true, numVerticesWire, 
				new VertexAttribute(Usage.Position, 2, "position"),
				new VertexAttribute(Usage.Generic, 3, "baryAttribs"));
		
		FloatArray verts = new FloatArray(numVerticesWire);
		boolean parity = true;
		
		for (int y = 0; y < tileSize; y++) {				
			for (int x = 0; x < tileSize; x++) {
				
				verts.addAll(x, y);				
				verts.addAll(1, 0, 0);
								
				verts.addAll(x, y + 1);				
				verts.addAll(0, 1, 0);
				
				if (parity) {
					verts.addAll(x + 1, y + 1);
				} else {
					verts.addAll(x + 1, y);
				}						
				verts.addAll(0, 0, 1);				
				
				
				verts.addAll(x + 1, y);				
				verts.addAll(1, 0, 0);
				
				if (parity) {
					verts.addAll(x, y);
				} else {
					verts.addAll(x, y + 1);
				}
				verts.addAll(0, 1, 0);
								
				verts.addAll(x + 1, y + 1);
				verts.addAll(0, 0, 1);
			
				
				if (x < tileSize - 1) {
					parity = !parity;
				}
			}
		}
		
		int vertexElems = 5;
		assert verts.size == numVerticesWire * vertexElems;
		vboGridWire.setVertices(verts.items, 0, verts.size);		
	}
		
	private void buildGridVertexData() {
		numVerticesPerLine = mesh.getTileSize() + 1;
		numVertices = Utils.sqr(numVerticesPerLine);
		
		vboGrid = new VertexBufferObject(true, numVertices, 
				new VertexAttribute(Usage.Position, 2, "position"));			
		
		int vertexElems = 2;
		FloatArray verts = new FloatArray(); 
					
		for (int y = 0; y < numVerticesPerLine; y++) {				
			for (int x = 0; x < numVerticesPerLine; x++) {
				verts.add(x);
				verts.add(y);
			}				
		}
		
		assert verts.size == numVertices * vertexElems;
		vboGrid.setVertices(verts.items, 0, verts.size);
	}
	
	private void buildIndexData() {
		numIndices = Utils.sqr(numVerticesPerLine - 1) * 6;				
		ShortArray indices = new ShortArray();
		boolean parity = false;
		
		for (int y = 0; y < numVerticesPerLine - 1; y++) {
			int startLineIdx = y * numVerticesPerLine;
			
			for (int x = 0; x < numVerticesPerLine - 1; x++) {
				if (parity) {
					indices.add(startLineIdx + x);
					indices.add(startLineIdx + numVerticesPerLine + x);
					indices.add(startLineIdx + x + 1);
					
					indices.add(startLineIdx + x + 1);
					indices.add(startLineIdx + numVerticesPerLine + x);
					indices.add(startLineIdx + numVerticesPerLine + x + 1);
				} else {
					indices.add(startLineIdx + x);
					indices.add(startLineIdx + numVerticesPerLine + x);
					indices.add(startLineIdx + numVerticesPerLine + x + 1);
					
					indices.add(startLineIdx + x);
					indices.add(startLineIdx + numVerticesPerLine + x + 1);
					indices.add(startLineIdx + x + 1);
				}
				
				if (x < numVerticesPerLine - 2) {
					parity = !parity;
				}
			}				
		}	
			
		assert indices.size == numIndices;
		iboGrid = new IndexBufferObject(numIndices);
		iboGrid.setIndices(indices.toArray(), 0, numIndices);
	}

	private void buildHeighTexture() {
		HeightMap hm = mesh.getHeightMap();
		ShortBuffer samples = hm.getSamples();
		
		// Unfortunately, GL_LUMINANCE + GL_UNSIGNED_SHORT not working in GLES
		// so we must use 8bit height texture. At least linear filter can be used.
		// Also just use pow2 sized part of heightmap as texture for 
		// better compatibility.
		
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
