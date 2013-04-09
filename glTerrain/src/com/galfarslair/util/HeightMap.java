package com.galfarslair.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPInputStream;

/*import ar.com.hjg.pngj.FileHelper;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngReader;*/

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.BufferUtils;
import com.galfarslair.glterrain.util.Assets;
import com.galfarslair.util.Utils.TerrainException;

public class HeightMap {
	public enum Format {
	  Byte, Short, Float	
	}
	
	private int width;
	private int height;
	private int bitDepth;
	private short[] data;
	private ByteBuffer rawSamples;
	private ShortBuffer samples;
	
	public static class HeightmapException extends TerrainException {
		public HeightmapException(String message) {
			super(message);
		}
		public HeightmapException(String message, FileHandle file) {			
			super(message + "\nHeightmap file: " + file.name());
		}
	}
	
	public HeightMap() {
		
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getBitDepth() {
		return bitDepth;
	}
	
	/***
	 * Loads heightmap from RAW file. 16bit unsigned values, square size, 
	 * and n^2+1 dimensions are expected.   
	 * @param file
	 * @throws IOException 
	 * @throws HeightmapException 
	 */
	public void loadFromRaw(FileHandle file) throws IOException, HeightmapException {
		int count = (int) (file.length() / 2);
		int side = (int) Math.sqrt(count);
		
		if (side * side != count) {
			throw new HeightmapException("RAW heightmap source is not square", file);
		}
		
		width = side;
		height = side;
	    	    
	    long time = System.nanoTime();	    
	    DataInputStream in = Assets.getFileStream(file);
	    
	    rawSamples = BufferUtils.newByteBuffer(width * height * 2);
	    samples = rawSamples.asShortBuffer();
	    
	    byte[] buffer = new byte[1024 * 16];
	    int readBytes = 0;

	    while ((readBytes = in.read(buffer)) != -1) {
	    	rawSamples.put(buffer, 0, readBytes);
		}
	    	    
		Utils.logElapsed("Heightmap RAW loaded in: ", time);
	}
	
	public void loadFromPng(FileHandle file) throws HeightmapException {
		long time = System.nanoTime();
		
		/*PngReader reader = new PngReader(file.read(), file.name());
		ImageInfo imgInfo = reader.imgInfo;
		
		if (!imgInfo.greyscale || imgInfo.alpha) {
			throw new Exception("Heightmap must be grayscale PNG");
		}
		
		width = imgInfo.cols;
		height = imgInfo.rows;
		bitDepth = imgInfo.bitDepth;
		
		checkSourceProps();
		
		buffer = BufferUtils.newByteBuffer(width * height);
		for (int i = 0; i < height; i++) {
			byte[] bytes = reader.readRowByte(null, i);
			buffer.put(bytes, 0, bytes.length);
		}
		
	    Utils.logElapsed("HM loaded in: ", time);
	    
	    buffer = BufferUtils.newByteBuffer(width * height);
	    
	    */	
	}
	
	public ShortBuffer getSamples() {
		return samples;
	}
	
	public short[] getRegion(int x, int y, int width, int height) {
		return null;
	}
	
	public float[] getRegionNormalized(int x, int y, int width, int height) {
		return null;
	}
	
	private void checkSourceProps() {
		
	}

}
