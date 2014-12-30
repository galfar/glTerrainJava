package com.galfarslair.glterrain;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        formKey = "",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUri = "https://galfar.cloudant.com/acra-glterraindemo/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "lfbutypseredgmartomplect",
        formUriBasicAuthPassword = "vPnruPgXRUnc38oNDwdVblWf",
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

