/**
 * Copyright (c) www.longdw.com
 */
package com.music;

import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Environment;

import com.music.activity.IConstants;
import com.music.model.MusicInfo;
import com.music.service.BluetoothIntentReceiver;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.z.CrashHandler;
import com.z.utils.LogUtils;
;

public class MusicApp extends Application implements IConstants{
	
	public static boolean mIsSleepClockSetting = false;
	public static ServiceManager mServiceManager = null;//控制当前播放
	private static String rootPath = "/mymusic";
	public static String lrcPath = "/lrc";//网络下载歌词路径
	public static String lrcPathUse = "/lrc";
	private String logPath = Environment.getExternalStorageDirectory()+"/music.log";
	private SharedPreferences sp;
	public static SPStorage spSD;
	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
		lrcPath = sp.getString(LYRIC_DEFAULE_PATH, lrcPath);
		lrcPathUse = sp.getString(LYRIC_SAVE_PATH, "");
		spSD = new SPStorage(this);
		LogUtils.filePath = logPath;
		mServiceManager = new ServiceManager(this);//服务管理
		mServiceManager.connectService();//绑定蓝牙播放的服务
		CrashHandler crashHandler = CrashHandler.getInstance(null);
		crashHandler.init(getApplicationContext());
		//蓝牙控制
		((AudioManager)getSystemService(AUDIO_SERVICE))
			.registerMediaButtonEventReceiver(new ComponentName(this,BluetoothIntentReceiver.class));
		initPath();
	}
	
	/**
	 * @param musicList  重新设置播放列表
	 * @param selectedId 要选中的id
	 */
	public static void refreshMusicList(List<MusicInfo> musicList,int selectedId) {
		mServiceManager.refreshMusicList(musicList, selectedId);
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
