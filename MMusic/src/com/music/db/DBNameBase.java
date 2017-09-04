package com.music.db;


/**
 * @ClassName:     DBNameBase.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2017年9月4日 上午10:31:54 
 * @Description:   定义表名
 */
public interface DBNameBase{
	static final String TABLE_MUSIC = "music_info";
	static final String TABLE_ALBUM = "album_info";
	static final String TABLE_ARTIST = "artist_info";
	static final String TABLE_FOLDER = "folder_info";
	static final String TABLE_MUSIC_CACHE = "folder_info_cache";//临时表，保存排序后的结果后将改为music_info
	static final String TABLE_FAVORITE = "favorite_info";
	String songid="songid";
	String albumid="albumid";
	String duration="duration";
	String musicname="musicname";
//	String musicname_a="musicname_a";
//	String artist_a="artist_a";
	String artist="artist";
	String data="data";
	String folder="folder";
	String musicnamekey="musicnamekey";
	String artistkey="artistkey";
	String favorite="favorite";
	String _id="_id";

}
