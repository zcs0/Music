/**
 * Copyright (c) www.longdw.com
 */
package com.music.service;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.RemoteViews;

import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MainContentActivity;
import com.music.aidl.IMediaService;
import com.music.model.MusicInfo;
import com.music.shake.ShakeDetector;
import com.music.shake.ShakeDetector.OnShakeListener;
import com.music.storage.SPStorage;

/**
 * 后台Service 控制歌曲的播放 控制顶部Notification的显示
 * @author 
 *
 */
public class MediaService extends Service implements IConstants, OnShakeListener {
	
	private static final int PAUSE_FLAG = 0x1;//updateNotification
	private static final int NEXT_FLAG = 0x2;
	private static final int PRE_FLAG = 0x3;
	private static final int EXIT_FLAG = 0x4;
	
	private MusicControl mMc;
	private NotificationManager mNotificationManager;
//	private Notification mNotification;
	private int NOTIFICATION_ID = 0x1;
	private RemoteViews rv;
	private ShakeDetector mShakeDetector;
	/** 当前是否正在播放 */
	private boolean mIsPlaying;
	/** 在设置界面是否开启了摇一摇的监听 */
	public boolean mShake;
	private SPStorage mSp;
	private ControlBroadcast mConrolBroadcast;//updateNotification更新
	private MusicPlayBroadcast mPlayBroadcast;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mMc = new MusicControl(this);
		mSp = new SPStorage(this);
		mShakeDetector = new ShakeDetector(this);
		mShakeDetector.setOnShakeListener(this);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		mConrolBroadcast = new ControlBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(PAUSE_BROADCAST_NAME);
		filter.addAction(NEXT_BROADCAST_NAME);
		filter.addAction(PRE_BROADCAST_NAME);
		filter.addAction(EXIT_BROADCAST_NAME);
		registerReceiver(mConrolBroadcast, filter);
		
		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter1 = new IntentFilter(BROADCAST_NAME);
		filter1.addAction(BROADCAST_SHAKE);
		registerReceiver(mPlayBroadcast, filter1);
//		sendBluetooth();//打开蓝牙监听
	}

	
	/** 
	 * 更新notification
	 * @param bitmap
	 * @param title
	 * @param name
	 */
	private void updateNotification(Bitmap bitmap, String title, String name) {
		Intent intent = new Intent(getApplicationContext(),
				MainContentActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),
				0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		rv = new RemoteViews(this.getPackageName(), R.layout.notification);
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = title;
		notification.contentIntent = pi;
		notification.contentView = rv;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		
		if(bitmap != null) {
			rv.setImageViewBitmap(R.id.image, bitmap);
		} else {
			rv.setImageViewResource(R.id.image, R.drawable.img_album_background);
		}
		rv.setTextViewText(R.id.title, title);
		rv.setTextViewText(R.id.text, name);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		//此处action不能是一样的 如果一样的 接受的flag参数只是第一个设置的值
		Intent pauseIntent = new Intent(PAUSE_BROADCAST_NAME);//暂停
		pauseIntent.putExtra("FLAG", PAUSE_FLAG);
		PendingIntent pausePIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
		rv.setOnClickPendingIntent(R.id.iv_pause, pausePIntent);
		
		Intent nextIntent = new Intent(NEXT_BROADCAST_NAME);//下一首
		nextIntent.putExtra("FLAG", NEXT_FLAG);
		PendingIntent nextPIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
		rv.setOnClickPendingIntent(R.id.iv_next, nextPIntent);
		
		Intent preIntent = new Intent(PRE_BROADCAST_NAME);//上一首
		preIntent.putExtra("FLAG", PRE_FLAG);
		PendingIntent prePIntent = PendingIntent.getBroadcast(this, 0, preIntent, 0);
		rv.setOnClickPendingIntent(R.id.iv_previous, prePIntent);
		
		Intent exit = new Intent(EXIT_BROADCAST_NAME);
		exit.putExtra("FLAG", EXIT_FLAG);
		PendingIntent i = PendingIntent.getBroadcast(this, 0, exit, 0);
		rv.setOnClickPendingIntent(R.id.iv_exit, i);
		
		startForeground(NOTIFICATION_ID, notification);
	}
	
	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				switch (playState) {
				case MPS_PLAYING:
					mIsPlaying = true;
					if(mSp.getShake()) {
						mShakeDetector.start();
					}
					break;
					default:
						mIsPlaying = false;
						mShakeDetector.stop();
				}
			} else if(intent.getAction().equals(BROADCAST_SHAKE)) {
				mShake = intent.getBooleanExtra(SHAKE_ON_OFF, false);
				if(mShake && mIsPlaying) {//如果开启了监听并且歌曲正在播放
					mShakeDetector.start();
				} else if(!mShake) {
					mShakeDetector.stop();
				}
			}
		}
	}
	
	private class ControlBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
//			Bundle extras = intent.getExtras();
//			String string = extras.getString("PPPP");
			int flag = intent.getIntExtra("FLAG", -1);
			switch(flag) {
			case PAUSE_FLAG:
//				MediaService.this.stopForeground(true);
				if(mMc.getPlayState()==MPS_PLAYING){//正在播放中
					mMc.pause();//暂停
				}else{
					mMc.replay();
				}
				break;
			case NEXT_FLAG://下一首
				mMc.next();
				break;
			case PRE_FLAG://上一首
				mMc.prev();
				break;
			case EXIT_FLAG://退出
				cancelNotification();
				mMc.exit();
			}
		}
	}
	
	private void cancelNotification() {
		stopForeground(true);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private class ServerStub extends IMediaService.Stub {

		@Override
		public boolean pause() throws RemoteException {
//			MediaService.this.stopForeground(true);
			return mMc.pause();
		}

		@Override
		public boolean prev() throws RemoteException {
			return mMc.prev();
		}

		@Override
		public boolean next() throws RemoteException {
			return mMc.next();
		}

		@Override
		public boolean play(int pos) throws RemoteException {
			return mMc.play(pos);
		}

		@Override
		public int duration() throws RemoteException {
			return mMc.duration();
		}

		@Override
		public int position() throws RemoteException {
			return mMc.position();
		}

		@Override
		public boolean seekTo(int progress) throws RemoteException {
			return mMc.seekTo(progress);
		}

		@Override
		public void refreshMusicList(List<MusicInfo> musicList) throws RemoteException {
			mMc.refreshMusicList(musicList);
		}

		@Override
		public void getMusicList(List<MusicInfo> musicList) throws RemoteException {
			List<MusicInfo> music = mMc.getMusicList();
			for (MusicInfo m : music) {
				musicList.add(m);
			}
		}

		@Override
		public int getPlayState() throws RemoteException {
			return mMc.getPlayState();
		}

		@Override
		public int getPlayMode() throws RemoteException {
			return mMc.getPlayMode();
		}

		@Override
		public void setPlayMode(int mode) throws RemoteException {
			mMc.setPlayMode(mode);
		}

		@Override
		public void sendPlayStateBrocast() throws RemoteException {
			mMc.sendBroadCast();
		}

		@Override
		public void exit() throws RemoteException {
			cancelNotification();
			stopSelf();
			mMc.exit();
		}

		@Override
		public boolean rePlay() throws RemoteException {
			return mMc.replay();
		}

		@Override
		public int getCurMusicId() throws RemoteException {
			return mMc.getCurMusicId();
		}

		@Override
		public void updateNotification(Bitmap bitmap, String title, String name)
				throws RemoteException {
			MediaService.this.updateNotification(bitmap, title, name);
		}

		@Override
		public void cancelNotification() throws RemoteException {
			MediaService.this.cancelNotification();
		}

		@Override
		public boolean playById(int id) throws RemoteException {
			return mMc.playById(id);
		}

		@Override
		public MusicInfo getCurMusic() throws RemoteException {
			return mMc.getCurMusic();
		}
		@Override
		public boolean reSongId(int id){
			return mMc.removeById(id);
		}

		@Override
		public int getCurPlayIndex() throws RemoteException {
			return mMc.getCurPlayIndex();
		}

		@Override
		public void refreshMusicList2(List<MusicInfo> musicList, int selectedId)
				throws RemoteException {
			mMc.refreshMusicList(musicList,selectedId);
			
		}
		
	}
	
	private final IBinder mBinder = new ServerStub();
	private BluetoothIntentReceiver bluetoothReceiver;

	@Override
	public void onShake() {
		mMc.next();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mConrolBroadcast != null) {
			unregisterReceiver(mConrolBroadcast);
		}
		if(mPlayBroadcast != null) {
			unregisterReceiver(mPlayBroadcast);
		}
		if(bluetoothReceiver!=null){
			unregisterReceiver(bluetoothReceiver);
		}
	}
	//蓝牙
	private void sendBluetooth(){
				IntentFilter intent = new IntentFilter();
				intent.setPriority(Integer.MAX_VALUE);
//				intent.addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
				intent.addAction("android.media.AUDIO_BECOMING_NOISY");
				intent.addAction("android.intent.action.MEDIA_BUTTON");
				intent.addAction("android.intent.action.VOICE_COMMAND");
//				intent.addAction("android.intent.action.ACTION_SCREEN_ON");//锁屏时会自动退出程序
//				intent.addAction("android.intent.action.SCREEN_OFF");//锁屏时会自动退出程序
				intent.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
				intent.addAction("android.intent.action.ACTION_SHUTDOWN");
				intent.addAction("android.intent.action.BOOT_COMPLETED");
				intent.addAction("android.permission.BLUETOOTH");
				intent.addAction("android.permission.BLUETOOTH_ADMIN");
				intent.addAction("android.intent.action.UPDATE_SUSPEND_TIME_BY_HAND");
				intent.addAction("android.media.AUDIO_BECOMING_NOISY");
				intent.addAction("android.intent.action.MEDIA_BUTTON");
				intent.addAction("android.intent.action.VOICE_COMMAND");
				intent.addAction("android.intent.action.ACTION_SCREEN_ON");
				intent.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
				intent.addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
				intent.addAction("android.intent.action.ACTION_SHUTDOWN");
				intent.addAction("android.intent.action.BOOT_COMPLETED");
				intent.addAction("android.intent.action.UPDATE_SUSPEND_TIME_BY_HAND");
				bluetoothReceiver = new BluetoothIntentReceiver();
				registerReceiver(bluetoothReceiver, intent);
//				bluetoothReceiver.abortBroadcast();//中断下一个接收
	}

}
