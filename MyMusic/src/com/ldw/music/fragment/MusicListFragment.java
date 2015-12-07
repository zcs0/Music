package com.ldw.music.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.adapter.DataPagerAdapter;
import com.ldw.music.adapter.MyAdapter;
import com.ldw.music.model.AlbumInfo;
import com.ldw.music.model.ArtistInfo;
import com.ldw.music.model.FolderInfo;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.uimanager.MyMusicUIManager;
import com.ldw.music.uimanager.SlidingManagerFragment;
import com.ldw.music.uimanager.UIManager;
import com.ldw.music.utils.MusicTimer;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.viewpagerlistener.ViewPagerOnPageChangeListener;

/**
 * @ClassName:     MusicListFragment.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月7日 下午5:42:37 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class MusicListFragment extends BaseFragment implements IConstants{
	
	private int mFrom;
	private FragmentActivity mActivity;
	private UIManager mUIManager;
	private Object mObj;
	private View mView;
	private ViewPager mViewPager;
	private List<View> mListViews = new ArrayList<View>();
	private LayoutInflater mInflater;
	private MyAdapter mAdapter;
	private ServiceManager mServiceManager;
	private Bitmap defaultArtwork;
	private RelativeLayout mBottomLayout;
	private ListView mListView;
	private MusicPlayBroadcast mPlayBroadcast;
	private MyMusicUIManager mUIm;
	private SlidingManagerFragment mSdm;
	private MusicTimer mMusicTimer;
	private String TAG = "MusicListFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
		mServiceManager = MusicApp.mServiceManager;
		mInflater = LayoutInflater.from(activity);
		showFragment(activity,this, R.id.rl_file_list);
	}
	public View getView(int from) {
		return getView(from, mObj);
	}
	public View getView(int from, Object object) {
		View contentView = mInflater.inflate(R.layout.mymusic, null);
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
			Log.d(TAG , "", e);
		}
	}
		
	/**
	 * 列表展示
	 */
	private void initListView() {
		mAdapter = new MyAdapter(mActivity, mServiceManager, mSdm);
		mListView.setAdapter(mAdapter);
		//点击条目播放音乐
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mAdapter.refreshPlayingList();
				mServiceManager
						.playById(mAdapter.getData().get(position).songId);
			}
		});
		StringBuffer select = new StringBuffer();
		String selection = null;
		switch (mFrom) {
		case START_FROM_ARTIST://歌手
			ArtistInfo artistInfo = (ArtistInfo) mObj;
			selection = artistInfo.artist_name;
			break;
		case START_FROM_ALBUM://专辑
			AlbumInfo albumInfo = (AlbumInfo) mObj;
			selection = albumInfo.album_id + "";
			break;
		case START_FROM_FOLDER://文件夹
			FolderInfo folderInfo = (FolderInfo) mObj;
			selection = folderInfo.folder_path;
			break;
		case START_FROM_FAVORITE://我的最爱
			break;
		case START_FROM_LOCAL://我的音乐
			break;
		}
		mAdapter.setData(MusicUtils.queryMusic(mActivity, select.toString(), selection, mFrom));
		
		
	}
	/**
	 * 用来更新播放界面
	 * @author Administrator
	 *
	 */
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

}
