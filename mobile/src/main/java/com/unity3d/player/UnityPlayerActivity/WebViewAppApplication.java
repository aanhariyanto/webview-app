package com.unity3d.player.UnityPlayerActivity;

import android.app.Application;
import android.content.Context;

public class WebViewAppApplication extends Application {
	private static WebViewAppApplication sInstance;

	public WebViewAppApplication() {
		sInstance = this;
	}


	public static Context getContext() {
		return sInstance;
	}


	@Override
	public void onCreate() {
		super.onCreate();

		// force AsyncTask to be initialized in the main thread due to the bug:
		// http://stackoverflow.com/questions/4280330/onpostexecute-not-being-called-in-asynctask-handler-runtime-exception
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
    }
}
