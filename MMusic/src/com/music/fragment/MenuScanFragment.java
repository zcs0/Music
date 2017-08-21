/**
 * Copyright (c) www.longdw.com
 */
package com.music.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MenuScanActivity;
import com.music.db.DatabaseHelper;
import com.music.utils.MusicUtils;

/**
 * 
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MenuScanFragment extends BaseFragment implements IConstants, OnClickListener {

	private Button mScanBtn;
	private ImageButton mBackBtn;
	private Handler mHandler;
	private DatabaseHelper mHelper;
	private ProgressDialog mProgress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new DatabaseHelper(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.menu_scan_fragment, container,
				false);
		mScanBtn = (Button) view.findViewById(R.id.scanBtn);
		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mScanBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mProgress.dismiss();
				((MenuScanActivity)getActivity()).mViewPager.setCurrentItem(0, true);
			}
		};

		return view;
	}

	private void getData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				mHelper.deleteTables(getActivity());
				MusicUtils.queryMusic(getActivity(), MusicType.START_FROM_LOCAL);
				MusicUtils.queryAlbums(getActivity());
				MusicUtils.queryArtist(getActivity());
				MusicUtils.queryFolder(getActivity());
				mHandler.sendEmptyMessage(1);
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		if(v == mScanBtn) {
			mProgress = new ProgressDialog(getActivity());
			mProgress.setMessage("正在扫描歌曲，请勿退出软件！");
			mProgress.setCancelable(false);
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.show();
			getData();
		} else if(v == mBackBtn) {
			((MenuScanActivity)getActivity()).mViewPager.setCurrentItem(0, true);
		}
	}
}
