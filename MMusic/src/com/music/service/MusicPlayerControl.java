/**
 * Copyright (c) www.longdw.com
 */
package com.music.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;

import com.music.activity.IConstants;
import com.music.activity.MainContentActivity;
import com.music.model.MusicInfo;
import com.music.utils.MusicUtils;
import com.z.utils.LogUtils;

/**
 * 歌曲控制(在serice中创建，负责MediaPlayer播放控制)
 * @author 
 *
 */
public class MusicPlayerControl implements IConstants, OnCompletionListener {
	
	private String TAG = MusicPlayerControl.class.getSimpleName();
	private MediaPlayer mMediaPlayer;
	private int mPlayMode;
	private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
	private int mPlayState;
	private int mCurPlayIndex;
	private Context mContext;
	private Random mRandom;
	private int mCurMusicId;
	private MusicInfo mCurMusic;
	
	
	public MusicPlayerControl(Context context) {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(this);
		mPlayMode = MPM_LIST_LOOP_PLAY;
		mPlayState = MPS_NOFILE;
		mCurPlayIndex = -1;
		mCurMusicId = -1;
		this.mContext = context;
		mRandom = new Random();
		mRandom.setSeed(System.currentTimeMillis());
	}
	/**
	 * 
	 * @param pos 集合中的位置
	 * @return
	 */
	public boolean play(int pos) {
//		if(mPlayState == MPS_NOFILE || mPlayState == MPS_INVALID) {
//			return false;
//		}
		if(mCurPlayIndex == pos) {
			if(!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				mPlayState = MPS_PLAYING;
				sendBroadCast();
			} else {
				pause();
			}
			return true;
		}
		if(!prepare(pos)) {
			return false;
		}
		return replay();
	}
	
	/**
	 * 根据歌曲的id来播放
	 * @param songId
	 * @return
	 */
	public boolean playById(int id) {
		int position = MusicUtils.seekPosInListById(mMusicList, id);
		position = position<0?0:position;
		mCurPlayIndex = position;
		if(mCurMusicId == id) {
			if(!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				mPlayState = MPS_PLAYING;
				sendBroadCast();
			} else {
				pause();
			}
			return true;
		}
		
		
		if(!prepare(position)) {
			return false;
		}
		return replay();
	}
	/**
	 * 接着开始播放
	 * @return
	 */
	public boolean replay() {
		if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {//当前音乐文件无效,无音乐文件
			return false;
		}
			
		mMediaPlayer.start();
		mPlayState = MPS_PLAYING;
		sendBroadCast();
		return true;
	}
	
	public boolean pause() {
		if(mPlayState != MPS_PLAYING) {
			return false;
		}
		mMediaPlayer.pause();
		mPlayState = MPS_PAUSE;
		sendBroadCast();
		return true;
	}
	/**
	 * 上一个
	 * @return
	 */
	public boolean prev() {
		if(mPlayState == MPS_NOFILE) {
			return false;
		}
		mCurPlayIndex--;
		mCurPlayIndex = reviseIndex(mCurPlayIndex);
		if(!prepare(mCurPlayIndex)) {
			return false;
		}
		return replay();
	}
	
	public boolean next() {
		if(mPlayState == MPS_NOFILE) {//是否是无文件状态
			return false;
		}
		mCurPlayIndex++;
		mCurPlayIndex = reviseIndex(mCurPlayIndex);
		if(!prepare(mCurPlayIndex)) {
			return false;
		}
		return replay();
	}
	
	private int reviseIndex(int index) {
		if(index < 0) {
			index = mMusicList.size() - 1;
		}
		if(index >= mMusicList.size()) {
			index = 0;
		}
		return index;
	}
	/**
	 * 获取当前播放时长位置。
	 * @return
	 */
	public int position() {
		if(mPlayState == MPS_PLAYING || mPlayState == MPS_PAUSE) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}
	
	/**
	 * 毫秒 获取文件的总持续时间。总时长
	 * @return
	 */
	public int duration() {
		if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
			return 0;
		}
		return mMediaPlayer.getDuration();
	}
	
	public boolean seekTo(int progress) {
		if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
			return false;
		}
//		int pro = reviseSeekValue(progress);
//		int time = mMediaPlayer.getDuration();
//		int curTime = (int)((float)pro / 100 * time);
		mMediaPlayer.seekTo(progress);
		return true;
	}
	
	private int reviseSeekValue(int progress) {
		if(progress < 0) {
			progress = 0;
		} else if(progress > 100) {
			progress = 100;
		}
		return progress;
	}
	
	public void refreshMusicList(List<MusicInfo> musicList) {
		mMusicList.clear();
		mMusicList.addAll(musicList);
//		Collections.sort(mMList, new ListComparator());
		if(mMusicList.size() == 0) {
			mPlayState = MPS_NOFILE;
			mCurPlayIndex = -1;
			return;
		}
//		sendBroadCast();
		///------------------------------------------------------------------
		switch(mPlayState) {
		case MPS_INVALID:
		case MPS_NOFILE:
		case MPS_PREPARE:
			prepare(0);
			break;
		}
	}
	/**
	 * 播放
	 * @param pos 集合中的位置
	 * @return
	 */
	private boolean prepare(int pos) {
		pos = pos>=mMusicList.size()?mMusicList.size()-1:pos;
		pos = pos<0?0:pos;//如果从是从0开始
		mCurPlayIndex = pos;
		mMediaPlayer.reset();
		String path = mMusicList.get(pos).data;
		try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
			mPlayState = MPS_PREPARE;
		} catch (Exception e) {
			LogUtils.e(TAG, "", e);
			mPlayState = MPS_INVALID;
			if(pos < mMusicList.size()) {
				pos++;
				playById(mMusicList.get(pos).songId);
			}
//			sendBroadCast();
			return false;
		}
		sendBroadCast();
		return true;
	}
	
	public void sendBroadCast() {
		mCurPlayIndex = mCurPlayIndex>=mMusicList.size()?mMusicList.size()-1:mCurPlayIndex;
		Intent intent = new Intent(BROADCAST_NAME);
		intent.putExtra(PLAY_STATE_NAME, mPlayState);
		intent.putExtra(PLAY_MUSIC_INDEX, mCurPlayIndex);
		intent.putExtra("music_num", mMusicList.size());
		if(mPlayState != MPS_NOFILE && mMusicList.size() > 0) {//如果不是没有文件了，列表大于0
			Bundle bundle = new Bundle();
//			if(mCurPlayIndex==mMusicList.size()){
//				System.out.println("+++++++++++++++");
//			}
			mCurMusic = mMusicList.get(mCurPlayIndex);
			mCurMusicId = mCurMusic.songId;
			bundle.putParcelable(MusicInfo.KEY_MUSIC, mCurMusic);
			intent.putExtra(MusicInfo.KEY_MUSIC, bundle);
		}
		mContext.sendBroadcast(intent);
	}
	
	public int getCurMusicId() {
		return mCurMusicId;
	}
	
	public MusicInfo getCurMusic() {
		return mCurMusic;
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
		return mPlayState;
	}
	
	public int getPlayMode() {
		return mPlayMode;
	}
	
	public void setPlayMode(int mode) {
		switch(mode) {
		case MPM_LIST_LOOP_PLAY:
		case MPM_ORDER_PLAY:
		case MPM_RANDOM_PLAY:
		case MPM_SINGLE_LOOP_PLAY:
			mPlayMode = mode;
			break;
		}
	}
	
	public List<MusicInfo> getMusicList() {
		return mMusicList;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		switch(mPlayMode) {
		case MPM_LIST_LOOP_PLAY:
			next();
			break;
		case MPM_ORDER_PLAY:
			if(mCurPlayIndex != mMusicList.size() - 1) {
				next();
			} else {
				prepare(mCurPlayIndex);
			}
			break;
		case MPM_RANDOM_PLAY:
			int index = getRandomIndex();
			if(index != -1) {
				mCurPlayIndex = index;
			} else {
				mCurPlayIndex = 0;
			}
			if(prepare(mCurPlayIndex)) {
				replay();
			}
			break;
		case MPM_SINGLE_LOOP_PLAY:
			play(mCurPlayIndex);
			break;
		}
	}
	
	private int getRandomIndex() {
		int size = mMusicList.size();
		if(size == 0) {
			return -1;
		}
		return Math.abs(mRandom.nextInt() % size);
	}
	/**
	 * 程序退出
	 */
	public void exitSendBroadcast() {
		pause();
		Intent intent  = new Intent(MainContentActivity.ALARM_CLOCK_BROADCAST);
		mContext.sendBroadcast(intent);
//		mMediaPlayer.stop();
//		mMediaPlayer.release();
//		mCurPlayIndex = -1;
//		mMusicList.clear();
//		
//		Intent intent = new Intent(BROADCAST_NAME);
//		intent.putExtra(PLAY_STATE_NAME, mPlayState);
//		intent.putExtra(PLAY_MUSIC_INDEX, mCurPlayIndex);
//		intent.putExtra("music_num", mMusicList.size());
//		if(mPlayState != MPS_NOFILE && mMusicList.size() > 0) {//如果不是没有文件了，列表大于0
//			Bundle bundle = new Bundle();
//			if(mCurPlayIndex==mMusicList.size()){
//				System.out.println("+++++++++++++++");
//			}
//			mCurMusic = mMusicList.get(mCurPlayIndex);
//			mCurMusicId = mCurMusic.songId;
//			bundle.putParcelable(MusicInfo.KEY_MUSIC, mCurMusic);
//			intent.putExtra(MusicInfo.KEY_MUSIC, bundle);
//		}
//		mContext.sendBroadcast(intent);
	}
	/**
	 * 当前播放顺序index 不管用
	 * @return
	 */
	public int getCurPlayIndex(){
//		if(mMusicList==null||mMusicList.size()<=0)return -1;
//		for (int i = 0; i < mMusicList.size(); i++) {
//			if(mCurMusic.songId == mMusicList.get(i).songId){
//				return i;
//			}
//		}
		return mCurPlayIndex;
	}
	public boolean removeById(int id) {
		MusicInfo music = null;
		//mCurPlayIndex = MusicUtils.seekPosInListById(mMusicList, mCurMusic.songId);//id);//获得当前的下标
		int index = MusicUtils.seekPosInListById(mMusicList, id);//id);//获得要删除的下标
		if(mCurPlayIndex==index){//如果移除是正在播放的
			if(mMediaPlayer.isPlaying()){
				next();
			}
		}
		mMusicList.remove(index);
		mCurPlayIndex = mCurPlayIndex>index?mCurPlayIndex-1:mCurPlayIndex;//如果移除的音乐播放的顺序在当前的前面，下标-1
		mPlayState = mMusicList.size()>0?mPlayState:MPS_NOFILE;//检察是否还有文件列表
		sendBroadCast();
		return music!=null;
	}
	public void refreshMusicList(List<MusicInfo> musicList, int selectedId) {
		mMusicList.clear();
		mMusicList.addAll(musicList);
		mCurPlayIndex = MusicUtils.seekPosInListById(mMusicList, selectedId);//id);//获得要删除的下标
		if(mMusicList.size() == 0) {
			mPlayState = MPS_NOFILE;
			mCurPlayIndex = -1;
			return;
		}
//		sendBroadCast();
		///------------------------------------------------------------------
		switch(mPlayState) {
		case MPS_INVALID:
		case MPS_NOFILE:
		case MPS_PREPARE:
			prepare(mCurPlayIndex);
			break;
		}
		
	}

}
