/**
 * Copyright (c) www.longdw.com
 */
package com.music.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.music.activity.IConstants;

@SuppressLint({ "WorldWriteableFiles", "CommitPrefEdits" })
public class SPStorage implements IConstants {
	
	private SharedPreferences mSp;
	private Editor mEditor;
	
	public SPStorage(Context context) {
		if(context!=null){
		mSp = context.getSharedPreferences(SP_NAME,
				Context.MODE_WORLD_WRITEABLE);
		mEditor = mSp.edit();
		}
	}
	
	/**
	 * 保存背景图片的地址
	 */
	public void savePath(String path) {
		mEditor.putString(SP_BG_PATH, path);
		mEditor.commit();
	}
	
	/**
	 * 获取背景图片的地址
	 * @return
	 */
	public String getPath() {
		return mSp.getString(SP_BG_PATH, null);
	}
	
	public void saveShake(boolean shake) {
		mEditor.putBoolean(SP_SHAKE_CHANGE_SONG, shake);
		mEditor.commit();
	}
	
	public boolean getShake() {
		return mSp.getBoolean(SP_SHAKE_CHANGE_SONG, false);
	}
	
	public void saveAutoLyric(boolean auto) {
		mEditor.putBoolean(SP_AUTO_DOWNLOAD_LYRIC, auto);
		mEditor.commit();
	}
	
	public boolean getAutoLyric() {
		return mSp.getBoolean(SP_AUTO_DOWNLOAD_LYRIC, false);
	}
	
	public void saveFilterSize(boolean size) {
		mEditor.putBoolean(SP_FILTER_SIZE, size);
		mEditor.commit();
	}
	
	public boolean getFilterSize() {
		return mSp.getBoolean(SP_FILTER_SIZE, false);
	}
	
	public void saveFilterTime(boolean time) {
		mEditor.putBoolean(SP_FILTER_TIME, time);
		mEditor.commit();
	}
	
	public boolean getFilterTime() {
		return mSp.getBoolean(SP_FILTER_TIME, false);
	}
	
	public int getLastPlayerListType(){
		return mSp.getInt(LAST_PLAYER_TYPE, -1);
	} 
	public int getLastPlayerId(){
		return mSp.getInt(LAST_PLAYER_ID, -1);
	}
	public void setLastPlayerListType(int type){
		mEditor.putInt(LAST_PLAYER_TYPE, type);
		mEditor.commit();
	} 
	public void setLastPlayerId(int id){
		mEditor.putInt(LAST_PLAYER_ID, id);
		mEditor.commit();
	}
	public void setLastPlayerMusicInfo(String info){
		mEditor.putString(LAST_PLAYER_INFO, info);
		mEditor.commit();
	}
	public String getLastPlayerMusicInfo(){
		return mSp.getString(LAST_PLAYER_INFO, null);
	}
	/**
	 * 歌词路径
	 * @return
	 */
	public String getUserLyricPath(){
		String path = mSp.getString(LYRIC_SAVE_PATH, null);
		if(TextUtils.isEmpty(path)){
			path =mSp.getString(LYRIC_DEFAULE_PATH, ALBUM_HEAD_PATH_CACHE);
		}
		return path;
	}
	/**
	 * user选择歌词保存路径
	 * @param path
	 * @return
	 */
	public String setUserLyricPath(String path){
		mSp.edit().putString(LYRIC_SAVE_PATH, path).commit();
		return path;
	}
	/**
	 * 下是否可以联网
	 * @return
	 */
	public boolean getFilterWifi() {
		return mSp.getBoolean(FILTER_WIFI, false);
	}
	/**
	 * wifi 下是否可以联网
	 * @param isOk true：可以联网
	 * @return
	 */
	public void setFilterWifi(boolean isOk) {
		mSp.edit().putBoolean(FILTER_WIFI, isOk).commit();
	}
	/**
	 * 人物头像路径，天天json位置
	 * @return
	 */
	public String getHeadPath() {
		return mSp.getString(ALBUM_HEAD_PATH, ALBUM_HEAD_PATH_CACHE);
	}
	/**
	 * 人物头像路径，天天json位置
	 * @param path
	 */
	public void setHeadPath(String path) {
		mSp.edit().putString(ALBUM_HEAD_PATH, path).commit();
	}

}
