/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.uimanager;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.adapter.DataPagerAdapter;
import com.ldw.music.fragment.BaseFragment;
import com.ldw.music.model.ArtistInfo;
import com.ldw.music.storage.SPStorage;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.viewpagerlistener.ViewPagerOnPageChangeListener;

/**
 * 歌手列表
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class ArtistBrowserManager extends BaseFragment implements OnClickListener,
OnItemClickListener, IConstants {
	
	private Activity mActivity;
	private UIManager mUIManager;
	private LayoutInflater mInflater;
	
	private ListView mListView;
	private ImageButton mBackBtn;
	private List<ArtistInfo> mArtistList;
	private MyAdapter mAdapter;
	
	private LinearLayout mArtistLayout;
	private View mView;
	private ViewPager mViewPager;
	private List<View> mListViews = new ArrayList<View>();
	public ArtistBrowserManager(){}
	public void show(FragmentActivity activity, UIManager manager) {
		this.mActivity = activity;
		this.mUIManager = manager;
		this.mInflater = LayoutInflater.from(activity);
		showFragment(activity,this, R.id.rl_file_list);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = mInflater.inflate(R.layout.vp_files_list, null);
		
		//mView = View.inflate(getActivity(), R.layout.vp_files_list, null);
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
	public View getView() {
		View view = mInflater.inflate(R.layout.artistbrower, null);
		initBg(view);
		initView(view);
		return view;
	}

	private void initView(View view) {
		mListView = (ListView) view.findViewById(R.id.artist_listview);
		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mBackBtn.setOnClickListener(this);

		mAdapter = new MyAdapter();
		mArtistList = MusicUtils.queryArtist(mActivity);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}
	
	private void initBg(View view) {
		mArtistLayout = (LinearLayout) view.findViewById(R.id.main_artist_layout);
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
		if(bitmap != null) {
			mArtistLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
		}
	}
	
	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mArtistList.size();
		}

		@Override
		public ArtistInfo getItem(int position) {
			return mArtistList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ArtistInfo artist = getItem(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.artistbrower_listitem, null);
				viewHolder.artistNameTv = (TextView) convertView
						.findViewById(R.id.artist_name_tv);
				viewHolder.numberTv = (TextView) convertView
						.findViewById(R.id.number_of_tracks_tv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.artistNameTv.setText(artist.artist_name);
			viewHolder.numberTv.setText(artist.number_of_tracks + "");

			return convertView;
		}

		private class ViewHolder {
			TextView artistNameTv, numberTv;
		}

	}

	@Override
	public void onClick(View v) {
		if (v == mBackBtn) {
			mUIManager.setCurrentItem();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mUIManager.setContentType(ARTIST_TO_MYMUSIC, mAdapter.getItem(position));
	}

	protected void setBgByPath(String path) {
		Bitmap bitmap = mUIManager.getBitmapByPath(path);
		if(bitmap != null) {
			mArtistLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
		}
	}
	
	public View getView(int from) {
		return null;
	}

	public View getView(int from, Object obj) {
		return null;
	}

}
