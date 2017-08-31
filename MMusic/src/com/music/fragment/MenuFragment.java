/**
 * Copyright (c) www.longdw.com
 */
package com.music.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MainContentActivity;
import com.music.activity.MenuBackgroundActivity;
import com.music.activity.MenuScanActivity;
import com.music.activity.MenuSettingActivity;
import com.music.service.ServiceManager;
import com.music.slidemenu.SlidingMenu.OnOpenedListener;

/**
 * 侧滑Menu
 * 该类提供了软件的设置，歌曲的控制等几大功能
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MenuFragment extends MusicFragment implements OnClickListener,
		IConstants, OnOpenedListener {

	private TextView mMediaCountTv;
	private TextView mScanTv, mPlayModeTv, mBackgroundTv, mSleepTv, mSettingTv,
			mExitTv;
	private MainContentActivity mMainActivity;

	private int mCurMode;
	private ServiceManager mServiceManager;
	private static final String modeName[] = { "列表循环", "顺序播放", "随机播放", "单曲循环" };
	private int modeDrawable[] = { R.drawable.icon_list_reapeat,
			R.drawable.icon_sequence, R.drawable.icon_shuffle,
			R.drawable.icon_single_repeat };

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}

	private void initView(View view) {
		mMediaCountTv = (TextView) view.findViewById(R.id.txt_media_count);
		mScanTv = (TextView) view.findViewById(R.id.txt_scan);
		mPlayModeTv = (TextView) view.findViewById(R.id.txt_play_mode);
		mBackgroundTv = (TextView) view.findViewById(R.id.txt_background);
		mSleepTv = (TextView) view.findViewById(R.id.txt_sleep);
		mSettingTv = (TextView) view.findViewById(R.id.preference_text);
		mExitTv = (TextView) view.findViewById(R.id.txt_exit);

		mMediaCountTv.setOnClickListener(this);
		mScanTv.setOnClickListener(this);
		mPlayModeTv.setOnClickListener(this);
		mBackgroundTv.setOnClickListener(this);
		mSleepTv.setOnClickListener(this);
		mSettingTv.setOnClickListener(this);
		mExitTv.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_scan://扫描歌曲
			startActivityForResult(new Intent(getActivity(), MenuScanActivity.class), 1);
			break;
		case R.id.txt_play_mode:
			changeMode();
			break;
		case R.id.txt_background://背景
			startActivity(new Intent(getActivity(), MenuBackgroundActivity.class));
			break;
		case R.id.preference_text://设置
			startActivity(new Intent(getActivity(), MenuSettingActivity.class));
			break;
		case R.id.txt_sleep://睡眠
			mMainActivity.showSleepDialog();
			break;
		case R.id.txt_exit:
			getActivity().finish();
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1) {
			//刷新主页内容
			((MainContentActivity)getActivity()).mMainFragment.refreshNum();
		}
	}

	private void changeMode() {
		mCurMode++;
		if (mCurMode > MPM_SINGLE_LOOP_PLAY) {
			mCurMode = MPM_LIST_LOOP_PLAY;
		}
		mServiceManager.setPlayMode(mCurMode);
		initPlayMode();
	}

	private void initPlayMode() {
		mPlayModeTv.setText(modeName[mCurMode]);
		Drawable drawable = getResources().getDrawable(modeDrawable[mCurMode]);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		mPlayModeTv.setCompoundDrawables(drawable, null, null, null);
	}

	@Override
	public void onOpened() {
		mCurMode = mServiceManager.getPlayMode();
		initPlayMode();
	}

	@Override
	public int createView() {
		// TODO Auto-generated method stub
		return R.layout.frame_menu1;
	}

	@Override
	public void initView(Bundle bundle, View view) {
		mMainActivity = (MainContentActivity) getActivity();
		mMainActivity.mSlidingMenu.setOnOpenedListener(this);
		initView(view);
		mServiceManager = MusicApp.mServiceManager;
		
	}
}
