package com.music.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * @ClassName:     MyPagerAdapter.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月6日 上午10:15:19 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class DataPagerAdapter extends PagerAdapter{

	private List<View> listViews;

	public DataPagerAdapter(List<View> views) {
		this.listViews = views;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(listViews.get(position));// 删除页卡
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {// 这个方法用来实例化页卡
		container.addView(listViews.get(position));// 添加页卡
		return listViews.get(position);
	}

	@Override
	public int getCount() {
		return listViews.size();// 返回页卡的数量
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;// 官方提示这样写
	}
}
