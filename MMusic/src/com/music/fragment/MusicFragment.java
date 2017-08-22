package com.music.fragment;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.music.R;

/**
 * @ClassName:     BaseFragment.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月5日 下午11:52:28 
 * @Description:  	Fragment的父类
 */
public abstract class MusicFragment extends BaseFragment {
	
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
	
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	public Bitmap getBitmapByPath(String path) {
		AssetManager am = getActivity().getAssets();
		Bitmap bitmap = null;
		try {
			InputStream is = am.open("bkgs/" + path);
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

}
