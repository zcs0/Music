package com.ldw.music.viewpagerlistener;

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

/**
 * @ClassName:     MyOnPageChangeListener.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月6日 上午10:17:27 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class ViewPagerOnPageChangeListener implements OnPageChangeListener{
	private ViewPager mViewPager;
	public ViewPagerOnPageChangeListener(ViewPager mViewPager){
		this.mViewPager = mViewPager;
	}
	int onPageScrolled = -1;

	// 当滑动状态改变时调用
	@Override
	public void onPageScrollStateChanged(int arg0) {
		System.out.println("onPageScrollStateChanged--->" + arg0);
//		if (arg0 == 0) {
//			mViewPager.removeAllViews();
//			mViewPager.setVisibility(View.GONE);
//		}
	}

	// 当当前页面被滑动时调用
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		onPageScrolled = arg0;
		// System.out.println("onPageScrolled--->" + "arg0=" + arg0 +
		// " arg1="
		// + arg1 + " arg2=" + arg2);
	}

	// 当新的页面被选中时调用
	@Override
	public void onPageSelected(int arg0) {
		if (arg0 == 0) {
			mViewPager.removeAllViews();
			mViewPager.setVisibility(View.GONE);
		}
		// System.out.println("onPageSelected--->" + arg0);
	}
}
