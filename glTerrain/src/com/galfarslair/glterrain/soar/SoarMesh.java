package com.galfarslair.glterrain.soar;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;
import com.galfarslair.glterrain.TerrainMesh;
import com.galfarslair.util.Utils;
import com.galfarslair.util.Utils.TerrainException;

import static com.galfarslair.util.Utils.logElapsed;

public class SoarMesh implements TerrainMesh {
	private float[] vertices;
	private float[] errors;
	private float[] radii;
	private int[] indices;
	
	private int refinementLevels;
	private int numVertices;
	private int numIndices;
	private int numTriangles;	
	private int size;
		
	private boolean cullingEnabled;
	private boolean forceFullRefinement;
	private int minLod;
	
	private Plane[] clippingPlanes;
	private final Vector3 viewPoint = new Vector3();
	
	private TriBuilder triBuilder = new TriBuilder();
	private Indexer indexer = new Indexer();
	private final Vector3 pos = new Vector3();
	private final Vector3 tmpVector = new Vector3();
	private float kappa;
	private float inverseKappa;
	
	private static final int SPHERE_UNDECIDED = 64;
	private static final int SPHERE_VISIBLE = 127;
		
	public SoarMesh() {
		cullingEnabled = true;				
		forceFullRefinement = false;
	}
	
	@Override
	public int getSize() {
		return size;
	}
	
	public int getNumVertices() {
		return numVertices;
	}
	
	public int getNumIndices() {
		return numIndices;
	}
	
	public float[] getVertices() {
		return vertices;
	}
	
	public int[] getIndices() {
		return indices;
	}
	
	public void build(Pixmap heightMap) throws TerrainException {
		size = heightMap.getWidth();
		assert (heightMap.getFormat() == Format.Intensity) || (heightMap.getFormat() == Format.Alpha) : "Invalid pxel format of heightmap";
		assert size == heightMap.getHeight() : "Heightmap needs to be square";
		assert Utils.isPow2(size - 1) : "Heightmap needs to be 2^n+1 pixels in size";
		
		long time = System.nanoTime();
		
		numVertices = size * size;
		refinementLevels =  Utils.log2(size - 1) * 2;
		
		vertices = new float[numVertices * 3];
		errors = new float[numVertices];
		radii = new float[numVertices];
		
		ByteBuffer heightBuffer = heightMap.getPixels();		
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				int idx = y * size + x;
				vertices[idx * 3 + 0] = x;
				vertices[idx * 3 + 1] = y;
				vertices[idx * 3 + 2] = (heightBuffer.get(idx) & 0xff) / 255.0f * size * HEIGHT_SCALE;				
			}			
		}
		
		computeTerrainLod();
		
		numIndices = size * size * 4;
		indices = new int[numIndices];
						
		logElapsed("Terrain build in:", time);
	}
	
	@Override
	public void update(PerspectiveCamera camera, float tolerance) {
		viewPoint.set(camera.position);
		clippingPlanes = camera.frustum.planes;	
		kappa = (tolerance / camera.viewportWidth) * (camera.fieldOfView * MathUtils.degreesToRadians);
		inverseKappa = kappa > 0.0f ? 1.0f / kappa : 1e06f;
		int mask = cullingEnabled ? SPHERE_UNDECIDED : SPHERE_VISIBLE;
		int levels = refinementLevels - 1;
		int halfLevels = refinementLevels / 2;
		
		triBuilder.stripBegin(indexer.indexSW(halfLevels), true);
	
		indexer.rootS(halfLevels);
		subMeshRefineVisible(levels, indexer.triI, indexer.triJ, indexer.triK, mask);
		triBuilder.stripAppend(indexer.indexSE(halfLevels), false);
		
		indexer.rootE(halfLevels);
		subMeshRefineVisible(levels, indexer.triI, indexer.triJ, indexer.triK, mask);
		triBuilder.stripAppend(indexer.indexNE(halfLevels), false);
		
		indexer.rootN(halfLevels);
		subMeshRefineVisible(levels, indexer.triI, indexer.triJ, indexer.triK, mask);		
		triBuilder.stripAppend(indexer.indexNW(halfLevels), false);
		
		indexer.rootW(halfLevels);
		subMeshRefineVisible(levels, indexer.triI, indexer.triJ, indexer.triK, mask);
		triBuilder.stripEnd(indexer.indexSW(halfLevels));		
	}

	@Override
	public float getHeightAtPos(float x, float y) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	private void subMeshRefineVisible(int level, int triI, int triJ, int triK, int mask) {
		if (mask == SPHERE_VISIBLE) {
			subMeshRefine(level, triI, triJ, triK);			
		} else {
			mask = isSphereVisible(indexer.split(triI, triJ, triK), mask);
			boolean refine = (level > minLod) && (mask > 0) && isVertexActive(indexer.split(triI, triJ, triK));
			
			if (refine) {
				indexer.childL(triI, triJ, triK);
				subMeshRefineVisible(level - 1, indexer.triI, indexer.triJ, indexer.triK, mask);
			}
			triBuilder.stripAppend(triI, (level & 1) != 0);
			if (refine) {
				indexer.childR(triI, triJ, triK);				
				subMeshRefineVisible(level - 1, indexer.triI, indexer.triJ, indexer.triK, mask);
			}
							
		}	
	}
	
	private void subMeshRefine(int level, int triI, int triJ, int triK) {
		boolean refine = (level > minLod) && (forceFullRefinement || isVertexActive(indexer.split(triI, triJ, triK)));
		
		if (refine) {
			indexer.childL(triI, triJ, triK);
			subMeshRefine(level - 1, indexer.triI, indexer.triJ, indexer.triK);
		}
		triBuilder.stripAppend(triI, (level & 1) != 0);
		if (refine) { 
			indexer.childR(triI, triJ, triK);
			subMeshRefine(level - 1, indexer.triI, indexer.triJ, indexer.triK);
		}
	}
	
	private void computeTerrainLod() {
		int n = 1 << (refinementLevels / 2);
		int m = n / 2;
		
		int i, j, s;
		int a, b, c;
		
		for (a = c = 1, b = 2, s = 0; a != n; a = c = b, b *= 2, s = n) {
			for (j = a; j < n; j += b) {
				for (i = 0; i <= n; i += b) {
					vertexLodCompute(i, j, 0, a, s);
					vertexLodCompute(j, i, a, 0, s);
				}
			}
			for (j = a; j < n; c = -c, j += b) {
				for (i = a; i < n; c = -c, i += b) {
					vertexLodCompute(i, j, a, c, n);
				}
			}
		}
		
		errors[0] = errors[n] = errors[n * size] = errors[n * size + n] = errors[m * size + m] = 1e06f;
		radii[0] = radii[n] = radii[n * size] = radii[n * size + n] = radii[m * size + m] = 1e06f;
	}
	
	private void vertexLodCompute(int i, int j, int di, int dj, int n) {
		int k;
		int idx = j * size + i;
		int cidx;
		float radius;
		
		pos.set(vertices[idx * 3 + 0], vertices[idx * 3 + 1], vertices[idx * 3 + 2]);				
		errors[idx] = Math.abs(pos.z - 0.5f * (vertices[((j - dj) * size + i - di) * 3 + 2] + vertices[((j + dj) * size + i + di) * 3 + 2]));
		radii[idx] = 0;
		
		if (n != 0) {
			dj = (di + dj) / 2;
			di -= dj;
			k = 4;
			do {
				if ((i != 0 || di >= 0) && (i != n || di <= 0) && 
					(j != 0 || dj >= 0) && (j != n || dj <= 0)) {
					
					cidx = (j + dj) * size + i + di;
					tmpVector.set(vertices[cidx * 3 + 0], vertices[cidx * 3 + 1], vertices[cidx * 3 + 2]);
					errors[idx] = Math.max(errors[idx], errors[cidx]);
					radius = pos.dst(tmpVector) + radii[cidx];
					radii[idx] = Math.max(radii[idx], radius);	
				}
				dj += di;
				di -= dj;
				dj += di;
			} while (--k > 0);
		}	
		
		
	}
	
	private int isSphereVisible(int idx, int mask) {
		pos.set(vertices[idx * 3 + 0], vertices[idx * 3 + 1], vertices[idx * 3 + 2]);
		float radius = radii[idx] + 1;
		float dist;	

		for (int i = 0; i < 6; i++) {
			if ((mask & (1 << i)) == 0) {
				dist = pos.dot(clippingPlanes[i].normal) + clippingPlanes[i].d;
				if (dist < -radius)
					return 0;
				if (dist > +radius)
					mask |= 1 << i;
			}
		}
		return mask;
	}
	
	private boolean isVertexActive(int idx) {		
		pos.set(vertices[idx * 3 + 0], vertices[idx * 3 + 1], vertices[idx * 3 + 2]);
		float d = inverseKappa * errors[idx] + radii[idx];
		return (d * d) > pos.dst2(viewPoint);		
	}	
	
	private class TriBuilder {
		
		void stripBegin(int firstIndex, boolean parity) {
			numIndices = 0;
			numTriangles = 0;
			indices[numIndices++] = firstIndex;
			indices[numIndices++] = firstIndex;
			indices[numIndices] = parity ? 1 : 0;
		}
		
		void stripAppend(int toAppend, boolean parity) {
			int tail = indices[numIndices - 2];
			int head = indices[numIndices - 1];
			boolean stripParity = indices[numIndices] > 0 ? true : false; 
			
			if ((toAppend != tail) && (toAppend != head)) {
				if (stripParity == parity) {
					indices[numIndices++] = tail;
				}				
				indices[numIndices++] = toAppend;
				indices[numIndices] = parity ? 1 : 0;				
				numTriangles++; 
			}	
		}
		
		void stripEnd(int toAppend) {
			indices[numIndices++] = toAppend;			
		    numTriangles++;		    
		}
	}
	
	private static class Indexer {
		
		int triI;
		int triJ;
		int triK;
		
		int linearIndex(int i, int j, int m) {
			return i + j + (j << m);
		}
		
		int indexSW(int m) {
			return linearIndex(0 << m, 0 << m, m);
		}
		
		int indexSE(int m) {
			return linearIndex(1 << m, 0 << m, m);
		}
		
		int indexNW(int m) {
			return linearIndex(0 << m, 1 << m, m);
		}
		
		int indexNE(int m) {
			return linearIndex(1 << m, 1 << m, m);
		}
		
		int indexC(int m) {
			return linearIndex(1 << (m - 1), 1 << (m - 1), m);
		}
		
		void rootS(int m) {
			triI = indexC(m);
			triJ = indexSW(m);
			triK = indexSE(m);
		}
		
		void rootN(int m) {
			triI = indexC(m);
			triJ = indexNE(m);
			triK = indexNW(m);
		}

		void rootW(int m) {
			triI = indexC(m);
			triJ = indexNW(m);
			triK = indexSW(m);
		}

		void rootE(int m) {
			triI = indexC(m);
			triJ = indexSE(m);
			triK = indexNE(m);
		}

		int split(int i, int j, int k) {
			return (j + k) / 2;
		}
		
		void childR(int i, int j, int k) {
			triI = split(i, j, k);
			triJ = i;
			triK = k;						
		}

		void childL(int i, int j, int k) {
			triI = split(i, j, k);
			triJ = j;
			triK = i;	
		}		
	}
	
}
