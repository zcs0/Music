package com.music.utils;

import java.util.Comparator;

import com.music.model.BaseMusic;

/**
 * @ClassName:     ListComparator.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月25日 下午11:57:35 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class ListComparator implements Comparator<BaseMusic>{
	char first_l, first_r;
	@Override
	public int compare(BaseMusic lhs, BaseMusic rhs) {
		first_l = lhs.musicName.charAt(0);
		first_r = rhs.musicName.charAt(0);
		if (StringHelper.checkType(first_l) == StringHelper.CharType.CHINESE) {
			first_l = StringHelper.getPinyinFirstLetter(first_l);
		}
		if (StringHelper.checkType(first_r) == StringHelper.CharType.CHINESE) {
			first_r = StringHelper.getPinyinFirstLetter(first_r);
		}
		if (first_l > first_r) {
			return 1;
		} else if (first_l < first_r) {
			return -1;
		} else {
			return 0;
		}
	}


}
