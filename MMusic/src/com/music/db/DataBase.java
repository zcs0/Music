package com.music.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.music.model.AlbumInfo;
import com.music.model.ArtistInfo;
import com.music.model.BaseMusic;

/**
 * @ClassName:     DataBase.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2017年8月31日 下午4:08:24 
 * @Description:   数据库操作的基类
 */
public class DataBase implements DBNameBase{
	protected Context mContext;
	
	public void delete(String tsble,int id){
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		db.delete(tsble, " _id=? ", new String[]{id+""});
	}
	/**
	 * 删除一个首歌
	 * @param context
	 * @param id
	 */
	public void delete(Context context,int id) {
		this.mContext = context;
		delete(TABLE_MUSIC, id);
		delete(TABLE_ALBUM, id);
		delete(TABLE_ARTIST, id);
		delete(TABLE_FOLDER, id);
		
	}
	/**
	 * 删除一个目录
	 * @param mContext
	 * @param baseMusic
	 */
	public void deleteFolder(Context mContext, BaseMusic baseMusic) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		db.delete(TABLE_FOLDER, " folder_path=? ", new String[]{baseMusic.folderPath});
		
	}
	/**
	 * 删除歌手
	 * @param mContext
	 * @param baseMusic
	 */
	public void deleteArtist(Context mContext, ArtistInfo baseMusic) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		db.delete(TABLE_ARTIST, " artist_name=? ", new String[]{baseMusic.artist_name});
	}
	/**
	 * 删除专辑
	 * @param mContext2
	 * @param info
	 */
	public void deleteAlbum(Context mContext2, AlbumInfo info) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		db.delete(TABLE_ALBUM, " artist_name=? ", new String[]{info.album_name});
	}
}
