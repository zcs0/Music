/**
 * Copyright (c) www.longdw.com
 */
package com.music.activity;

import android.os.Environment;

/**
 * 常量
 * @author 
 *
 */
public interface IConstants {
	
	public static final String BROADCAST_NAME = "com.music.broadcast";
	public static final String SERVICE_NAME = "com.music.service.MediaService";//蓝牙监听
	public static final String BROADCAST_QUERY_COMPLETE_NAME = "com.music.querycomplete.broadcast";
	public static final String BROADCAST_CHANGEBG = "com.music.changebg";//背景底图
	public static final String BROADCAST_SHAKE = "com.music.shake";
	public static final String PAUSE_BROADCAST_NAME = "com.music.pause.broadcast";//暂停
	public static final String NEXT_BROADCAST_NAME = "com.music.next.broadcast";//下一首
	public static final String PRE_BROADCAST_NAME = "com.music.pre.broadcast";//上一首
	public static final String EXIT_BROADCAST_NAME = "com.music.ex.broadcast";//退出程序
	
	//是否开启了振动模式
	public static final String SHAKE_ON_OFF = "SHAKE_ON_OFF";
	
	public static final String SP_NAME = "com.music_preference";
	public static final String SP_BG_PATH = "bg_path";//图片保存路径
	public static final String SP_SHAKE_CHANGE_SONG = "shake_change_song";
	public static final String SP_AUTO_DOWNLOAD_LYRIC = "auto_download_lyric";//是否自动下载歌词
	public static final String SP_FILTER_SIZE = "filter_size";//过滤大小
	public static final String SP_FILTER_TIME = "filter_time";//过滤时间
	public static final String LAST_PLAYER_TYPE = "last_player_type";//上次播放时的列表类型
	public static final String LAST_PLAYER_ID = "last_player_id";//上次播放时ID
	public static final String LAST_PLAYER_INFO = "last_player_info";//上次播放的info根据它来查询信息
	public static final String LYRIC_DEFAULE_PATH = "lyric_defaule_path";//歌词默认保存路径
	public static final String LYRIC_SAVE_PATH = "lyric_save_path";//用户所指定路径
	public static final String FILTER_WIFI = "filter_wifi";//wifi是否可联网
	public static final String ALBUM_HEAD_PATH = "album_head_path";//人物的头像
	public static final String ALBUM_HEAD_PATH_CACHE = Environment.getExternalStorageDirectory()+"/lrc";//图片缓存默认路径
	
	public final static int REFRESH_PROGRESS_EVENT = 0x100;

	// 播放状态
	public static final int MPS_NOFILE = -1; // 无音乐文件
	public static final int MPS_INVALID = 0; // 当前音乐文件无效
	public static final int MPS_PREPARE = 1; // 准备就绪
	public static final int MPS_PLAYING = 2; // 播放中
	public static final int MPS_PAUSE = 3; // 暂停

	// 播放模式
	/**列表循环**/
	public static final int MPM_LIST_LOOP_PLAY = 0; // 列表循环
	/**顺序播放**/
	public static final int MPM_ORDER_PLAY = 1; // 顺序播放
	/** 随机播放**/
	public static final int MPM_RANDOM_PLAY = 2; // 随机播放
	/** 单曲循环**/
	public static final int MPM_SINGLE_LOOP_PLAY = 3; // 单曲循环
	
	public static final String PLAY_STATE_NAME = "PLAY_STATE_NAME";
	public static final String PLAY_MUSIC_INDEX = "PLAY_MUSIC_INDEX";//播放到第几个
	
	//歌手和专辑列表点击都会进入MMMusic 此时要传递参数表明是从哪里进入的
	public enum MusicType{
		/** 歌手 */
		START_FROM_ARTIST(1,"歌手分类"),
		/** 专辑 */
		START_FROM_ALBUM (2,"专辑分类"),
		/** 我的音乐	 */
		START_FROM_LOCAL(3,"音乐列表"),
		/** 文件夹 */
		START_FROM_FOLDER(4,"文件夹"),
		/** 我的最爱 */
		START_FROM_FAVORITE(5,"我的收藏"),
		
		FOLDER_TO_MYMUSIC(6),
		ALBUM_TO_MYMUSIC(7),
		ARTIST_TO_MYMUSIC(8),
		MENU_BACKGROUND(9);
		
		private int value;
		private String title;

		MusicType(int value){
			this.value= value;
		}
		MusicType(int value,String str){
			this.value= value;
			this.title = str;
		}
		public int getValue(){
			return value;
		}
		public String getTitle(){
			return title==null?"":title;
		}
		
		public static MusicType getType(int index){
			switch (index) {
			case 1:
				return MusicType.START_FROM_ARTIST;
			case 2:
				return MusicType.START_FROM_ALBUM;
			case 3:
				return MusicType.START_FROM_LOCAL;
			case 4:
				return MusicType.START_FROM_FOLDER;
			case 5:
				return MusicType.START_FROM_FAVORITE;
			case 6:
				return MusicType.FOLDER_TO_MYMUSIC;
			case 7:
				return MusicType.ALBUM_TO_MYMUSIC;
			case 8:
				return MusicType.ARTIST_TO_MYMUSIC;
			case 9:
				return MusicType.MENU_BACKGROUND;
			default:
				return MusicType.START_FROM_ARTIST;
			}
		}
		
	}
	public static final String FROM = "from";
	
	
}
