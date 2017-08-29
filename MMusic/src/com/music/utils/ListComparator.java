package com.music.utils;

import java.util.Comparator;

import android.text.TextUtils;

import com.music.activity.IConstants;
import com.music.activity.IConstants.MusicType;
import com.music.model.BaseMusic;

/**
 * @ClassName:     ListComparator.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月25日 下午11:57:35 
 * @Description:  按名字第一个字符进行排序
 */
public class ListComparator implements Comparator<BaseMusic>{
	String first_l, first_r;
	private MusicType mFrom;
	public ListComparator(IConstants.MusicType mFrom){
		this.mFrom = mFrom;
	}
	@Override
	public int compare(BaseMusic lhs, BaseMusic rhs) {
		first_l = lhs.getTitle();
		first_r = rhs.getTitle();
		if(mFrom == MusicType.START_FROM_FOLDER){
			int length = first_l.length();
			int length2 = first_r.length();
			return length<length2?-1:1;
		}
//		switch (mFrom) {
//		case START_FROM_LOCAL:// 我的音乐
//			first_l = lhs.musicName;
//			first_r = rhs.musicName;
//			break;
//		case START_FROM_FAVORITE://我的最爱
//			first_l = lhs.musicName;
//			first_r = rhs.musicName;
//			break;
//		case START_FROM_FOLDER://文件夹
//			FolderInfo info = (FolderInfo) lhs;
//			FolderInfo info2 = (FolderInfo) rhs;
//			int length = info.folderPath.length();
//			int length2 = info2.folderPath.length();
//			return length<length2?-1:1;
//			//break;
//		case START_FROM_ARTIST://歌手
//			ArtistInfo artistInfo = (ArtistInfo) lhs;
//			ArtistInfo artistInfo2 = (ArtistInfo) rhs;
//			first_l = artistInfo.artist_name;
//			first_r = artistInfo2.artist_name;
//			break;
//		case START_FROM_ALBUM:// 专辑
//			AlbumInfo albumInfo = (AlbumInfo) lhs;
//			AlbumInfo albumInfo2 = (AlbumInfo) rhs;
//			first_l = albumInfo.album_name;
//			first_r = albumInfo2.album_name;
//			
//			break;
//		}
		
		if(TextUtils.isEmpty(first_l)||TextUtils.isEmpty(first_r)){
			return 0;
		}
		String pingYin = StringHelper.getPingYin(first_l);
		String pingYin2 = StringHelper.getPingYin(first_r);
		char ch = pingYin.charAt(0);
		char ch2 = pingYin2.charAt(0);
		return ch<ch2?-1:1;
	}


}
