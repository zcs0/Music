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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.GridView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.MainContentActivity;
import com.music.adapter.MyGridViewAdapter;
import com.music.aidl.IMediaService;
import com.music.db.FolderInfoDao;
import com.music.interfaces.IOnServiceConnectComplete;
import com.music.model.BaseMusic;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.uimanager.MainBottomUIManager;
import com.music.uimanager.UIManager;
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
public class MainFragment extends MusicFragment implements IConstants,
		IOnServiceConnectComplete, OnTouchListener {

	private GridView mGridView;
	private MyGridViewAdapter mAdapter;
	protected IMediaService mService;
	
	public UIManager mUIManager;
	
	private MusicTimer mMusicTimer;
	private MusicPlayBroadcast mPlayBroadcast;
	private MainBottomUIManager mBottomUIManager;
	private SlidingManagerFragment mSdm;
	private View mBottomLayout, mMainLayout;
	private ServiceManager mServiceManager;
	private FolderInfoDao mFolderDao;
	private Bitmap defaultArtwork;
	int oldY = 0;
	private SPStorage pPStorage;
	private ImageLoader imageLoad;
	@Override
	public int createView() {
		return R.layout.frame_main;
	}
	@Override
	public void initView(Bundle bundle, View view) {
		mActivity = getActivity();
		imageLoad = new ImageLoader(getActivity());
		pPStorage = MusicApp.spSD;
		imageLoad.setCachePath(pPStorage.getHeadPath());
		imageLoad.setBoolCache(true);
		pPStorage.setHeadPath(pPStorage.getHeadPath());
		mGridView = (GridView) findViewById(R.id.gv_view);
		mServiceManager = MusicApp.mServiceManager;
		findViewById(R.id.btn_menu).setOnClickListener(this);
		mAdapter = new MyGridViewAdapter(this.getActivity());//歌曲分类
		view.setOnTouchListener(this);
		mBottomLayout = findViewById(R.id.rl_bottomLayout);//底部音乐控制
		
		MusicApp.mServiceManager.setOnServiceConnectComplete(this);//设置绑定播放服务的监听

		mGridView.setAdapter(mAdapter);
		
		mUIManager = new UIManager(getActivity(), view,mServiceManager);//中间显示的管理
		mBottomUIManager = new MainBottomUIManager(getActivity(), view);//底部播放管理
		mSdm = new SlidingManagerFragment(getActivity(), mServiceManager);//播放界面，显示歌词进度，人物图片
		
		//开始一个定时器，监听播放进度
		mMusicTimer = new MusicTimer(mSdm.getHandler(), mBottomUIManager.mHandler);//播放界面，和底部刷新播放时间的监听
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
				findViewById(R.id.rl_media_paly).setVisibility(View.VISIBLE);
				FragmentTransaction beginTransaction2 = getActivity().getSupportFragmentManager().beginTransaction();
				beginTransaction2.setCustomAnimations(R.anim.push_bottom_in, R.anim.push_bottom_out,R.anim.push_bottom_in, R.anim.push_bottom_out);
				beginTransaction2.addToBackStack("");
				beginTransaction2.show(mSdm);
				beginTransaction2.commit();
				mSdm.startPhotoPlayer();
			}
		});
		int lastPlayerId = pPStorage.getLastPlayerId();//最后次的id
		MusicInfo oldMusic = MusicUtils.getMusicInfoBySongId(getActivity(),lastPlayerId+"");
		if(oldMusic!=null){//获得上次最播放的一首歌曲
			mSdm.refreshUI(0, oldMusic.duration, oldMusic);
			mBottomUIManager.refreshUI(0, oldMusic.duration, oldMusic);
		}
		
		showSelectOption(mGridView);//进入选择的音乐类型界面
		defaultArtwork = BitmapFactory.decodeResource(getResources(),R.drawable.img_album_background);
	}
	
	/**
	 * 设置音乐的管理者
	 * @param mServiceManager
	 */
//	public void setServiceManager(ServiceManager mServiceManager){
//		this.mServiceManager = mServiceManager;
//	}

	/**
	 * 主界面几个音乐分类
	 * 选中一种类型后，进度界面
	 * @param mGridView
	 */
	private void showSelectOption(GridView mGridView) {
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MusicType from = MusicType.ALBUM_TO_MYMUSIC;
				switch (position) {
				case 0:// 我的音乐
					from = MusicType.START_FROM_LOCAL;
					break;
				case 1:// 我的最爱
					from = MusicType.START_FROM_FAVORITE;
					break;
				case 2:// 文件夹
					from = MusicType.START_FROM_FOLDER;
					break;
				case 3:// 歌手
					from = MusicType.START_FROM_ARTIST;
					break;
				case 4:// 专辑
					from = MusicType.START_FROM_ALBUM;
					break;
				}
				mUIManager.setContentType(from);//通知打开音乐类型
			}
		});
	}
	/**
	 * 
	 * @author ZCS
	 * 绑定播放服务的监听
	 */
	@Override
	public void onServiceConnectComplete(IMediaService service) {
		// service绑定成功会执行到这里
		refreshNum();
		int type = pPStorage.getLastPlayerListType();//最后次的type
		String info = pPStorage.getLastPlayerMusicInfo();//最后次的type
		if(type<=0){
			type = MusicType.START_FROM_LOCAL.getCode();
		}
		setPlayerList(MusicType.getType(type), info);//得到并设置最后一次播放时的列表
	}
	/**
	 * 设置播放列表
	 * @param type
	 * @param info
	 */
	private void setPlayerList(final MusicType type, final String info) {
		new AsyncTask<Void, Void, List<MusicInfo> >(){
			@Override
			protected List<MusicInfo>  doInBackground(Void... params) {
				List<BaseMusic> queryMusic = new ArrayList<BaseMusic>();
				switch (type) {
				case START_FROM_LOCAL:// 我的音乐
					queryMusic = MusicUtils.queryMusic(getActivity());
					break;
				case START_FROM_FAVORITE://我的最爱
					queryMusic = MusicUtils.queryFavorite(getActivity());
					break;
				case START_FROM_FOLDER://文件夹
					queryMusic = MusicUtils.queryFolder(mActivity);
					break;
				case START_FROM_ARTIST://歌手
					queryMusic = MusicUtils.queryArtist(mActivity);
					break;
				case START_FROM_ALBUM:// 专辑
					queryMusic = MusicUtils.queryAlbums(mActivity);
					break;
				}
				
				List<MusicInfo> musicList = new ArrayList<MusicInfo>();
				for (BaseMusic baseMusic : queryMusic) {
					if(baseMusic instanceof MusicInfo){
						musicList.add((MusicInfo)baseMusic);
					}
				}
				if(musicList!=null&&musicList.size()>0){//排序
					Collections.sort(musicList, new ListComparator(type));//按名字排序后显示
				}
				return musicList;
			}
			protected void onPostExecute(List<MusicInfo> result) {
				int lastPlayerId = MusicApp.spSD.getLastPlayerId();//最后次的id
				lastPlayerId = lastPlayerId<=0?0:lastPlayerId;
				if(result.size()>0)
					MusicApp.refreshMusicList(result,lastPlayerId);
			};
			
		}.execute();
	}
	/**
	 * 显示音乐分类的个数
	 */
	public void refreshNum() {
		new AsyncTask<Void, Void, Void>() {
			int musicCount;
			int artistCount;
			int folderCount;
			int albumCount;
			int favoriteCount;
			@Override
			protected Void doInBackground(Void... params) {
				musicCount = MusicUtils.getDataCount(getActivity(),MusicType.START_FROM_LOCAL);
				artistCount = MusicUtils.getDataCount(getActivity(),MusicType.START_FROM_ARTIST);
				albumCount = MusicUtils.getDataCount(getActivity(),MusicType.START_FROM_ALBUM);
				folderCount = MusicUtils.getDataCount(getActivity(),MusicType.START_FROM_FOLDER);
				favoriteCount = MusicUtils.getDataCount(getActivity(),MusicType.START_FROM_FAVORITE);
				return null;
			}
			protected void onPostExecute(Void result) {
				mAdapter.setNum(musicCount, artistCount, albumCount, folderCount, favoriteCount);
			}
		}.execute();
		
		
	}
	
	private class MusicPlayBroadcast extends BroadcastReceiver {
		MusicInfo music;//当前播放的音乐
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
				case MPS_PAUSE://暂停
					mMusicTimer.stopTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,music);
					mSdm.showPlay(false);
					mBottomUIManager.refreshUI(mServiceManager.position(), music.duration,music);
					mBottomUIManager.showPlay(false);
					mSdm.stopPhotoPlayer();
//					mServiceManager.cancelNotification();//Notification弹出的消失
					break;
				case MPS_PLAYING://播放中
					mMusicTimer.startTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,music);
					mSdm.showPlay(true);
					mBottomUIManager.refreshUI(mServiceManager.position(), music.duration,music);
					mBottomUIManager.showPlay(true);
					mSdm.startPhotoPlayer();
					break;
				case MPS_PREPARE://准备就绪
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(false);
					mBottomUIManager.refreshUI(0, music.duration, music);
					mBottomUIManager.showPlay(false);
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
					// 更新顶部notification
					break;
				}
				Bitmap bitmap = MusicUtils.getCachedArtwork(getActivity(),music.albumId, defaultArtwork);
				mServiceManager.updateNotification(bitmap, music.musicName,music.artist,playState);
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
