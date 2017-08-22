/**
 * Copyright (c) www.longdw.com
 */
package com.music.uimanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MainContentActivity;
import com.music.fragment.MusicListFragment;
import com.music.model.BaseMusic;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;

/**
 * 动态生成view并通过viewPager来显示
 * 
 * @author longdw
 * 
 */
public class UIManager implements IConstants {


	private FragmentActivity mActivity;
	private View mView;
	private LayoutInflater mInflater;
	/** mViewPager为第一层 mViewPagerSub为第二层（例如从文件夹或歌手进入列表，点击列表会进入第二层） */
	private ViewPager  mViewPagerSub;
	//private ViewPager mViewPager;
	private List<View> mListViews, mListViewsSub;

	private MainContentActivity mMainActivity;
	
	private View mMainLayout;
	private ChangeBgReceiver mReceiver;
	private MainUIManager mMainUIManager;
	private ServiceManager mServiceManager;
	/**
	 * UI界面管理
	 * @param activity
	 * @param view 主界面
	 * @param mServiceManager 音乐播放管理
	 */
	public UIManager(FragmentActivity activity, View view,ServiceManager sm) {
		this.mActivity = activity;
		this.mView = view;
		this.mServiceManager = sm;
		mMainActivity = (MainContentActivity) activity;
		this.mInflater = LayoutInflater.from(activity);
		initBroadCast();//广播
		initBg();//背景
		//init();//初始化
	}
	/**
	 * 设置进入时的类型
	 * @param type
	 */
	public void setContentType(MusicType type) {
		// 此处可以根据传递过来的view和type分开来处理
		setContentType(type, null);
	}

	private View findViewById(int id) {
		return mView.findViewById(id);
	}
	/**
	 * 显示中间和标头区域
	 * @param type
	 * @param baseMusic
	 */
	public void setContentType(MusicType type, BaseMusic baseMusic) {
		mListViews = new ArrayList<View>();
		//显示中间和标题区域
		findViewById(R.id.rl_file_list).setVisibility(View.VISIBLE);
		FragmentTransaction beginTransaction = mActivity.getSupportFragmentManager().beginTransaction();
		MusicListFragment musicList = new MusicListFragment();
		musicList.setServiceManager(mServiceManager);//设置音乐播放管理者
		musicList.setBaseMusic(type,baseMusic);//设置要显示的数据
		beginTransaction.setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out,R.anim.push_right_in, R.anim.push_right_out);
		beginTransaction.replace(R.id.rl_file_list, musicList);
		beginTransaction.addToBackStack("");
		beginTransaction.commit();
		
	}
	private void initBroadCast() {
		mReceiver = new ChangeBgReceiver();
		IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);//背景底图
		mActivity.registerReceiver(mReceiver, filter);
	}
	/**
	 * 获得图片背景
	 */
	private void initBg() {
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		mMainLayout = findViewById(R.id.main_layout);
		Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
		if(bitmap != null) {
			mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
		}
		
		//如果第一次进来 SharedPreference中没有数据
		if(TextUtils.isEmpty(mDefaultBgPath)) {
			mSp.savePath("004.jpg");
		}
	}
	/**
	 * 修改背景底图的监听
	 * @author zcs
	 *
	 */
	private class ChangeBgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String path = intent.getStringExtra("path");
			if(TextUtils.isEmpty(path))return;
			Bitmap bitmap = getBitmapByPath(path);
			if(bitmap != null) {
				mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
			}
			if(mMainUIManager != null) {
				mMainUIManager.setBgByPath(path);
			}
		}
	}
	public Bitmap getBitmapByPath(String path) {
		AssetManager am = mActivity.getAssets();
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
	public void unChangeBgReceiver(){
		mActivity.unregisterReceiver(mReceiver);
	}

}
