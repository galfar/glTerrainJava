package com.galfarslair.glterrain.vtf;

import static com.galfarslair.util.Utils.log2;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AbstractDocument.LeafElement;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

public class VtfMesh implements TerrainMesh {

	private static final int DEFAULT_LEAF_SIZE = 8;
	
	private int size;
	private int leafSize = DEFAULT_LEAF_SIZE;
	
	private HeightMap heightMap;	
	private RootNode root;
	private Array<Node> activeNodes;

	private int levels;
	private byte[][] lodLookup;
	private int gridMapSize;

	private int leafCount;
	
	public Array<Node> getActiveNodes() {
		return activeNodes;
	}
		
	@Override
	public void build(HeightMap heightMap) throws TerrainException {
		assert heightMap.getWidth() == heightMap.getHeight() : "Heightmap needs to be square";
		this.heightMap = heightMap;
		this.size = heightMap.getWidth() - 1;		
		assert Utils.isPow2(size) : "Heightmap needs to be 2^n+1 pixels in size";
		
		
		levels = log2(size) - log2(leafSize);		
		leafCount = (int)Math.pow(4, levels);
		gridMapSize = size / leafSize;
		lodLookup = new byte[gridMapSize][gridMapSize];
		
		root = new RootNode(size, leafSize);
						
		activeNodes = new Array<Node>(leafCount);		
	}

	@Override
	public void update(PerspectiveCamera camera, float tolerance) {
		final PerspectiveCamera localCamera = camera;
		//final float perspectiveFactor = (float) (camera.viewportHeight / (2 * Math.tan(Math.PI / 4)));
		//final float localTolerance = tolerance;
		activeNodes.clear();
						
		traverseQuadTree(root, new NodeAction() {
			@Override
			public boolean execute(Node node) {
				/*if (!localCamera.frustum.sphereInFrustumWithoutNearFar(node.bounds.getCenter(), node.radius)) {
					return false;
				}*/
				
				Vector2 p = new Vector2(localCamera.position.x, localCamera.position.y);
				Vector2 c = new Vector2(node.bounds.getCenter().x, node.bounds.getCenter().y);
				
				float dist = localCamera.position.dst(node.bounds.getCenter());
				dist = p.dst(c);
				//double treshold = Math.pow(5.0, levels - node.level);
				double treshold = Math.pow(5.0, levels - node.level);
				
				if (node.isLeaf) {			
					activeNodes.add(node);
					
					int lsize = node.size / leafSize;
					for (int y = 0; y < lsize; y++) {
						for (int x = 0; x < lsize; x++) {
							lodLookup[node.x / leafSize + x][node.y / leafSize + y] = (byte) node.level;
						}
					}				
					
					return false;
				}
								
				if (dist < treshold) {
				//if (node.level < levels) {
					return true;
				} else {
					activeNodes.add(node);
					
					// TODO: add to lookup
					int lsize = node.size / leafSize;
					for (int y = 0; y < lsize; y++) {
						for (int x = 0; x < lsize; x++) {
							lodLookup[node.x / leafSize + x][node.y / leafSize + y] = (byte) node.level;
						}
					}					
					
					return false;
				}				
			}
		});
	}
	
	public interface NodeAction {
		boolean execute(Node node);
	}
	
	public void visitQuadTreeLeaves(Node node, NodeAction action) {
		if (node.isLeaf) {
			action.execute(node);
		} else {
			for (Node child : node.children) {
				visitQuadTreeLeaves(child, action);
			}
		}
	}
	
	public void traverseQuadTree(Node node, NodeAction action) {
		if (action.execute(node)) {
			for (Node child : node.children) {
				traverseQuadTree(child, action);
			}
		}
	}

	@Override
	public int getSize() {		
		return size;
	}

	@Override
	public float getHeightAtPos(float x, float y) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public HeightMap getHeightMap() {
		return heightMap;
	}
	
	public int getTileSize() {		
		return leafSize;
	}
	
	public int[] lookupNeghborLods(Node node) {
		int[] lods = new int[] { node.level, node.level, node.level, node.level };
		
		int gridSize = node.size / leafSize;		
		int gridX = node.x / leafSize;
		int gridY = node.y / leafSize;
		
		if (gridX > 0) {
			lods[0] = lodLookup[gridX - 1][gridY]; // left neighbor LOD
		}
		if (gridY > 0) {
			lods[1] = lodLookup[gridX][gridY - 1]; // top neighbor LOD
		}		
		if ((gridX + gridSize) < gridMapSize) {
			lods[2] = lodLookup[gridX + gridSize][gridY]; // right neighbor LOD
		}
		if ((gridY + gridSize) < gridMapSize) {
			lods[3] = lodLookup[gridX][gridY + gridSize]; // bottom neighbor LOD
		}		
		
		return lods;
	}
	
	static class Node {
		public static final int CHILD_TOP_LEFT     = 0;
		public static final int CHILD_TOP_RIGHT    = 1;
		public static final int CHILD_BOTTOM_LEFT  = 2;
		public static final int CHILD_BOTTOM_RIGHT = 3;		
	
		public final int level;
		public final int size;		
		public final int x;
		public final int y;
		
		public Node[] children;
		public boolean isLeaf;

		public final BoundingBox bounds = new BoundingBox();
		public float radius;
		
		public Node(int level, int size, int x, int y) {
			this.level = level;
			this.size = size;
			this.x = x;
			this.y = y;			
			bounds.inf();			
			bounds.ext(x, y, -size / 2);
			bounds.ext(x + size, y + size, size / 2);
			radius = bounds.max.dst(bounds.getCenter());
		}
		
		protected void buildTree(int leafSize) {
			if (size > leafSize) {
				int halfSize = size / 2;
				int nextLevel = level + 1;
				children = new Node[4];
				children[CHILD_TOP_LEFT]     = new Node(nextLevel, halfSize, x, y);
				children[CHILD_TOP_RIGHT]    = new Node(nextLevel, halfSize, x + halfSize, y);
				children[CHILD_BOTTOM_LEFT]  = new Node(nextLevel, halfSize, x, y + halfSize);
				children[CHILD_BOTTOM_RIGHT] = new Node(nextLevel, halfSize, x + halfSize, y + halfSize);
				for (Node child : children) {
					child.buildTree(leafSize);
					
					/*if (child.bounds.min.z < minZ) {
						minZ = child.bounds.min.z;
					}
					if (child.bounds.max.z > maxZ) {
						maxZ = child.bounds.max.z;
					}*/
				}
			} else {
				isLeaf = true;
			}
		}
	}
	
	public static class RootNode extends Node {
		public RootNode(int size, int leafSize) {
			super(0, size, 0, 0);
			buildTree(leafSize);
		}
	}

}
