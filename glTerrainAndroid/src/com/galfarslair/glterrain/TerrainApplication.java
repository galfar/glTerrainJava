package com.galfarslair.glterrain;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
		mailTo = "marekmauder@gmail.com",
		customReportContent = { ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
				ReportField.TOTAL_MEM_SIZE, ReportField.AVAILABLE_MEM_SIZE, ReportField.BRAND, 
		        ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA,
		        ReportField.DISPLAY, ReportField.APPLICATION_LOG, ReportField.DEVICE_FEATURES,
		        ReportField.STACK_TRACE, ReportField.LOGCAT },           
        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crashToastText,
        resDialogText = R.string.crashDialogText,
        resDialogCommentPrompt = R.string.crashDialogCommentPrompt,
        resDialogOkToast = R.string.crashDialogOkToast) 
public class TerrainApplication extends Application {
	public static String APP_VERSION;
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
	}
}

