package com.galfarslair.glterrain.mipmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.glterrain.TerrainRenderer;
import com.galfarslair.glterrain.mipmap.MipMapMesh.Node;
import com.galfarslair.glterrain.mipmap.MipMapMesh.NodeAction;
import com.galfarslair.util.ShortArray;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class MipMapRenderer implements TerrainRenderer {

	private MipMapMesh mesh;	
	private ObjectMap<MipMapMesh.Node, VertexBufferObject> heightBuffers;
	private GeometryData geoData;	
	private ShaderProgram shader;
	
	public MipMapRenderer(FileHandle vertexShader, FileHandle fragmentShader) {
		shader = new ShaderProgram(vertexShader, fragmentShader);		
		Utils.logInfo(shader.getLog());		
	}
	
	@Override
	public void assignMesh(TerrainMesh mesh) throws TerrainException {
		assert mesh instanceof MipMapMesh;
		this.mesh = (MipMapMesh)mesh;
		buildData();
	}

	@Override
	public void render(Camera camera) {
		shader.begin();
		geoData.vbo.bind(shader);
		geoData.ibo.bind();

		shader.setUniformMatrix("matProjView", camera.combined);
		shader.setUniformi("texGround", 0);
		shader.setUniformi("texDetail", 1);
		shader.setUniformf("terrainSize", mesh.getSize());
		shader.setUniformf("nodeSize", mesh.getLeafSize());

		for (MipMapMesh.Node leaf : mesh.getVisibleLeaves()) {
			int lod = leaf.getCurrentLod();
			shader.setUniformf("nodePos", leaf.getX(), leaf.getY());
			VertexBufferObject heights = heightBuffers.get(leaf);
			heights.bind(shader);
			Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, geoData.numIndices[lod],
					GL20.GL_UNSIGNED_SHORT, geoData.indexOffsets[lod] * 2);
			heights.unbind(shader);
		}

		geoData.ibo.unbind();
		geoData.vbo.unbind(shader);
		shader.end();
	}
	
	
	private void buildData() {
		geoData = new GeometryData(mesh.getLeafSize(), mesh.getLods());
		heightBuffers = new ObjectMap<MipMapMesh.Node, VertexBufferObject>();
		mesh.visitQuadTreeLeaves(mesh.getRootNode(), new NodeAction() {
			@Override
			public void execute(Node node) {
				VertexBufferObject vbo = new VertexBufferObject(true, geoData.numVertices, new VertexAttribute(Usage.Position, 1, "height"));
				vbo.setVertices(node.getHeights(), 0, geoData.numVertices);
				heightBuffers.put(node, vbo);
			}
		});
	}
	
	private static class GeometryData {
		int numVertices;		
		int numVerticesPerLine;
		int[] numIndices;
		int[] indexOffsets;
		VertexBufferObject vbo;
		IndexBufferObject ibo;
		
		public GeometryData(int leafSize, int lods) {
			numVerticesPerLine = leafSize + 1;
			numVertices = Utils.sqr(numVerticesPerLine);
			buildVertexData();
			
			numIndices = new int[lods];
			indexOffsets = new int[lods];
			buildIndexData(lods);			
		}
		
		private void buildVertexData() {
			vbo = new VertexBufferObject(true, numVertices, 
					new VertexAttribute(Usage.Position, 2, "position"),
					new VertexAttribute(Usage.Generic, 3, "baryAttribs"));			
			FloatArray verts = new FloatArray(); 
			int baryIdx = 0;
			/* Generate barycentric coordinates for grid triangles for wireframe display in shader.
			 * Based on http://codeflow.org/entries/2012/aug/02/easy-wireframe-display-with-barycentric-coordinates/
			 * Distribution of coords across the grid base on drawing stuff on paper, works for all lod levels :) 
			 */
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
					
					if (x < numVerticesPerLine - 1) {
						baryIdx++;
						if (baryIdx >= 3)
							baryIdx = 0;
					}
				}				
			}						
			
			assert verts.size == numVertices * 5;
			vbo.setVertices(verts.toArray(), 0, numVertices * 5);		
		}
		
		private void buildIndexData(int lods) {
			int stride = 1;
			int totalIndices = 0;
			ShortArray indices = new ShortArray();
			
			for (int lod = 0; lod < lods; lod++) {
				numIndices[lod] = Utils.sqr((numVerticesPerLine - 1) / stride) * 6;
								
				for (int y = 0; y < numVerticesPerLine - 1; y += stride) {
					int startLineIdx = y * numVerticesPerLine;
					
					for (int x = 0; x < numVerticesPerLine - 1; x += stride) {
						indices.add(startLineIdx + x);
						indices.add(startLineIdx + numVerticesPerLine * stride + x);
						indices.add(startLineIdx + x + stride);
						
						indices.add(startLineIdx + x + stride);
						indices.add(startLineIdx + numVerticesPerLine * stride + x);
						indices.add(startLineIdx + numVerticesPerLine * stride + x + stride);
					}				
				}	
				
				stride *= 2;
				indexOffsets[lod] = totalIndices;
				totalIndices += numIndices[lod];				
			}
				
			assert indices.size == totalIndices;
			ibo = new IndexBufferObject(totalIndices);
			ibo.setIndices(indices.toArray(), 0, totalIndices);
		}
	}

}
