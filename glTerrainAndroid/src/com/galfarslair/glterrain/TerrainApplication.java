package com.galfarslair.glterrain;

import android.app.Application;
import android.content.pm.PackageManager.NameNotFoundException;

import org.acra.*;
import org.acra.annotation.*;

//@ReportsCrashes(formKey = "dGVacG0ydVHnaNHjRjVTUTEtb3FPWGc6MQ") 
public class TerrainApplication extends Application {
	public static String APP_VERSION;
	@Override
	public void onCreate() {
		super.onCreate();

		

		ACRA.init(this);
	}
}

