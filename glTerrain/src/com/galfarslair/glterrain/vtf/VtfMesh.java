package com.galfarslair.glterrain.vtf;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.util.HeightMap;
import com.galfarslair.util.Utils.TerrainException;

public class VtfMesh implements TerrainMesh {

	private static final int DEFAULT_TILE_SIZE = 64;
	
	private int size;
	private int tileSize = DEFAULT_TILE_SIZE;
	
	private HeightMap heightMap;
	private List<Tile> tiles = new ArrayList<Tile>();
	
	@Override
	public void build(HeightMap heightMap) throws TerrainException {
		this.heightMap = heightMap;
		
		size = heightMap.getWidth();
		
		int tileCount = (size - 1) / tileSize;
		for (int x = 0; x < tileCount; x++) {
			for (int y = 0; y < tileCount; y++) {
				Tile tile = new Tile(tileSize, x * tileSize, y * tileSize);
				tiles.add(tile);
			}
		}		
	}

	@Override
	public void update(PerspectiveCamera camera, float tolerance) {
		

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
		return tileSize;
	}
	
	public List<Tile> getTiles() {
		return tiles;
	}
	
	static class Tile {
		public final int size;		
		public final int x;
		public final int y;
		
		public final BoundingBox bounds = new BoundingBox();
		
		public Tile(int size, int x, int y) {
			this.size = size;
			this.x = x;
			this.y = y;
			bounds.ext(x, y, -50);
			bounds.ext(x + size, y + size, 50);
		}
	}

}
