package com.galfarslair.glterrain;

import junit.framework.Assert;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.galfarslair.glterrain.TerrainRunner;
import com.galfarslair.glterrain.util.PlatformSupport;

public class AndroidTerrainStarter extends AndroidApplication {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        String apkVersion = null;
        try {
        	apkVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
		}
        
        Assert.assertEquals("Android app and core versions don't match", TerrainRunner.VERSION, apkVersion);
        
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGL20 = true;
        cfg.useAccelerometer = true;
        cfg.useCompass = false;        
        cfg.depth = 24;        
        
        
        initialize(new TerrainRunner(new AndroidPlatformSupport()), cfg);
    }
    
    private static class AndroidPlatformSupport implements PlatformSupport {
		@Override
		public void enableWireframe() {
		}
		@Override
		public void updateDisplay() {
		}		
	}

}