/**
 * Copyright (c) www.longdw.com
 */
package com.music.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.music.activity.IConstants;
import com.music.model.BaseMusic;
import com.music.model.MusicInfo;
import com.music.utils.ListComparator;
import com.z.utils.JSONUtils;
import com.z.utils.LogUtils;
/**
 * 数据库操作
 * @author zcs
 *
 */
public class MusicInfoDao extends DataBase implements IConstants {
	private String TAG="MusicInfoDao";

	/**
	 *  数据库
	 * @param context
	 */
	public MusicInfoDao(Context context) {
		this.mContext = context;
	}
	/**
	 * 保存列表
	 * @param list
	 */
	public synchronized void saveMusicInfo(List<BaseMusic> list) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		try {
			String json = JSONUtils.toJson(list);
			LogUtils.d(TAG, "保存音乐列表->"+json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		db.beginTransaction();
		for (BaseMusic musics : list) {
			MusicInfo music = (MusicInfo) musics;
//			if(isEmpty(music.data,db))continue;//如果已存在
			ContentValues cv = new ContentValues();
			cv.put(songid, music.songId);
			cv.put(albumid, music.albumId);
			cv.put(duration, music.duration);
			cv.put(musicname, music.musicName);
			cv.put(artist, music.artist);
			cv.put(data,music.data);
			cv.put(folder, music.folder);
			cv.put(musicnamekey, music.musicNameKey);
			cv.put(artistkey, music.artistKey);
			cv.put(favorite, music.favorite);
			cv.put(favorite, music.favorite);
			db.insert(TABLE_MUSIC, null, cv);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	public List<BaseMusic> getMusicInfo() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select * from " + TABLE_MUSIC;
		return parseCursor(db.rawQuery(sql, null));
	}
	
	private List<BaseMusic> parseCursor(Cursor cursor) {
		List<BaseMusic> list = new ArrayList<BaseMusic>();
		while(cursor.moveToNext()) {
			MusicInfo music = new MusicInfo();
			music._id = cursor.getInt(cursor.getColumnIndex(_id));
			music.songId = cursor.getInt(cursor.getColumnIndex(songid));
			music.albumId = cursor.getInt(cursor.getColumnIndex(albumid));
			music.duration = cursor.getInt(cursor.getColumnIndex(duration));
			music.musicName = cursor.getString(cursor.getColumnIndex(musicname));
			music.artist = cursor.getString(cursor.getColumnIndex(artist));
			music.data = cursor.getString(cursor.getColumnIndex(data));
			music.folder = cursor.getString(cursor.getColumnIndex(folder));
			music.musicNameKey = cursor.getString(cursor.getColumnIndex(musicnamekey));
			music.artistKey = cursor.getString(cursor.getColumnIndex(artistkey));
			music.favorite = cursor.getInt(cursor.getColumnIndex(favorite));
//			music.musicname_a = cursor.getString(cursor.getColumnIndex(musicname_a));
//			music.artist_a = cursor.getString(cursor.getColumnIndex(artist_a));
			list.add(music);
		}
		cursor.close();
		return list;
	}
	public BaseMusic getMusicInfoBySongId(String songId){
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "";
			sql = "select * from " + TABLE_MUSIC + " where songid = ? order by musicname ";
		List<BaseMusic> parseCursor = parseCursor(db.rawQuery(sql, new String[]{ songId }));
		if(parseCursor!=null&&parseCursor.size()>0){
			return parseCursor.get(0);
		}
		return null;
	}
	/**
	 * 数据库中此文件路径下的音乐
	 * @param path
	 * @return
	 */
	public List<BaseMusic> getMusicListByPath(String path) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select * from " + TABLE_MUSIC + " where folder = ? order by musicname ";
		return parseCursor(db.rawQuery(sql, new String[]{ path }));
	}
	/**
	 * 暂时没用上
	 * @param selection
	 * @param type
	 * @return
	 */
	public List<BaseMusic> getMusicInfoByType(String selection, MusicType type) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "";
		switch (type) {
		case START_FROM_ARTIST://歌手
			sql = "select * from " + TABLE_MUSIC + " where artist = ? ;//order by musicname ";
			break;
		case START_FROM_ALBUM://专辑
			sql = "select * from " + TABLE_MUSIC + " where albumid = ? order by musicname ";
			break;
		case START_FROM_FOLDER://文件夹 
			sql = "select * from " + TABLE_MUSIC + " where folder = ? order by musicname ";
			break;
		}
		return parseCursor(db.rawQuery(sql, new String[]{ selection }));
	}
	/**
	 * 设置为收藏
	 * @param id
	 * @param favorite
	 */
	public void setFavoriteStateById(int id, int favorite) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "update " + TABLE_MUSIC + " set favorite = " + favorite + " where _id = " + id;
		db.execSQL(sql);
	}
	
	/**
	 * 数据库中是否有数据
	 * @return
	 */
	public boolean hasData() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_MUSIC;
		Cursor cursor = db.rawQuery(sql, null);
		boolean has = false;
		if(cursor.moveToFirst()) {
			int count = cursor.getInt(0);
			if(count > 0) {
				has = true;
			}
			LogUtils.d(TAG, "检察数据库->"+count);
		}
		cursor.close();
		return has;
	}
	/**
	 * 数据库是否已存在
	 * @param folder 文件路径
	 * @return true:存在
	 */
	private boolean isEmpty(String folder,SQLiteDatabase db){
		String sql = "select count(*) from " + TABLE_MUSIC + " where folder = "+"'"+folder+"'";
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		return count>0;
	}
	/**
	 * 歌曲列表
	 * @return
	 */
	public int getMusicCount() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_MUSIC;
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		return count;
	}

	public List<BaseMusic> getFavoriteList() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select * from " + TABLE_MUSIC +" where favorite = 1";
		return parseCursor(db.rawQuery(sql, null));
	}
	/**
	 * 保存列表
	 * @param list
	 */
	public void saveMusicSort(List<BaseMusic> list) {
		Collections.sort(list, new ListComparator(IConstants.MusicType.START_FROM_LOCAL));
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		db.beginTransaction();
		for (BaseMusic musics : list) {
			MusicInfo music = (MusicInfo) musics;
			if(isEmpty(music.data,db))continue;//如果已存在
			ContentValues cv = new ContentValues();
			cv.put(songid, music.songId);
			cv.put(albumid, music.albumId);
			cv.put(duration, music.duration);
			cv.put(musicname, music.musicName);
			cv.put(artist, music.artist);
			cv.put(data,music.data);
			cv.put(folder, music.folder);
			cv.put(musicnamekey, music.musicNameKey);
			cv.put(artistkey, music.artistKey);
			cv.put(favorite, music.favorite);
//			cv.put(musicname_a, converterToPinYin(music.musicName));
//			cv.put(artist_a, converterToPinYin(music.artist));
			db.insert(TABLE_MUSIC_CACHE, null, cv);
		}
		db.execSQL("DROP TABLE "+TABLE_MUSIC);//删除原始表
		db.execSQL("ALTER TABLE "+TABLE_MUSIC_CACHE+" RENAME TO "+TABLE_MUSIC);//修改排序后的表为基础表music_info
		db.setTransactionSuccessful();
		db.endTransaction();
		//ALTER TABLE "Student" RENAME TO "_Student_old_20140409";
		//INSERT INTO "Student" ("Id", "Name") SELECT "Id", "Title" FROM "_Student_old_20140409";
	}
	private String converterToPinYin(String chinese) {
		String pinyinString = "";
		char[] charArray = chinese.toCharArray();
		// 根据需要定制输出格式，我用默认的即可
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		try {
			// 遍历数组，ASC码大于128进行转换
			for (int i = 0; i < charArray.length; i++) {
				if (charArray[i] > 128) {
					// charAt(0)取出首字母
					if (charArray[i] >= 0x4e00 && charArray[i] <= 0x9fa5) { // 判断是否中文
						pinyinString += PinyinHelper.toHanyuPinyinStringArray(
								charArray[i], defaultFormat)[0].charAt(0);
					} else { // 不是中文的打上未知，所以无法处理韩文日本等等其他文字
						pinyinString += "?";
					}
				} else {
					pinyinString += charArray[i];
				}
			}
			return pinyinString;
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 统计收藏个数
	 * @return
	 */
	public int getCountFavorite() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_MUSIC +" where favorite = 1";
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		return count;
	}
	
	public void delete(int id){
		super.delete(TABLE_MUSIC, id);
	}

}
