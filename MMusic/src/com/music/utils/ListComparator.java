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
		if(lhs==null||rhs==null)return 0;
		if(mFrom == MusicType.START_FROM_FOLDER){
			int length = lhs.folderPath.length();
			int length2 = rhs.folderPath.length();
			return length<length2?-1:1;
		}
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
