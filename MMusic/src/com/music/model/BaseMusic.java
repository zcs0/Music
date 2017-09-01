package com.music.model;

import java.util.List;

/**
 * @ClassName:     BaseMusic.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月8日 上午10:07:00 
 * @Description:   文件夹和文件的父类
 */
public abstract class BaseMusic {
	public static String KEY_FOLDER_NAME = "folder_name";
	public static String KEY_FOLDER_PATH = "folder_path";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_ALBUM_ID = "album_id";
	public static final String KEY_NUMBER_OF_SONGS = "number_of_songs";
	public static final String KEY_ALBUM_ART = "album_art";
	public static final String KEY_ARTIST_NAME = "artist_name";
	public static final String KEY_NUMBER_OF_TRACKS = "number_of_tracks";
	public int _id = -1;
	public String folderName;
	public String folderPath;
	public List<String> headUrl;
	/**
	 * 音乐名
	 */
	public String musicName;
	/**
	 * 显示标识
	 * @return
	 */
	public abstract String getTitle();
	
}
