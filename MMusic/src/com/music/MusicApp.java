/**
 * Copyright (c) www.longdw.com
 */
package com.music;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.music.activity.IConstants;
import com.music.service.ServiceManager;
import com.z.CrashHandler;
;

public class MusicApp extends Application implements IConstants{
	
	public static boolean mIsSleepClockSetting = false;
	public static ServiceManager mServiceManager = null;
	private static String rootPath = "/mymusic";
	public static String lrcPath = "/lrc";
	public static String lrcPathUse = "/lrc";
	private SharedPreferences sp;
	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
		lrcPath = sp.getString(LYRIC_DEFAULE_PATH, "/lrc");
		lrcPathUse = sp.getString(LYRIC_SAVE_PATH, "");
		mServiceManager = new ServiceManager(this);//服务管理
		CrashHandler crashHandler = CrashHandler.getInstance(null);
		crashHandler.init(getApplicationContext());
		initPath();
	}
	
	private void initPath() {
		String ROOT = "";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			ROOT = Environment.getExternalStorageDirectory().getPath();
		}
//		rootPath = ROOT + rootPath;
//		lrcPath = rootPath + lrcPath;
//		File lrcFile = new File(lrcPath);
//		if(lrcFile.exists()) {
//			lrcFile.mkdirs();
//		}
	}
}
