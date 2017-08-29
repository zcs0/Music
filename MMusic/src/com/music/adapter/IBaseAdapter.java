package com.music.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.BaseAdapter;

import com.music.activity.IConstants;
import com.music.model.BaseMusic;

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
	protected IConstants.MusicType mFrom;
	/**
	 * 当数据库中有数据的时候会调用该方法来更新列表
	 * 
	 * @param list
	 */
	public void setData(List<BaseMusic> list) {
		mMList = list;
//		if(list==null||list.size()<=0)return;
		// 为list排序
//		Collections.sort(mMList, new ListComparator());
		notifyDataSetChanged();
	}
	public void setData(List<BaseMusic> list,IConstants.MusicType from) {
		this.mFrom = from;
		mMList = list;
//		if(list==null||list.size()<=0)return;
		// 为list排序
//		Collections.sort(mMList, new ListComparator());
		notifyDataSetChanged();
	}
	
	public List<BaseMusic> getMList() {
		//Collections.sort(mMList, comparator);
		return mMList;
	}

}
