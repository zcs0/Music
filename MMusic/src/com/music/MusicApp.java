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
import com.z.utils.LogUtils;
;

public class MusicApp extends Application implements IConstants{
	
	public static boolean mIsSleepClockSetting = false;
	public static ServiceManager mServiceManager = null;//控制当前播放
	private static String rootPath = "/mymusic";
	public static String lrcPath = "/lrc";
	public static String lrcPathUse = "/lrc";
	private String logPath = Environment.getExternalStorageDirectory()+"/music.log";
	private SharedPreferences sp;
	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
		lrcPath = sp.getString(LYRIC_DEFAULE_PATH, lrcPath);
		lrcPathUse = sp.getString(LYRIC_SAVE_PATH, "");
		LogUtils.filePath = logPath;
		mServiceManager = new ServiceManager(this);//服务管理
		mServiceManager.connectService();//绑定播放的服务
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
