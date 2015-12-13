package com.music.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.adapter.AlbumBrowserAdapter;
import com.music.adapter.ArtistBrowserAdapter;
import com.music.adapter.DataPagerAdapter;
import com.music.adapter.FolderBrowserAdapter;
import com.music.adapter.IBaseAdapter;
import com.music.adapter.MusicAdapter;
import com.music.model.AlbumInfo;
import com.music.model.ArtistInfo;
import com.music.model.BaseMusic;
import com.music.model.FolderInfo;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.uimanager.MusicUIManager;
import com.music.uimanager.SlidingManagerFragment;
import com.music.uimanager.UIManager;
import com.music.utils.MusicTimer;
import com.music.utils.MusicUtils;
import com.music.viewpagerlistener.ViewPagerOnPageChangeListener;

/**
 * @ClassName: MusicListFragment.java
 * @author zcs
 * @version V1.0
 * @Date 2015年12月7日 下午5:42:37
 * @Description: 用来展示的
 */
public class MusicListFragment extends BaseFragment implements IConstants {

	private int mFrom=-1;
	private UIManager mUIManager;
	private BaseMusic mBaseMusic;
	private View mView;
	private ViewPager mViewPager;
	private List<View> mListViews = new ArrayList<View>();
	private LayoutInflater mInflater;
	private MusicAdapter mAdapter;
	private ServiceManager mServiceManager;
	private Bitmap defaultArtwork;
	private RelativeLayout mBottomLayout;
	private ListView mListView;
	private MusicPlayBroadcast mPlayBroadcast;
	private MusicUIManager mUIm;
	private SlidingManagerFragment mSdm;
	private MusicTimer mMusicTimer;
	private String TAG = "MusicListFragment";
	private IBaseAdapter musicAdapter;
	private BaseMusic baseMusic;
	private View contentView;
	private ListView listView;
	/**
	 * 保存从数据库中查询的结果
	 */
	private List<BaseMusic> queryMusic;
	private TextView tv_title;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mActivity = getActivity();
		mView = View.inflate(getActivity(), R.layout.fragment_list_item, null);
		mListViews.add(new TextView(mActivity));
		mListViews.add(createView());
		mViewPager = findViewById(R.id.vp_file_list);
		mViewPager.setAdapter(new DataPagerAdapter(mListViews));
		mViewPager.setCurrentItem(1, true);
		mViewPager.setOnPageChangeListener(new ViewPagerOnPageChangeListener(
				mViewPager));
		initView();//初始化控件
		createAdapter();//创建Adapter
		initListViewStatus();
		return mView;
	}
	
	/**
	 * 创建Adapter
	 */
	private void createAdapter() {
		if(mFrom<=0||listView==null)return;
		switch (mFrom) {
		case START_FROM_LOCAL:// 我的音乐
			tv_title.setText("音乐列表");
			queryMusic = MusicUtils.queryMusic(mActivity, mFrom);
			musicAdapter = new MusicAdapter(mActivity, mServiceManager, queryMusic);
			break;
		case START_FROM_FAVORITE://我的最爱
			tv_title.setText("我的最爱");
			queryMusic = MusicUtils.queryFavorite(mActivity);
			MusicAdapter adapter = new MusicAdapter(mActivity, mServiceManager, queryMusic);
			adapter.setData(queryMusic,START_FROM_FAVORITE);
			musicAdapter = adapter;
			break;
		case START_FROM_FOLDER://文件夹
			tv_title.setText("文件夹");
			queryMusic = MusicUtils.queryFolder(mActivity);
			musicAdapter = new FolderBrowserAdapter(mActivity, mServiceManager, queryMusic);
			break;
		case START_FROM_ARTIST://歌手
			tv_title.setText("歌手分类");
			queryMusic = MusicUtils.queryArtist(mActivity);
			musicAdapter = new ArtistBrowserAdapter(mActivity, mServiceManager, queryMusic);
			break;
		case START_FROM_ALBUM:// 专辑
			tv_title.setText("专辑分类");
			queryMusic = MusicUtils.queryAlbums(mActivity);
			musicAdapter = new AlbumBrowserAdapter(mActivity, mServiceManager, queryMusic);
			break;
		}
		if(musicAdapter!=null&&listView!=null){
			listView.setAdapter(musicAdapter);
		}
	}
	/**
	 * 查找控件
	 * @param ids
	 * @return
	 */
	private <T extends View> T findViewById(int ids) {
		View view = mView.findViewById(ids);
		if(view==null&&contentView!=null){
			view = contentView.findViewById(ids);
		}
		return (T) view;
	}
	
	private View createView(){
		contentView = contentView!=null?contentView:View.inflate(mActivity, R.layout.music_list, null);
		return contentView;
	}
	private void initView(){
		listView = findViewById(R.id.music_list_view);
		tv_title = findViewById(R.id.tv_title);
		int playState = mServiceManager.getPlayState();
		MusicInfo music = mServiceManager.getCurMusic();
		if (playState == MPS_NOFILE || playState == MPS_INVALID) {
			//return;
		}
		
		//返回上一页
		findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backStack();
			}
		});
		//点击条目时的事件
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				List<BaseMusic> mList = musicAdapter.getMList();
				BaseMusic baseMusic = mList.get(position);
				//BaseMusic baseMusic = queryMusic.get(position);
				if(baseMusic instanceof MusicInfo){//如果是一个可播放的文件
					ArrayList<MusicInfo> mMusicList  = new ArrayList<MusicInfo>();
					for (BaseMusic list : mList) {
						mMusicList.add((MusicInfo)list);
					}
					mServiceManager.refreshMusicList(mMusicList);
					mServiceManager.playById(((MusicInfo)baseMusic).songId);
				}else{
					List<BaseMusic> queryMusic=null;
					if(baseMusic instanceof FolderInfo){//如果是个文件夹信息
						queryMusic = MusicUtils.queryMusic(mActivity,"", baseMusic.folder_path,START_FROM_FOLDER);
						System.out.println(baseMusic.folder_path);
					}else if(baseMusic instanceof ArtistInfo){//歌手
						queryMusic = MusicUtils.queryMusic(mActivity,"", ((ArtistInfo)baseMusic).artist_name,START_FROM_ARTIST);
						System.out.println(((ArtistInfo)baseMusic).artist_name);
					}else if(baseMusic instanceof AlbumInfo){// 专辑
						queryMusic = MusicUtils.queryMusic(mActivity,"", ((AlbumInfo)baseMusic).album_id + "",START_FROM_ALBUM);
						System.out.println(((AlbumInfo)baseMusic).album_id);
					}
					if(queryMusic!=null&&queryMusic.size()>0){
						musicAdapter = new MusicAdapter(mActivity, mServiceManager, queryMusic);
						listView.setAdapter(musicAdapter);
					}
				}
			}
		});
	}
	

	/**
	 * 
	 * @param activity
	 * @param manager
	 * @param from
	 * @param data
	 */
	public void show(FragmentActivity activity, UIManager manager, int from,
			BaseMusic data) {
		this.mBaseMusic = data;
		this.mFrom = from;
		this.mActivity = activity;
		this.mUIManager = manager;
		mServiceManager = MusicApp.mServiceManager;
		mInflater = LayoutInflater.from(activity);
		showFragment(activity, this, R.id.rl_file_list);
	}
	/**
	 * 设置播放状态
	 */
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
			List<MusicInfo> musicList = mAdapter.getmMusicList();
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
	 * 用来更新播放界面
	 * 
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
	/**
	 * 设置展示的数据
	 * @param baseMusic
	 */
	public void setBaseMusic(int type,BaseMusic baseMusic) {
		this.mFrom = type;
		this.baseMusic = baseMusic;
		
	}
	/**
	 * 设置音乐播放管理者
	 * @param mServiceManager2
	 */
	public void setServiceManager(ServiceManager sm) {
		this.mServiceManager=sm;
		
	}

	

}
