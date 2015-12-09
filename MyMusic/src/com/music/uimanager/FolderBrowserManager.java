/**
 * Copyright (c) www.longdw.com
 */
package com.music.uimanager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants;
import com.music.adapter.DataPagerAdapter;
import com.music.fragment.BaseFragment;
import com.music.model.BaseMusic;
import com.music.storage.SPStorage;
import com.music.utils.MusicUtils;
import com.music.viewpagerlistener.ViewPagerOnPageChangeListener;

/**
 * 文件夹列表
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class FolderBrowserManager extends BaseFragment implements IConstants,
		OnItemClickListener, OnClickListener {

	private Activity mActivity;
	private LayoutInflater mInflater;

	private ListView mListView;
	private MyAdapter mAdapter;
	private List<BaseMusic> list = new ArrayList<BaseMusic>();
	private ImageButton mBackBtn;

	private UIManager mUIManager;

	private RelativeLayout mFolderLayout;
	private View mView;
	private ViewPager mViewPager;
	private List<View> mListViews = new ArrayList<View>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = View.inflate(getActivity(), R.layout.vp_files_list, null);
		mViewPager = (ViewPager) findViewById(R.id.vp_file_list);
		mViewPager.setVisibility(View.VISIBLE);
		mListViews.add(new TextView(getActivity()));
		mListViews .add(getView());
		mViewPager.setAdapter(new DataPagerAdapter(mListViews));
		mViewPager.setCurrentItem(1, true);
		mViewPager.setOnPageChangeListener(new ViewPagerOnPageChangeListener(mViewPager));
		return mView;
	}
	public View findViewById(int ids){
		return mView.findViewById(ids);
	}
	public void  show(FragmentActivity activity, UIManager manager) {
		this.mActivity = activity;
		this.mUIManager = manager;
		mInflater = LayoutInflater.from(activity);
		showFragment(activity,this, R.id.rl_file_list);
	}
	public View getView() {
		View folderView = mInflater.inflate(R.layout.folderbrower, null);
		initBg(folderView);
		initView(folderView);
		folderView.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				backStack();
				
			}
		});
		return folderView;
	}

	private void initView(View view) {

		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mBackBtn.setOnClickListener(this);

		mListView = (ListView) view.findViewById(R.id.folder_listview);
		mListView.setOnItemClickListener(this);
		mAdapter = new MyAdapter();
		list = MusicUtils.queryFolder(mActivity);
		mListView.setAdapter(mAdapter);
	}

	private void initBg(View view) {
		mFolderLayout = (RelativeLayout) view
				.findViewById(R.id.main_folder_layout);
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
		if (bitmap != null) {
			mFolderLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		}
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public BaseMusic getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BaseMusic folder = getItem(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.folderbrower_listitem, null);
				viewHolder.folderNameTv = (TextView) convertView
						.findViewById(R.id.folder_name_tv);
				viewHolder.folderPathTv = (TextView) convertView
						.findViewById(R.id.folder_path_tv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.folderNameTv.setText(folder.folder_name);
			viewHolder.folderPathTv.setText(folder.folder_path);

			return convertView;
		}

		private class ViewHolder {
			TextView folderNameTv, folderPathTv;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mUIManager
				.setContentType(FOLDER_TO_MYMUSIC, mAdapter.getItem(position));
	}

	@Override
	public void onClick(View v) {
		//mUIManager.setCurrentItem(0);mUIManager.setCurrentItem(0);//返回列表的上一页
	}

	protected void setBgByPath(String path) {
		Bitmap bitmap = mUIManager.getBitmapByPath(path);
		if (bitmap != null) {
			mFolderLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		}
	}

	public View getView(int from) {
		return null;
	}

	public View getView(int from, Object obj) {
		return null;
	}

}
