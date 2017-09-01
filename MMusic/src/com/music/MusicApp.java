/**
 * Copyright (c) www.longdw.com
 */
package com.music;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.music.activity.IConstants;
import com.music.model.MusicInfo;
import com.music.service.BluetoothIntentReceiver;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.utils.MusicTimer;
import com.music.utils.TimerListener;
import com.z.CrashHandler;
import com.z.utils.LogUtils;

public class MusicApp extends Application implements IConstants{
	
	public static boolean mIsSleepClockSetting = false;
	public static ServiceManager mServiceManager = null;//控制当前播放
	private static String rootPath = "/mymusic";
	public static String lrcPath = "/lrc";//网络下载歌词路径
	public static String lrcPathUse = "/lrc";
	private Handler mHandler;
	private String logPath = Environment.getExternalStorageDirectory()+"/music.log";
	private SharedPreferences sp;
	public static SPStorage spSD;
	private static List<TimerListener> timeList= new ArrayList();
	private static MusicTimer mMusicTimer;
	@Override
	public void onCreate() {
		super.onCreate();
		sp = getSharedPreferences(SP_NAME,Context.MODE_WORLD_WRITEABLE);
		lrcPath = sp.getString(LYRIC_DEFAULE_PATH, lrcPath);
		lrcPathUse = sp.getString(LYRIC_SAVE_PATH, "");
		spSD = new SPStorage(this);
		LogUtils.filePath = logPath;
		LogUtils.isPrint = true;
		LogUtils.isSave = true;
		mServiceManager = new ServiceManager(this);//服务管理
		mServiceManager.connectService();//绑定蓝牙播放的服务
		CrashHandler crashHandler = CrashHandler.getInstance(null);
		crashHandler.init(getApplicationContext());
		//蓝牙控制
		((AudioManager)getSystemService(AUDIO_SERVICE))
			.registerMediaButtonEventReceiver(new ComponentName(this,BluetoothIntentReceiver.class));
		initPath();
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(timeList!=null&&timeList.size()>0){
					for (int i = 0; i < timeList.size(); i++) {
						TimerListener timeListener = timeList.get(i);
						timeListener.update(i);
					}
				}
			}
		};
		mMusicTimer = new MusicTimer(mHandler);
	}
	
	/**
	 * @param musicList  重新设置播放列表
	 * @param selectedId 要选中的id
	 */
	public static void refreshMusicList(List<MusicInfo> musicList,int selectedId) {
		mServiceManager.refreshMusicList(musicList, selectedId);
	}
	/**
	 * 添加一个循环时间监听
	 * @param time
	 */
	public static void addTimerListener(TimerListener timer){
		if(timer==null) return;
		timeList.add(timer);
	}
	/**
	 * 移除一个循环时间监听
	 * @param time
	 */
	public static void removeTimerListener(TimerListener timer){
		if(timer==null) return;
		timeList.remove(timer);
	}
	/**
	 * 清空所有循环时间监听
	 * @param time
	 */
	public static void removeTimeListenerAll(){
		timeList.clear();
	}
	/**
	 * 开时间循环
	 */
	public static void startTimer(){
		mMusicTimer.startTimer();
	}
	public static void stopTimer(){
		mMusicTimer.stopTimer();
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
