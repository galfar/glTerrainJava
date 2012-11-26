package com.galfarslair.glterrain.mipmap;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.galfarslair.glterrain.TerrainMesh;
import static com.galfarslair.util.Utils.log2;
import static com.galfarslair.util.Utils.isPow2;

import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class MipMapMesh implements TerrainMesh {

	public static final int CHILD_TOP_LEFT     = 0;
	public static final int CHILD_TOP_RIGHT    = 1;
	public static final int CHILD_BOTTOM_LEFT  = 2;
	public static final int CHILD_BOTTOM_RIGHT = 3;
	
	private static final int DEFAULT_LEAF_SIZE = 32;
	
	private int size;
	private int leafSize = DEFAULT_LEAF_SIZE;
	private int leafCount;
	private int levels;
	private int lods;
	private float tolerance = 4;
	private RootNode root;
	private Array<Node> visibleLeaves;
	
	public MipMapMesh() {
		
	}
	
	@Override
	public int getSize() {		
		return size;
	}
	
	public int getLeafSize() {
		return leafSize;
	}
	
	public int getLods() {
		return lods;
	}
	
	public RootNode getRootNode() {
		return root;
	}
	
	public Array<Node> getVisibleLeaves() {
		return visibleLeaves;
	}
	
	@Override
	public void build(Pixmap heightMap) throws TerrainException {
		size = heightMap.getWidth();
		assert (heightMap.getFormat() == Format.Intensity) || (heightMap.getFormat() == Format.Alpha) : "Invalid pxel format of heightmap";
		assert size == heightMap.getHeight() : "Heightmap needs to be square";
		assert isPow2(size - 1) : "Heightmap needs to be 2^n+1 pixels in size";
				
		levels = log2(size - 1) - log2(leafSize) + 1;
		lods = log2(leafSize) + 1;
		leafCount = (int)Math.pow(4, levels);
		
		root = new RootNode(size - 1);
		ByteBuffer heightBuffer = heightMap.getPixels();
		root.buildTree(heightBuffer, size, leafSize);
		
		visibleLeaves =  new Array<Node>(leafCount);		
	}

	@Override
	public void update(PerspectiveCamera camera) {
		final PerspectiveCamera localCamera = camera;
		final float perspectiveFactor = (float) (camera.viewportHeight / (2 * Math.tan(Math.PI / 4)));
		visibleLeaves.clear();
		
		visitQuadTreeLeaves(root, new NodeAction() {
			@Override
			public void execute(Node node) {
				if (localCamera.frustum.sphereInFrustumWithoutNearFar(node.bounds.getCenter(), node.radius)) { 
					visibleLeaves.add(node);					
					node.determineLod(localCamera.position, perspectiveFactor, tolerance);					
				}
			}
		});
	}

	public interface NodeAction {
		void execute(MipMapMesh.Node node);
	}
	
	public void visitQuadTreeLeaves(MipMapMesh.Node node, NodeAction action) {
		if (node.isLeaf()) {
			action.execute(node);
		} else {
			for (Node child : node.children) {
				visitQuadTreeLeaves(child, action);
			}
		}
	}
	
	public static class Node {		
		private int size;		
		private int x;
		private int y;
		private int lods;
		private int currentLod;
		private Node[] children;
		private final BoundingBox bounds = new BoundingBox();
		private float[] heights;
		private float errors[];	
		private float radius;
				
		public Node(int x, int y, int size) {			
			this.x = x;
			this.y = y;
			this.size = size;
		}
		
		public int getX() {
			return x;
		}
		
		public int getY() {
			return y;
		}
		
		public boolean isLeaf() {
			return children == null;
		}
		
		public int getCurrentLod() {
			return currentLod;
		}
		
		public float[] getHeights() {
			return heights;
		}
				
		protected void buildTree(ByteBuffer heightBuffer, int pitch, int leafSize) {
			if (size > leafSize) {
				int halfSize = size / 2;			
				children = new Node[4];
				children[CHILD_TOP_LEFT] = new Node(x, y, halfSize);
				children[CHILD_TOP_RIGHT] = new Node(x + halfSize, y, halfSize);
				children[CHILD_BOTTOM_LEFT] = new Node(x, y + halfSize, halfSize);
				children[CHILD_BOTTOM_RIGHT] = new Node(x + halfSize, y + halfSize, halfSize);
				for (Node child : children) {
					child.buildTree(heightBuffer, pitch, leafSize);
				}
			} else {
				lods = log2(size) + 1;
				int leafPitch = size + 1;
				heights = new float[Utils.sqr(leafPitch)];
				float minZ = 1e06f, maxZ = -1e06f;
				errors = new float[lods];	
								
				for (int iy = 0; iy <= size; iy++) {
					heightBuffer.position((y + iy) * pitch + x);
					for (int ix = 0; ix <= size; ix++) {
						float z = ((heightBuffer.get() & 0xff) - 128)/ 255.0f * 220 * 0.25f;
						if (z < minZ) 
							minZ = z;
						if (z > maxZ) 
							maxZ = z;
						heights[iy * leafPitch + ix] = z;
					}
				}		
				
				int step = 1;
				for (int i = 0; i < lods; i++) {					
					float maxError = 0;
					
					for (int y = 0; y < leafPitch - 1; y += step) {
						for (int x = 0; x < leafPitch - 1; x += step) {
							float z00 = heights[y * leafPitch + x];
							float z10 = heights[y * leafPitch + x + step];
							float z01 = heights[(y + step) * leafPitch + x];
							float z11 = heights[(y + step) * leafPitch + x + step];
							
							for (int j = 0; j < step; j++) {
								float ys = j / step;								
								for (int k = 0; k < step; k++) {
									float xs = k / step;
									
									float z = heights[(y + j) * leafPitch + x + k];
									float iz = (xs + ys <= 1) ? 
							              (z00 + (z10 - z00) * xs + (z01 - z00) * ys) : (z11 + (z01 - z11) * (1 - xs) + (z10 - z11) * (1 - ys));
																		
									float error = Math.abs(iz - z); 
									maxError = Math.max(maxError, error);
								}								
							}
						}
					}
					
					step *= 2;
					errors[i] = maxError;
				}				
				
				bounds.min.set(x, y, minZ);
				bounds.max.set(x + size, y + size, maxZ);
				bounds.set(bounds.min, bounds.max);				
				radius = bounds.max.sub(bounds.getCenter()).len();
			} 
		}
		
		public void determineLod(Vector3 eyePos, float perspectiveFactor, float tolerance) {
			float distToEye = eyePos.dst(bounds.getCenter());			
			currentLod = 0;
			for (int i = lods - 1; i >= 1; i--) {
				if (errors[i] / distToEye * perspectiveFactor < tolerance) {
					currentLod = i;
					break;
				}
			}
		}
	}
	
	public static class RootNode extends Node {		
		public RootNode(int size) {			
			super(0, 0, size);
		}
	}


	
}
