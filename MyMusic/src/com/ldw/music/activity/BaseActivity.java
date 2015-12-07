package com.ldw.music.activity;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @ClassName:     BaseActivity.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月5日 下午11:48:58 
 * @Description:   所在Activity的父类
 */
public class BaseActivity extends FragmentActivity implements OnClickListener {
	/**
	 * 设置点击事件
	 * 
	 * @param view
	 */
	public <T extends View> T setOnClick(View view) {
		if (view != null) {
			view.setOnClickListener(this);
		}
		return (T) view;
	}

	/**
	 * 设置多个控件点击事件
	 * 
	 * @param ids
	 *            []
	 */
	protected void setOnClick(int... ids) {
		int[] id = ids;
		if (id != null && id.length > 0) {
			for (int i : id) {
				getView(i).setOnClickListener(this);
			}
		}
	}

	public <T extends View> T setOnClick(int layoutResId) {
		View view = getView(layoutResId);
		view.setOnClickListener(this);
		return (T) view;
	}

	public <T extends View> T getView(int layoutId) {
		return (T) findViewById(layoutId);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
