/**
 * Copyright (c) www.longdw.com
 */
package com.music.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;

import com.music.activity.IConstants;
import com.music.aidl.IMediaService;
import com.music.interfaces.IOnServiceConnectComplete;
import com.music.model.MusicInfo;

/**
 * 控制Service
 * @author 
 *
 */
public class ServiceManager implements IConstants {

	public IMediaService mService;
	private Context mContext;
	private ServiceConnection mConn;
	private IOnServiceConnectComplete mIOnServiceConnectComplete;

	public ServiceManager(Context context) {
		this.mContext = context;
		initConn();	
	}

	private void initConn() {
		mConn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IMediaService.Stub.asInterface(service);
				if (mService != null) {
					mIOnServiceConnectComplete.onServiceConnectComplete(mService);
				}
			}
		};
	}
	/**
	 * 绑定服务 蓝牙
	 */
	public void connectService() {
		Intent intent = new Intent(IConstants.SERVICE_NAME/*"com.music.service.MediaService"*/);
		mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
	}
	
	public void disConnectService() {
		mContext.unbindService(mConn);
		mContext.stopService(new Intent(IConstants.SERVICE_NAME));//"com.music.service.MediaService"
	}
	
	/**
	 * 重新设置播放列表
	 * @param musicList
	 */
	public void refreshMusicList(List<MusicInfo> musicList) {
		if(musicList != null && mService != null) {
			try {
				mService.refreshMusicList(musicList);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param musicList  重新设置播放列表
	 * @param selectedId 要选中的id
	 */
	public void refreshMusicList(List<MusicInfo> musicList,int selectedId) {
		if(musicList != null && mService != null) {
			try {
				mService.refreshMusicList2(musicList,selectedId);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<MusicInfo> getMusicList() {
		List<MusicInfo> musicList = new ArrayList<MusicInfo>();
		try {
			if(mService != null) {
				mService.getMusicList(musicList);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return musicList;
	}
	/**
	 * 选中第几个，但状态为暂停
	 * @param pos
	 * @return
	 */
	public boolean play(int pos) {
		if(mService != null) {
			try {
				return mService.play(pos);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * 删除播放列表的一个
	 * @param id songId
	 * @return
	 */
	public boolean removeSongId(int id){
		try {
			return mService.reSongId(id);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean playById(int id) {
		if(mService != null) {
			try {
				return mService.playById(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * 继续播放
	 * @return
	 */
	public boolean rePlay() {
		if(mService != null) {
			try {
				return mService.rePlay();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean pause() {
		if(mService != null) {
			try {
				return mService.pause();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * 上一首
	 * @return
	 */
	public boolean prev() {
		if(mService != null) {
			try {
				return mService.prev();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean next() {
		if(mService != null) {
			try {
				return mService.next();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean seekTo(int progress) {
		if(mService != null) {
			try {
				return mService.seekTo(progress);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/**
	 * 获取当前播放位置。
	 * @return
	 */
	public int position() {
		if(mService != null) {
			try {
				return mService.position();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	/**
	 * 总时长
	 * @return
	 */
	public int duration() {
		if(mService != null) {
			try {
				return mService.duration();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	/**
	 * MPS_NOFILE准备中</br>
	 * MPS_PLAYING正在播放中</br>
	 * MPS_PAUSE 暂停中</br>
	 * MPS_PREPARE 等待中</br>
	 * MPS_INVALID 不可播放的文件</br>
	 * {@link IConstants}
	 * @return
	 */
	public int getPlayState() {
		if(mService != null) {
			try {
				return mService.getPlayState();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	/**
	 * 播放模式
	 * @param mode </br>{@link IConstants#MPM_LIST_LOOP_PLAY}}：列表循环
	 * <p>{@link IConstants#MPM_ORDER_PLAY}}:顺序播放</p>
	 * <p>{@link IConstants#MPM_RANDOM_PLAY}}:随机播放</p>
	 * <p>{@link IConstants#MPM_SINGLE_LOOP_PLAY}}:单曲循环</p>
	 */
	public void setPlayMode(int mode) {
		if(mService != null) {
			try {
				mService.setPlayMode(mode);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 播放模式
	 * @param mode </br>{@link IConstants#MPM_LIST_LOOP_PLAY}}：列表循环
	 * <p>{@link IConstants#MPM_ORDER_PLAY}}:顺序播放</p>
	 * <p>{@link IConstants#MPM_RANDOM_PLAY}}:随机播放</p>
	 * <p>{@link IConstants#MPM_SINGLE_LOOP_PLAY}}:单曲循环</p>
	 */
	public int getPlayMode() {
		if(mService != null) {
			try {
				return mService.getPlayMode();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	/**
	 * 正在播放的是第几个mCurMusic.songId
	 * @return songId
	 */
	public int getCurMusicId() {
		if(mService != null) {
			try {
				return mService.getCurMusicId();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	/**
	 * 当前正在播放的歌词
	 * @return
	 */
	public MusicInfo getCurMusic() {
		if(mService != null) {
			try {
				return mService.getCurMusic();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void sendBroadcast() {
		if(mService != null) {
			try {
				mService.sendPlayStateBrocast();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void exit() {
		if(mService != null) {
			try {
				mService.exit();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mContext.unbindService(mConn);
		mContext.stopService(new Intent(SERVICE_NAME));
	}
	/**
	 * 弹出顶部提示
	 * @param bitmap
	 * @param title
	 * @param name
	 */
	public void updateNotification(Bitmap bitmap, String title, String name) {
		try {
			mService.updateNotification(bitmap, title, name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void cancelNotification() {
		try {
			mService.cancelNotification();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 设置监听
	 * @param IServiceConnect
	 */
	public void setOnServiceConnectComplete(
			IOnServiceConnectComplete IServiceConnect) {
		mIOnServiceConnectComplete = IServiceConnect;
	}
	/**
	 * 不管用
	 * @return
	 */
	public int getCurPlayIndex(){
		try {
			mService.getCurPlayIndex();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

}
