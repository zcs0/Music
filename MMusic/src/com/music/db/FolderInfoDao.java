/**
 * Copyright (c) www.longdw.com
 */
package com.music.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.music.model.BaseMusic;
import com.music.model.FolderInfo;
public class FolderInfoDao extends DataBase{

	public FolderInfoDao(Context context) {
		this.mContext = context;
	}
	
	public void saveFolderInfo(List<BaseMusic> list) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		for (BaseMusic infos : list) {
			FolderInfo info =(FolderInfo) infos;
			ContentValues cv = new ContentValues();
			cv.put("folder_name", info.folderName);
			cv.put("folder_path", info.folderPath);
			db.insert(TABLE_FOLDER, null, cv);
		}
	}
	public List<BaseMusic> getFolderInfo() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		List<BaseMusic> list = new ArrayList<BaseMusic>();
		String sql = "select * from " + TABLE_FOLDER;
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()) {
			FolderInfo info = new FolderInfo();
			info.folderName = cursor.getString(cursor.getColumnIndex("folder_name"));
			info.folderPath = cursor.getString(cursor.getColumnIndex("folder_path"));
			list.add(info);
		}
		cursor.close();
		return list;
	}
	
	/**
	 * 数据库中是否有数据
	 * @return
	 */
	public boolean hasData() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_FOLDER;
		Cursor cursor = db.rawQuery(sql, null);
		boolean has = false;
		if(cursor.moveToFirst()) {
			int count = cursor.getInt(0);
			if(count > 0) {
				has = true;
			}
		}
		cursor.close();
		return has;
	}
	/**
	 * 文件夹列表
	 * @return
	 */
	public int getDataCount() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_FOLDER;
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		return count;
	}
	public void delete(int id){
		super.delete(TABLE_FOLDER, id);
	}
}
