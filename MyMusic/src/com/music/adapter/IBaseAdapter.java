package com.music.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.widget.BaseAdapter;

import com.music.model.BaseMusic;
import com.music.utils.StringHelper;

/**
 * @ClassName:     IBaseAdapter.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月8日 下午2:10:52 
 * @Description:   所有Adapter的父类
 */
public abstract class IBaseAdapter extends BaseAdapter {
	protected Context mContext;
	protected List<BaseMusic> mMList = new ArrayList<BaseMusic>();
	/**
	 * 当数据库中有数据的时候会调用该方法来更新列表
	 * 
	 * @param list
	 */
	public void setData(List<BaseMusic> list) {
		mMList = list;
		if(list==null||list.size()<=0)return;
		// 为list排序
		Collections.sort(mMList, comparator);
		notifyDataSetChanged();
	}
	Comparator<BaseMusic> comparator = new Comparator<BaseMusic>() {

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
	};
	public List<BaseMusic> getMList() {
		//Collections.sort(mMList, comparator);
		return mMList;
	}

}
