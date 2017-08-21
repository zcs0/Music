/**
 * Copyright (c) www.longdw.com
 */
package com.music.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MainContentActivity;
import com.music.aidl.IMediaService;
import com.music.db.AlbumInfoDao;
import com.music.db.ArtistInfoDao;
import com.music.db.FavoriteInfoDao;
import com.music.db.FolderInfoDao;
import com.music.db.MusicInfoDao;
import com.music.interfaces.IOnServiceConnectComplete;
import com.music.model.BaseMusic;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.uimanager.MainBottomUIManager;
import com.music.uimanager.SlidingManagerFragment;
import com.music.uimanager.UIManager;
import com.music.uimanager.UIManager.OnRefreshListener;
import com.music.utils.ListComparator;
import com.music.utils.MusicTimer;
import com.music.utils.MusicUtils;
import com.music.utils.PhotoReadUtils;
import com.z.netUtil.ImageUtil.ImageLoader;

/**
 * 首页内容
 * 该类展示了软件的几大模块
 * 另外要注意嵌套的两层ViewPager
 * @author 
 *
 */
public class MainFragment extends BaseFragment implements IConstants,
		IOnServiceConnectComplete, OnRefreshListener, OnTouchListener {

	private GridView mGridView;
	private MyGridViewAdapter mAdapter;
	protected IMediaService mService;

	
	private MusicInfoDao mMusicDao;
	private ArtistInfoDao mArtistDao;
	private AlbumInfoDao mAlbumDao;
	private FavoriteInfoDao mFavoriteDao;
	public UIManager mUIManager;
	
	private MusicTimer mMusicTimer;
	private MusicPlayBroadcast mPlayBroadcast;
	private MainBottomUIManager mBottomUIManager;
	private SlidingManagerFragment mSdm;
	private View mBottomLayout, mMainLayout;
	private ServiceManager mServiceManager;
	private FolderInfoDao mFolderDao;
	private Bitmap defaultArtwork;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMusicDao = new MusicInfoDao(getActivity());
		mFolderDao = new FolderInfoDao(getActivity());
		mArtistDao = new ArtistInfoDao(getActivity());
		mAlbumDao = new AlbumInfoDao(getActivity());
		mFavoriteDao = new FavoriteInfoDao(getActivity());
		
	}
	/**
	 * 设置音乐的管理者
	 * @param mServiceManager
	 */
	public void setServiceManager(ServiceManager mServiceManager){
		this.mServiceManager = mServiceManager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mActivity = getActivity();
		imageLoad = new ImageLoader(getActivity());
		pPStorage = new SPStorage(getActivity());
		imageLoad.setCachePath(pPStorage.getHeadPath());
		imageLoad.setBoolCache(true);
		pPStorage.setHeadPath(pPStorage.getHeadPath());
		mView = inflater.inflate(R.layout.frame_main1, container, false);
		mGridView = (GridView) mView.findViewById(R.id.gv_view);
		mView.findViewById(R.id.btn_menu).setOnClickListener(this);
		mAdapter = new MyGridViewAdapter();
		mView.setOnTouchListener(this);
		mBottomLayout = mView.findViewById(R.id.rl_bottomLayout);//底部音乐控制

		MusicApp.mServiceManager.connectService();
		MusicApp.mServiceManager.setOnServiceConnectComplete(this);

		mGridView.setAdapter(mAdapter);
		
		mUIManager = new UIManager(getActivity(), mView,mServiceManager);//中间显示的管理
		mUIManager.setOnRefreshListener(this);
		
		mBottomUIManager = new MainBottomUIManager(getActivity(), mView);//底部播放管理
		mSdm = new SlidingManagerFragment(getActivity(), mServiceManager);//播放界面
		mMusicTimer = new MusicTimer(mSdm.mHandler, mBottomUIManager.mHandler);//播放界面，和底部刷新播放时间的监听
		mSdm.setMusicTimer(mMusicTimer);
		mPlayBroadcast = new MusicPlayBroadcast();//接收播放的广播
		
		//添加一个播放的广播监听
		IntentFilter filter = new IntentFilter(BROADCAST_NAME);
		filter.addAction(BROADCAST_NAME);
		getActivity().registerReceiver(mPlayBroadcast, filter);
		if(mServiceManager.getPlayState()==MPS_PLAYING){//如果进入时已经正在播放
			mMusicTimer.startTimer();
//			mServiceManager.rePlay();
			mServiceManager.sendBroadcast();
			mSdm.loadLyric(mServiceManager.getCurMusic());
		}
		FragmentTransaction beginTransaction = getActivity().getSupportFragmentManager().beginTransaction();
		beginTransaction.replace(R.id.rl_media_paly, mSdm).commit();
		hide(getActivity(), mSdm);
		mBottomLayout.setOnClickListener(new View.OnClickListener() {//显示播放
			@Override
			public void onClick(View arg0) {
				mView.findViewById(R.id.rl_media_paly).setVisibility(View.VISIBLE);
				FragmentTransaction beginTransaction2 = getActivity().getSupportFragmentManager().beginTransaction();
				beginTransaction2.setCustomAnimations(R.anim.push_bottom_in, R.anim.push_bottom_out,R.anim.push_bottom_in, R.anim.push_bottom_out);
				beginTransaction2.addToBackStack("");
				beginTransaction2.show(mSdm);
				beginTransaction2.commit();
				mSdm.startPhotoPlayer();
				
			}
		});
		int lastPlayerId = pPStorage.getLastPlayerId();//最后次的id
		MusicInfo oldMusic = (MusicInfo) mMusicDao.getMusicInfoBySongId(lastPlayerId+"");
		
//		mSdm.loadLyric(musicInfoBySongId);
		if(oldMusic!=null){//获得上次最播放的一首歌曲
			mSdm.refreshUI(0, oldMusic.duration, oldMusic);
			mBottomUIManager.refreshUI(0, oldMusic.duration, oldMusic);
		}
		/**
		 * 选择不同类型的音乐
		 */
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int from = -1;
				switch (position) {
				case 0:// 我的音乐
					from = START_FROM_LOCAL;
					break;
				case 1:// 我的最爱
					from = START_FROM_FAVORITE;
					break;
				case 2:// 文件夹
					from = START_FROM_FOLDER;
					break;
				case 3:// 歌手
					from = START_FROM_ARTIST;
					break;
				case 4:// 专辑
					from = START_FROM_ALBUM;
					break;
				}
				mUIManager.setContentType(from);//通知打开音乐类型
				
			}
		});
		defaultArtwork = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_album_background);
		return mView;
	}
	/**
	 * 主界面几个音乐分类
	 * @author ZCS
	 *
	 */
	private class MyGridViewAdapter extends BaseAdapter {

		private int[] drawable = new int[] { R.drawable.icon_local_music,
				R.drawable.icon_favorites, R.drawable.icon_folder_plus,
				R.drawable.icon_artist_plus, R.drawable.icon_album_plus };
		private String[] name = new String[] { "我的音乐", "我的最爱", "文件夹", "歌手",
				"专辑" };
		private int musicNum = 0, artistNum = 0, albumNum = 0, folderNum = 0, favoriteNum = 0;

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setNum(int music_num, int artist_num, int album_num,
				int folder_num, int favorite_num) {
			musicNum = music_num;
			artistNum = artist_num;
			albumNum = album_num;
			folderNum = folder_num;
			favoriteNum = favorite_num;
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.main_gridview_item, null);
				holder.iv = (ImageView) convertView
						.findViewById(R.id.gridview_item_iv);
				holder.nameTv = (TextView) convertView
						.findViewById(R.id.gridview_item_name);
				holder.numTv = (TextView) convertView
						.findViewById(R.id.gridview_item_num);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			switch (position) {
			case 0:// 我的音乐
				holder.numTv.setText(musicNum + "");
				break;
			case 1:// 我的最爱
				holder.numTv.setText(favoriteNum + "");
				break;
			case 2:// 文件夹
				holder.numTv.setText(folderNum + "");
				break;
			case 3:// 歌手
				holder.numTv.setText(artistNum + "");
				break;
			case 4:// 专辑
				holder.numTv.setText(albumNum + "");
				break;
			}
			holder.iv.setImageResource(drawable[position]);
			holder.nameTv.setText(name[position]);

			return convertView;
		}

		private class ViewHolder {
			ImageView iv;
			TextView nameTv, numTv;
		}
	}
	@Override
	public void onServiceConnectComplete(IMediaService service) {
		// service绑定成功会执行到这里
		refreshNum();
		
		
		int type = pPStorage.getLastPlayerListType();//最后次的type
		String info = pPStorage.getLastPlayerMusicInfo();//最后次的type
		if(type<=0)return;
		List<BaseMusic> queryMusic = new ArrayList<BaseMusic>();
		switch (type) {
		case START_FROM_LOCAL:// 我的音乐
			queryMusic = MusicUtils.queryMusic(getActivity(), type);
			break;
		case START_FROM_FAVORITE://我的最爱
			queryMusic = MusicUtils.queryFavorite(getActivity());
			break;
		case START_FROM_FOLDER://文件夹
			queryMusic = MusicUtils.queryMusic(getActivity(),"", info,START_FROM_FOLDER);
//			queryMusic = MusicUtils.queryFolder(getActivity());
			break;
		case START_FROM_ARTIST://歌手
			queryMusic = MusicUtils.queryMusic(getActivity(),"", info,START_FROM_ARTIST);
			break;
		case START_FROM_ALBUM:// 专辑
			queryMusic = MusicUtils.queryMusic(getActivity(),"", info + "",START_FROM_ALBUM);
			break;
		}
		
		List<MusicInfo> musicList = new ArrayList<MusicInfo>();
		for (BaseMusic baseMusic : queryMusic) {
			if(baseMusic instanceof MusicInfo){
				musicList.add((MusicInfo)baseMusic);
			}
		}
		
		if(musicList!=null&&musicList.size()>0){
			Collections.sort(musicList, new ListComparator());//排序后显示
			int lastPlayerId = pPStorage.getLastPlayerId();//最后次的id
			mServiceManager.refreshMusicList(musicList,lastPlayerId);
		}
		
	}
	/**
	 * 显示音乐分类的个数
	 */
	public void refreshNum() {
		int musicCount = mMusicDao.getDataCount();
		int artistCount = mArtistDao.getDataCount();
		int albumCount = mAlbumDao.getDataCount();
		int folderCount = mFolderDao.getDataCount();
		int favoriteCount = mFavoriteDao.getDataCount();
		
		mAdapter.setNum(musicCount, artistCount, albumCount, folderCount, favoriteCount);
	}
	MusicInfo music;
	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				music = new MusicInfo();
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);//播放到第几个
				Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
				if (bundle != null) {
					music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
				}
				pPStorage.setLastPlayerId(music.songId);//保存最新播放的歌曲id
				switch (playState) {
				case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);
					mBottomUIManager.refreshUI(0, music.duration, music);
					mBottomUIManager.showPlay(true);
					break;
				case MPS_PAUSE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(true);
					mBottomUIManager.refreshUI(mServiceManager.position(), music.duration,
							music);
					mBottomUIManager.showPlay(true);
					mSdm.stopPhotoPlayer();
//					mServiceManager.cancelNotification();//Notification弹出的消失
					break;
				case MPS_PLAYING:
					mMusicTimer.startTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(false);

					mBottomUIManager.refreshUI(mServiceManager.position(), music.duration,
							music);
					mBottomUIManager.showPlay(false);
					
					mSdm.startPhotoPlayer();
					break;
				case MPS_PREPARE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);
					mBottomUIManager.refreshUI(0, music.duration, music);
					mBottomUIManager.showPlay(true);
					mBottomUIManager.showImage(music);//小头像
					mSdm.loadLyric(music);// 读取歌词文件
					mSdm.setBackgroundImage(music,imageLoad);//背景为空
					new Thread(){//读取图片
						public void run() {
							List<String> photoList = PhotoReadUtils.getPath(pPStorage.getHeadPath(), music.musicName, music.artist);//所有符合的文件
							imageLoad.setCachePath(pPStorage.getHeadPath());//图片下载缓存到些路径
							if(photoList!=null&&photoList.size()>0){
								fileToUrl = PhotoReadUtils.fileToUrl(new File(photoList.get(0)));
								handler.sendEmptyMessage(music.songId);
							}
						};
					}.start();
					Bitmap bitmap = MusicUtils.getCachedArtwork(getActivity(),
							music.albumId, defaultArtwork);
					// 更新顶部notification
					mServiceManager.updateNotification(bitmap, music.musicName,
							music.artist);
					break;
				}
				
			}
		}
	}
	List<String> fileToUrl;
	private Handler handler = new Handler(){//显示歌手的图片
		public void handleMessage(Message msg) {
			int id = msg.what;
			MusicInfo curMusic = mServiceManager.getCurMusic();
			curMusic.headUrl = fileToUrl;
			if(id == curMusic.songId){
				mSdm.setBackgroundImage(curMusic,imageLoad);
			}
		};
	};
	@Override
	public void onRefresh() {
		refreshNum();
	}

	int oldY = 0;
	private View mView;
	private SPStorage pPStorage;
	private ImageLoader imageLoad;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
//		int bottomTop = mBottomLayout.getTop();
//		System.out.println(bottomTop);
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			oldY = (int) event.getY();
//			if (oldY > bottomTop) {
//				mSdm.open();
//			}
//		}
		return true;
	}
	/**
	 * 播放界面是否正在显示
	 * @return
	 */
	public boolean getIsPlayShow() {
		//mSdm.isAdded();
		return mSdm.isVisible();
	}
	/**
	 * 隐藏播放界面
	 */
	public void hidePlayUi() {
		//hide(getActivity(), mSdm);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_menu://侧边框
			((MainContentActivity)mActivity).mSlidingMenu.showMenu(true);
			break;

		default:
			break;
		}
	}
	/**
	 * 注销播放时的监听
	 */
	public void unPlayBroadcast(){
		getActivity().unregisterReceiver(mPlayBroadcast);
		mUIManager.unChangeBgReceiver();
	}
}
