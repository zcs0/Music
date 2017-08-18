package com.music.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

import com.music.R;

/**
 * @ClassName:     BaseFragment.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月5日 下午11:52:28 
 * @Description:  	Fragment的父类
 */
public class BaseFragment extends Fragment implements OnClickListener {
	
	protected FragmentActivity mActivity;
	protected void showFragment(Fragment fragment){
		this.mActivity = mActivity!=null?mActivity:getActivity();
		showFragment(mActivity, fragment, R.id.rl_file_list);
	}
	protected void showFragment(FragmentActivity activity,Fragment fragment,int ids) {//rl_file_list
		this.mActivity = activity;
		FragmentTransaction beginTransaction = activity.getSupportFragmentManager().beginTransaction();
		if(!fragment.isAdded()){//没有添加
			//beginTransaction.replace(ids, fragment);
			beginTransaction.add(ids, fragment);
		}else{
			beginTransaction.show(fragment);
		}
		beginTransaction.addToBackStack(null);
		beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE/*TRANSIT_FRAGMENT_OPEN*/);
		beginTransaction.commit();
	}
	protected void hide(FragmentActivity activity,Fragment fragment) {
		FragmentTransaction beginTransaction = activity.getSupportFragmentManager().beginTransaction();
		beginTransaction.hide(fragment);
		beginTransaction.commit();
	}
	protected void show(FragmentActivity activity,Fragment fragment) {
		FragmentTransaction beginTransaction = activity.getSupportFragmentManager().beginTransaction();
		beginTransaction.show(fragment);
		beginTransaction.commit();
	}
	/**
	 * 返回上一y
	 */
	public void backStack(){
		mActivity = mActivity!=null?mActivity:getActivity();
		mActivity.getSupportFragmentManager().popBackStack();
	}
	
	
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
				getViewById(i).setOnClickListener(this);
			}
		}
	}

	public <T extends View> T setOnClick(int layoutResId) {
		View view = getViewById(layoutResId);
		view.setOnClickListener(this);
		return (T) view;
	}

	public <T extends View> T getViewById(int layoutId) {
		return (T) getActivity().findViewById(layoutId);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
