package com.galfarslair.glterrain.mipmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
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
	private ObjectMap<MipMapMesh.Node, VertexBufferObject> skirtHeightBuffers;
	private LeafGrid leafGrid;	
	private ShaderProgram shader;
	private ShaderProgram shaderSkirt;
	
	public MipMapRenderer(ShaderProgram shader, ShaderProgram shaderSkirt) {
		assert shader != null;
		assert shaderSkirt != null;
		this.shader = shader;
		this.shaderSkirt = shaderSkirt;
	}
	
	@Override
	public void assignMesh(TerrainMesh mesh) throws TerrainException {
		assert mesh instanceof MipMapMesh;
		this.mesh = (MipMapMesh)mesh;
		buildData();
	}

	@Override
	public void render(Camera camera) {
		VertexBufferObject heights;
		Array<MipMapMesh.Node> visibleLeaves = mesh.getVisibleLeaves();
		
		/*visibleLeaves.clear();
		visibleLeaves.add(mesh.getLeafAtPos(1, 1));*/
		
		shader.begin();
		leafGrid.vbo.bind(shader);
		leafGrid.ibo.bind();

		shader.setUniformMatrix("matProjView", camera.combined);
		shader.setUniformi("texGround", 0);
		shader.setUniformi("texDetail", 1);
		shader.setUniformf("terrainSize", mesh.getSize());
		shader.setUniformf("nodeSize", mesh.getLeafSize());
		
		for (MipMapMesh.Node leaf : visibleLeaves) {
			int lod = leaf.getCurrentLod();
			shader.setUniformf("nodePos", leaf.getX(), leaf.getY());
			heights = heightBuffers.get(leaf);
			heights.bind(shader);
			Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, leafGrid.numIndices[lod],
					GL20.GL_UNSIGNED_SHORT, leafGrid.indexOffsets[lod] * 2);
			heights.unbind(shader);
		}
		
		leafGrid.ibo.unbind();
		leafGrid.vbo.unbind(shader);
		shader.end();
		
		
		shaderSkirt.begin();
		leafGrid.vboSkirt.bind(shaderSkirt);
		leafGrid.iboSkirt.bind();

		shaderSkirt.setUniformMatrix("matProjView", camera.combined);
		shaderSkirt.setUniformi("texGround", 0);
		shaderSkirt.setUniformf("terrainSize", mesh.getSize());
		shaderSkirt.setUniformf("nodeSize", mesh.getLeafSize());
				
		for (MipMapMesh.Node leaf : visibleLeaves) {
			int lod = leaf.getCurrentLod();
			shaderSkirt.setUniformf("nodePos", leaf.getX(), leaf.getY());
			heights = skirtHeightBuffers.get(leaf);
			heights.bind(shaderSkirt);
			Gdx.gl20.glDrawElements(GL20.GL_TRIANGLES, leafGrid.numSkirtIndices[lod],
					GL20.GL_UNSIGNED_SHORT, leafGrid.skirtIndexOffsets[lod] * 2);
			heights.unbind(shaderSkirt);
		}
		
		leafGrid.vboSkirt.unbind(shaderSkirt);
		leafGrid.iboSkirt.unbind();
		shaderSkirt.end();
	}
	
	@Override
	public void dispose() {
		leafGrid.dispose();
		ObjectMap.Values<VertexBufferObject> vbos = heightBuffers.values();
		while (vbos.hasNext()) {
			vbos.next().dispose();
		}			
	}	
	
	private void buildData() {
		leafGrid = new LeafGrid(mesh.getLeafSize(), mesh.getLods());
		heightBuffers = new ObjectMap<MipMapMesh.Node, VertexBufferObject>();
		skirtHeightBuffers = new ObjectMap<MipMapMesh.Node, VertexBufferObject>();
		
		mesh.visitQuadTreeLeaves(mesh.getRootNode(), new NodeAction() {
			@Override
			public void execute(Node node) {
				VertexBufferObject vbo;
				final float[] heights = node.getHeights();
				
				// Grid heights
				vbo = new VertexBufferObject(true, leafGrid.numVertices, new VertexAttribute(Usage.Position, 1, "height"));				
				vbo.setVertices(heights, 0, leafGrid.numVertices);
				heightBuffers.put(node, vbo);
				
				// Skirt heights
				vbo = new VertexBufferObject(true, leafGrid.numSkirtVertices, new VertexAttribute(Usage.Position, 1, "height"));				
				final FloatArray skirt = new FloatArray(leafGrid.numSkirtVertices);
				final int vertsOnLine = leafGrid.numVerticesPerLine;
				final float minZ = node.getBounds().min.z;
				
				leafGrid.walkSkirtSides(new SkirtElementAction() {				
					@Override
					public void execute(float x, float y) {
						float z = heights[(int) (vertsOnLine * y + x)];
						skirt.add(z);
						skirt.add(minZ);								
					}
				});				
				vbo.setVertices(skirt.toArray(), 0, leafGrid.numSkirtVertices);
				skirtHeightBuffers.put(node, vbo);				
			}
		});
	}
	
	private interface SkirtElementAction {
		void execute(float x, float y);
	}
	
	private class LeafGrid implements Disposable {
		int numVertices;		
		int numVerticesPerLine;		
		int[] numIndices;
		int[] indexOffsets;
		VertexBufferObject vbo;
		IndexBufferObject ibo;
		
		int[] numSkirtIndices;
		int[] skirtIndexOffsets;
		int numSkirtVertices;
		VertexBufferObject vboSkirt;
		IndexBufferObject iboSkirt;
				
		public LeafGrid(int leafSize, int lods) {
			numVerticesPerLine = leafSize + 1;
			numVertices = Utils.sqr(numVerticesPerLine);			
			buildGridVertexData();
			
			numSkirtVertices = (numVerticesPerLine * 2) * 4;
			buildSkirtVertexData();
			
			numIndices = new int[lods];
			indexOffsets = new int[lods];
			buildIndexData(lods);	
			
			numSkirtIndices = new int[lods];
			skirtIndexOffsets = new int[lods];
			buildSkirtIndexData(lods);
		}
		
		private void buildGridVertexData() {
			vbo = new VertexBufferObject(true, numVertices, 
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
			vbo.setVertices(verts.toArray(), 0, verts.size);
		}
		
		private void buildSkirtVertexData() {
			vboSkirt = new VertexBufferObject(true, numSkirtVertices, new VertexAttribute(Usage.Position, 2, "position"));
			final FloatArray skirt = new FloatArray();
			
			walkSkirtSides(new SkirtElementAction() {				
				@Override
				public void execute(float x, float y) {
					skirt.add(x);
					skirt.add(y);					
					
					skirt.add(x);
					skirt.add(y);													
				}
			});
				
			assert skirt.size == numSkirtVertices * 2;			
			vboSkirt.setVertices(skirt.toArray(), 0, skirt.size);
		}
		
		public void walkSkirtSides(SkirtElementAction action) {
			addSkirtSide(action, 0, 0, 1, 0); 
			addSkirtSide(action, 1, 0, 0, numVerticesPerLine - 1); 
			addSkirtSide(action, 0, numVerticesPerLine - 1, -1, numVerticesPerLine - 1); 
			addSkirtSide(action, -1, numVerticesPerLine - 1, 0, 0);
		}
		
		private void addSkirtSide(SkirtElementAction action, float xMult, float xOffset, float yMult, float yOffset) {
			float x, y;			
			for (int i = 0; i < numVerticesPerLine; i++) {
				x = i * xMult + xOffset;	
				y = i * yMult + yOffset;			    
				action.execute(x, y);								
			}
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
		
		private void buildSkirtIndexData(int lods) {
			int stride = 1;
			int totalIndices = 0;
			ShortArray skirt = new ShortArray();
			
			for (int lod = 0; lod < lods; lod++) {
				numSkirtIndices[lod] = (numVerticesPerLine - 1) / stride * 4 * 6; 
				
				for (int j = 0; j < 4; j++) {
					int startEdgeIdx = j * numVerticesPerLine * 2;
					
					for (int i = 0; i < numVerticesPerLine - 1; i += stride) {
						skirt.add(startEdgeIdx + i * 2);
						skirt.add(startEdgeIdx + i * 2 + 1);
						skirt.add(startEdgeIdx + (i + stride) * 2);
						
						skirt.add(startEdgeIdx + i * 2 + 1);
						skirt.add(startEdgeIdx + (i + stride) * 2 + 1);
						skirt.add(startEdgeIdx + (i + stride) * 2);
					}				
				}					
				
				stride *= 2;
				skirtIndexOffsets[lod] = totalIndices;
				totalIndices += numSkirtIndices[lod];				
			}
			
			assert skirt.size == totalIndices;
			iboSkirt = new IndexBufferObject(skirt.size);
			iboSkirt.setIndices(skirt.toArray(), 0, skirt.size);			
		}

		@Override
		public void dispose() {
			vbo.dispose();
			ibo.dispose();			
		}
	}

}
