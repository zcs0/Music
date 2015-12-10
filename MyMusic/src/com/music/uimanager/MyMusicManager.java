/**
 * Copyright (c) www.longdw.com
 */
package com.music.uimanager;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.adapter.DataPagerAdapter;
import com.music.adapter.MusicAdapter;
import com.music.fragment.BaseFragment;
import com.music.model.AlbumInfo;
import com.music.model.ArtistInfo;
import com.music.model.FolderInfo;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.utils.MusicTimer;
import com.music.utils.MusicUtils;
import com.music.viewpagerlistener.ViewPagerOnPageChangeListener;

/**
 * 音乐列表-文件列表
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MyMusicManager extends BaseFragment implements IConstants,
		OnTouchListener {
	private ViewPager mViewPager;
	private View mView;
	private LayoutInflater mInflater;
	private FragmentActivity mActivity;

	private String TAG = MyMusicManager.class.getSimpleName();
	private MusicAdapter mAdapter;
	private ListView mListView;
	private ServiceManager mServiceManager = null;
	private SlidingManagerFragment mSdm;
	private MyMusicUIManager mUIm;
	private MusicTimer mMusicTimer;
	private MusicPlayBroadcast mPlayBroadcast;

	private int mFrom;
	private Object mObj;

	private RelativeLayout mBottomLayout, mMainLayout;
	private Bitmap defaultArtwork;

	private UIManager mUIManager;
	public MyMusicManager(){}
	private List<View> mListViews = new ArrayList<View>();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = View.inflate(getActivity(), R.layout.vp_files_list, null);
		mViewPager = (ViewPager) findViewById(R.id.vp_file_list);
		mViewPager.setVisibility(View.VISIBLE);
		mListViews.add(new TextView(getActivity()));
		mListViews.add(getView(mFrom));
		mViewPager.setAdapter(new DataPagerAdapter(mListViews));
		mViewPager.setCurrentItem(1, true);
		mViewPager.setOnPageChangeListener(new ViewPagerOnPageChangeListener(mViewPager));
		return mView;
	}
	public View findViewById(int ids){
		return mView.findViewById(ids);
	}
	public void  show(FragmentActivity activity, UIManager manager,int from,Object obj) {
		this.mObj = obj;
		this.mFrom = from;
		this.mActivity = activity;
		this.mUIManager = manager;
		mInflater = LayoutInflater.from(activity);
		showFragment(activity,this, R.id.rl_file_list);
	}

	public View getView(int from) {
		return getView(from, mObj);
	}

	public View getView(int from, Object object) {
		View contentView = mInflater.inflate(R.layout.music_list, null);
		mObj = object;
		//initBg(contentView);
		initView(contentView);
		//返回上一页
		contentView.findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						backStack();
						
					}
				});
		return contentView;
	}

	private void initView(View view) {
		defaultArtwork = BitmapFactory.decodeResource(mActivity.getResources(),
				R.drawable.img_album_background);
		mServiceManager = MusicApp.mServiceManager;

		mBottomLayout = (RelativeLayout) view.findViewById(R.id.rl_bottomLayout);

		mListView = (ListView) view.findViewById(R.id.music_listview);

		mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter = new IntentFilter(BROADCAST_NAME);
		filter.addAction(BROADCAST_NAME);
		filter.addAction(BROADCAST_QUERY_COMPLETE_NAME);
		mActivity.registerReceiver(mPlayBroadcast, filter);

		mUIm = new MyMusicUIManager(mActivity, mServiceManager, view,
				mUIManager);
		mSdm = new SlidingManagerFragment(mActivity, mServiceManager);
		mMusicTimer = new MusicTimer(mSdm.mHandler, mUIm.mHandler);
		mSdm.setMusicTimer(mMusicTimer);

		initListView();

		initListViewStatus();
		
	}

	private void initBg(View view) {
		mMainLayout = (RelativeLayout) view
				.findViewById(R.id.main_mymusic_layout);
		mMainLayout.setOnTouchListener(this);
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
		if (bitmap != null) {
			mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		} else {
			mMainLayout.setBackgroundResource(R.drawable.bg);
		}
	}

	private void initListViewStatus() {
		try {
			mSdm.setListViewAdapter(mAdapter);
			int playState = mServiceManager.getPlayState();
			if (playState == MPS_NOFILE || playState == MPS_INVALID) {
				return;
			}
			if (playState == MPS_PLAYING) {
				mMusicTimer.startTimer();
			}
			List<MusicInfo> musicList = mAdapter.getData();
			int playingSongPosition = MusicUtils.seekPosInListById(musicList,
					mServiceManager.getCurMusicId());
			mAdapter.setPlayState(playState, playingSongPosition);
			MusicInfo music = mServiceManager.getCurMusic();
			mSdm.refreshUI(mServiceManager.position(), music.duration, music);
			mSdm.showPlay(false);
			mUIm.refreshUI(mServiceManager.position(), music.duration, music);
			mUIm.showPlay(false);

		} catch (Exception e) {
			Log.d(TAG, "", e);
		}
	}
	/**
	 * 列表展示
	 */
	private void initListView() {
		//mAdapter = new MusicAdapter(mActivity, mServiceManager, mSdm);
		mListView.setAdapter(mAdapter);
		//播放音乐
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//mAdapter.refreshPlayingList();
				mServiceManager
						.playById(mAdapter.getData().get(position).songId);
			}
		});
		StringBuffer select = new StringBuffer();
		switch (mFrom) {
		case START_FROM_ARTIST://歌手
			ArtistInfo artistInfo = (ArtistInfo) mObj;
			// select.append(" and " + Media.ARTIST + " = '"
			// + artistInfo.artist_name + "'");
			mAdapter.setData(MusicUtils.queryMusic(mActivity,
					select.toString(), artistInfo.artist_name,
					START_FROM_ARTIST));
			break;
		case START_FROM_ALBUM://专辑
			AlbumInfo albumInfo = (AlbumInfo) mObj;
			// select.append(" and " + Media.ALBUM_ID + " = "
			// + albumInfo.album_id);
			mAdapter.setData(MusicUtils.queryMusic(mActivity,
					select.toString(), albumInfo.album_id + "",
					START_FROM_ALBUM));
			break;
		case START_FROM_FOLDER://文件夹

			FolderInfo folderInfo = (FolderInfo) mObj;
			// select.append(" and " + Media.DATA + " like '"
			// + folderInfo.folder_path + File.separator + "%'");
			mAdapter.setData(MusicUtils.queryMusic(mActivity,
					select.toString(), folderInfo.folder_path,
					START_FROM_FOLDER));
			break;
		case START_FROM_FAVORITE://我的最爱
			mAdapter.setData(MusicUtils.queryFavorite(mActivity),
					START_FROM_FAVORITE);
			break;
		default:
			mAdapter.setData(MusicUtils.queryMusic(mActivity, START_FROM_LOCAL));//我的音乐
			break;
		}
	}

	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				MusicInfo music = new MusicInfo();
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
				Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
				if (bundle != null) {
					music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
				}
				mAdapter.setPlayState(playState, curPlayIndex);
				switch (playState) {
				case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);

					mUIm.refreshUI(0, music.duration, music);
					mUIm.showPlay(true);
					mServiceManager.next();
					break;
				case MPS_PAUSE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(true);

					mUIm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mUIm.showPlay(true);

					mServiceManager.cancelNotification();
					break;
				case MPS_PLAYING:
					mMusicTimer.startTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(false);

					mUIm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mUIm.showPlay(false);

					Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity,
							music.albumId, defaultArtwork);
					// Bitmap bitmap = MusicUtils.getArtwork(getActivity(),
					// music._id, music.albumId);
					// 更新顶部notification
					mServiceManager.updateNotification(bitmap, music.musicName,
							music.artist);

					break;
				case MPS_PREPARE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);

					mUIm.refreshUI(0, music.duration, music);
					mUIm.showPlay(true);

					// 读取歌词文件
					mSdm.loadLyric(music);
					break;
				}
			}
		}
	}

	int oldY = 0;
	

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int bottomTop = mBottomLayout.getTop();
		System.out.println(bottomTop);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			oldY = (int) event.getY();
			if (oldY > bottomTop) {
				//mSdm.open();
			}
		}
		return true;
	}

	protected void setBgByPath(String path) {
		Bitmap bitmap = mUIManager.getBitmapByPath(path);
		if (bitmap != null) {
			mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		}
	}


}
