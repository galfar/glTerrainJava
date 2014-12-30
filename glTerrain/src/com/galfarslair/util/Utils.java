package com.galfarslair.util;

import com.badlogic.gdx.Gdx;

public final class Utils {
	
	@SuppressWarnings("serial")
	public static class TerrainException extends Exception {
		public TerrainException(String message) {
			super(message);
		}	
	}
	
	private static final String LOG_TAG = "glTerrain";
	
	public static boolean isPow2(int x) {
		return (x & -x) == x;
	}
	
	public static int log2(int bits) {
		int log = 0;
	    if ((bits & 0xffff0000) != 0) { bits >>>= 16; log = 16; }
	    if (bits >= 256) { bits >>>= 8; log += 8; }
	    if (bits >= 16 ) { bits >>>= 4; log += 4; }
	    if (bits >= 4  ) { bits >>>= 2; log += 2; }
	    return log + (bits >>> 1);
	}
	
	public static int pow2(int x) {
		return (int)Math.pow(2, x);
	}
	
	public static int sqr(int x) {
		return x * x;
	}
	
	public static void logInfo(String msg, Object... params) {
		String logMsg = String.format(msg, params);
		Gdx.app.log(LOG_TAG, logMsg);
	}
	
	public static long elapsedTimeMs(long startTime) {
		return (System.nanoTime() - startTime) / 1000000;		
	}
	
	public static String formatElapsed(String msg, long startTime) {
		long elapsed = elapsedTimeMs(startTime);
		return String.format(msg + " %,d ms", elapsed);
	}
	
	public static void logElapsed(String msg, long startTime) {		
		String logMsg = formatElapsed(msg, startTime);
		Gdx.app.log(LOG_TAG, logMsg);
	}
	
	public static void delay(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
		}
	}
	
	public static float calcPhysicalSizeInCm(int pixelSize) {
		return pixelSize / Gdx.graphics.getPpcX();
	}
}
