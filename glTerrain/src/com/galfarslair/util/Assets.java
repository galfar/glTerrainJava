package com.galfarslair.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Assets {

	public static FileHandle getFile(String dataPath) {
		return Gdx.files.internal("data/" + dataPath);
	}
	
}
