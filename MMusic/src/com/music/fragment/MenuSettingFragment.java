/**
 * Copyright (c) www.longdw.com
 */
package com.music.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MenuSettingActivity;
import com.music.dialog.FileExplorerDialog;
import com.music.storage.SPStorage;

/**
 * 设置
 * 
 * @author
 *
 */
public class MenuSettingFragment extends MusicFragment implements
		OnClickListener, IConstants {

	private LinearLayout mAdviceLayout, mAboutLayout;
	private CheckedTextView mChangeSongTv, mAutoLyricTv, mFilterSizeTv,
			mFilterTimeTv, mFilterWifi;
	private SPStorage mSp;

	private ImageButton mBackBtn;
	private View lrcPath;
	private View headImg;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			showLrcDialog(msg.what);
			if(pdialog!=null)pdialog.dismiss();
		};
	};


	private void initView(View view) {
		mAboutLayout = (LinearLayout) view
				.findViewById(R.id.setting_about_layout);
		mAdviceLayout = (LinearLayout) view
				.findViewById(R.id.setting_advice_layout);
		mAboutLayout.setOnClickListener(this);
		mAdviceLayout.setOnClickListener(this);

		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mBackBtn.setOnClickListener(this);

		mChangeSongTv = (CheckedTextView) view
				.findViewById(R.id.shake_change_song);
		mAutoLyricTv = (CheckedTextView) view
				.findViewById(R.id.auto_download_lyric);
		mFilterSizeTv = (CheckedTextView) view.findViewById(R.id.filter_size);
		mFilterTimeTv = (CheckedTextView) view.findViewById(R.id.filter_time);
		mFilterWifi = (CheckedTextView) view.findViewById(R.id.filter_wifi);
		lrcPath = view.findViewById(R.id.lrc_layout);
		headImg = view.findViewById(R.id.head_img_path);
		tvPath = (TextView) view.findViewById(R.id.tv_lrc_path);
		tvHead = (TextView) view.findViewById(R.id.tv_head_path);

		mChangeSongTv.setChecked(mSp.getShake());
		mAutoLyricTv.setChecked(mSp.getAutoLyric());
		mFilterSizeTv.setChecked(mSp.getFilterSize());
		mFilterTimeTv.setChecked(mSp.getFilterTime());
		mFilterWifi.setChecked(mSp.getFilterWifi());

		mChangeSongTv.setOnClickListener(this);
		mAutoLyricTv.setOnClickListener(this);
		mFilterSizeTv.setOnClickListener(this);
		mFilterTimeTv.setOnClickListener(this);
		mFilterWifi.setOnClickListener(this);
		lrcPath.setOnClickListener(this);
		headImg.setOnClickListener(this);

		tvPath.setText(mSp.getUserLyricPath());
		tvHead.setText(mSp.getHeadPath());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_about_layout:
			break;
		case R.id.setting_advice_layout:
			break;
		case R.id.shake_change_song:
			mChangeSongTv.toggle();
			mSp.saveShake(mChangeSongTv.isChecked());
			Intent intent = new Intent(BROADCAST_SHAKE);
			intent.putExtra(SHAKE_ON_OFF, mChangeSongTv.isChecked());
			getActivity().sendBroadcast(intent);
			break;
		case R.id.auto_download_lyric:
			mAutoLyricTv.toggle();
			mSp.saveAutoLyric(mAutoLyricTv.isChecked());
			break;
		case R.id.filter_size:
			mFilterSizeTv.toggle();
			mSp.saveFilterSize(mFilterSizeTv.isChecked());
			break;
		case R.id.filter_time:
			mFilterTimeTv.toggle();
			mSp.saveFilterTime(mFilterTimeTv.isChecked());
			break;
		case R.id.filter_wifi:
			mFilterWifi.toggle();
			mSp.saveFilterTime(mFilterWifi.isChecked());
			break;
		case R.id.backBtn:
			((MenuSettingActivity) getActivity()).mViewPager.setCurrentItem(0,
					true);
			break;
		case R.id.lrc_layout://歌词保存路径
			pdialog = new ProgressDialog(getActivity());
			pdialog.show();
			new Thread(){
				public void run() {
					readFile(100);
				};
				//showLrcDialog(100);
			}.start();
			break;
		case R.id.head_img_path://图片保存路径
			pdialog = new ProgressDialog(getActivity());
			pdialog.show();
			new Thread(){
				public void run() {
					readFile(101);
				};
				//showLrcDialog(100);
			}.start();
			break;
		}
	}
	
	public void savePath(int type,int position){
		String path = fileList.get(position);
		switch (type) {
		case 100:
			mSp.setUserLyricPath(fileMain+path);
			break;
		case 101:
			mSp.setHeadPath(fileMain+path);
			break;
		}
		tvPath.setText(mSp.getUserLyricPath());
		tvHead.setText(mSp.getHeadPath());
	}

	/**
	 * 读取文件
	 * @param type
	 */
	private void readFile(int type){
		if(fileList==null||fileList.size()<=0){
			fileList = getPath(new File(fileMain));
			List<String> listStr = new ArrayList<String>();
			for (String string : fileList) {
				if(string.length()>fileMain.length()+1){
					String substring = string.substring(fileMain.length());
					if(!substring.startsWith(".")){
						listStr.add(substring);
					}
				}else{
					listStr.add(string);
				}
				
			}
			fileList = listStr;
		}
		handler.sendEmptyMessage(type);
	}
	String fileMain = Environment.getExternalStorageDirectory()+"/";
	
	
	
	/**
	 * 搜索的弹出框
	 */
	private void showLrcDialog(final int type) {
		WindowManager wm = mActivity.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
//		if(fileList!=null)fileList.clear();
		
		final FileExplorerDialog dialog = new FileExplorerDialog(getActivity(), R.style.lrc_dialog);
		dialog.setFileList(fileList);
		dialog.show();
		dialog.setOnItemClickListener(new FileExplorerDialog.ItemClickListener() {
			
			@Override
			public void click(View view, int position, long id) {
				savePath(type,position);
				dialog.dismiss();
			}
		});
	}

	private List<String> fileList ;
	private TextView tvPath;
	private TextView tvHead;
	private ProgressDialog pdialog;
	/**
	 * 目录下的文件目录
	 * @param file
	 * @return
	 */
	private List<String> getPath(File file){
		fileList = fileList!=null?fileList:new ArrayList<String>();
		//Environment.getExternalStorageDirectory();
		if(file.isDirectory()){
			fileList.add(file.getPath());
			File[] list = file.listFiles();
			for (File files : list) {
				if(files.getPath().matches(".+/[01](/[^/]+){0,3}")){//0|1目录下的最多三层
					getPath(files);
				}
			}
		}
		return fileList;
	}

	@Override
	public int createView() {
		// TODO Auto-generated method stub
		return R.layout.menu_setting_fragment;
	}

	@Override
	public void initView(Bundle bundle, View view) {
		mActivity = getActivity();
		mSp = new SPStorage(getActivity());
		initView(view);
	}
	
	
	
	
}
