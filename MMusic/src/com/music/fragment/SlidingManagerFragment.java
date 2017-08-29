/**
 * Copyright (c) www.longdw.com
 */
package com.music.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants;
import com.music.activity.PlayQueueActivity;
import com.music.adapter.MusicAdapter;
import com.music.db.FavoriteInfoDao;
import com.music.db.MusicInfoDao;
import com.music.lrc.LyricPlayerManager;
import com.music.model.LyricSentence;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.utils.MusicTimer;
import com.music.utils.TimerTasks;
import com.music.view.ScrollDrawerLayout;
import com.z.netUtil.ImageUtil.ImageLoader;

/**
 * 播放界面
 * 
 * @author 
 *
 */
@SuppressLint({ "HandlerLeak", "ValidFragment" })
public class SlidingManagerFragment extends MusicFragment implements OnClickListener,
		 IConstants, OnDrawerOpenListener,
		OnDrawerCloseListener {
	private int photoIndex = 0;//音乐图片播放的下标
	private MusicInfo mCurrentMusicInfo;//当前音乐
	private ImageLoader imageLoad;//图片加载工
	final int layout_view = R.layout.media_player;//真正的歌词显示区域
	private View mSliding;
	private TextView mMusicNameTv, mArtistTv, mCurTimeTv, mTotalTimeTv;
	private ImageButton mPrevBtn, mNextBtn, mVolumeBtn,
			mFavoriteBtn;
	/**
	 * 歌词显示
	 */
	private LinearLayout mVolumeLayout;
	private Activity mActivity;
	private ScrollDrawerLayout mView;
	private ServiceManager mServiceManager;
	private SeekBar  mVolumeSeekBar;//播放进度条，声音大小控制
	private boolean mPlayAuto = true;
	/**
	 * 歌词
	 */
	List<LyricSentence> lyricList;
	
	private AudioManager mAudioManager;
	private int mMaxVolume;//最大音量
	private int mCurVolume;//当前音量

	private Animation view_in, view_out;
	private GridView mGridView;
	private ImageButton mShowMoreBtn;

	private ImageView mMoveIv;
	private boolean mIsFavorite = false;
	private FavoriteInfoDao mFavoriteDao;
	private MusicInfoDao mMusicDao;
	
	private boolean mListNeedRefresh = false;
	private MusicAdapter mAdapter;;
	private MusicTimer mMusicTimer;
	private int mProgress;
//	private LyricAdapter mLyricAdapter;
	private int mScreenWidth;

	private SPStorage mSp;
	/** 歌词是否正在下载 */
	private boolean mIsLyricDownloading;
	protected String TAG="SlidingManagerFragment";
	private Handler mHandler;
	private LyricPlayerManager lyricManager;
	private ImageView mheadImg;
	private ImageButton playerAndPause;
	public SlidingManagerFragment(){}
	/**
	 * 再次进入时需调用
	 */
	public void showView(boolean show){
		if(show&&mView!=null){
			mView.moveByLocation(0,0);//移动到顶部
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		if(lyricManager!=null){
			lyricManager.onResume();
		}
	}
	/**
	 * 播放界面
	 * 
	 * @param a
	 * @param sm
	 * @param view
	 */
	public SlidingManagerFragment(FragmentActivity activity, ServiceManager sm) {
		this.mServiceManager = sm;
		this.mActivity = activity;
		mSp = new SPStorage(mActivity);
		mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量
		mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
		
	}

	private void initView() {
		mView.setOnScrollListener(scrollListener);
		// 歌词秀设置---------------------------------------------------------------
//		mLyricAdapter = new LyricAdapter(mActivity);
		
//		mListView = (ListView) findViewById(R.id.music_listview);
		mGridView = (GridView) findViewById(R.id.gv_view);
		mSliding = (View) findViewById(R.id.md_media_player);
		mMusicNameTv = (TextView) findViewById(R.id.musicname_tv);
		mArtistTv = (TextView) findViewById(R.id.artist_tv);
		mPrevBtn = (ImageButton) findViewById(R.id.btn_playPre);
		mNextBtn = (ImageButton) findViewById(R.id.btn_playNext);
		playerAndPause = (ImageButton) findViewById(R.id.btn_player_and_pause);
		mVolumeBtn = (ImageButton) findViewById(R.id.btn_volume);
		mShowMoreBtn = (ImageButton) findViewById(R.id.btn_more);
		mFavoriteBtn = (ImageButton) findViewById(R.id.btn_favorite);
		mMoveIv = (ImageView) findViewById(R.id.move_iv);
		mVolumeSeekBar = (SeekBar) findViewById(R.id.volume_seekbar);
		mheadImg = (ImageView) findViewById(R.id.ll_head_img);
		mPrevBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		playerAndPause.setOnClickListener(this);
		mVolumeBtn.setOnClickListener(this);
		mShowMoreBtn.setOnClickListener(this);
		mFavoriteBtn.setOnClickListener(this);
		mVolumeSeekBar.setMax(200);
		float cur = mCurVolume/(mMaxVolume+0.0f)*200;
		mVolumeSeekBar.setProgress((int) cur);
		mVolumeSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);//控制声音大小

		mCurTimeTv = (TextView) findViewById(R.id.currentTime_tv);
		mTotalTimeTv = (TextView) findViewById(R.id.totalTime_tv);

		mVolumeLayout = (LinearLayout) findViewById(R.id.volumeLayout);
	}
	
	/**
	 * 显示时间uI
	 * @param curTime
	 * @param totalTime
	 * @param music
	 */
	public void refreshUI(int curTime, int totalTime, MusicInfo music) {
		if(mView==null)return;
		mCurrentMusicInfo = music;
		if(lyricManager!=null)
			lyricManager.refreshSeekProgress(curTime, totalTime);
		if (music.favorite == 1) {
			mIsFavorite = true;
			mFavoriteBtn.setImageResource(R.drawable.icon_favourite_checked);
		} else {
			mIsFavorite = false;
			mFavoriteBtn.setImageResource(R.drawable.icon_favourite_normal);
		}

		totalTime /= 1000;
		int totalminute = totalTime / 60;
		int totalsecond = totalTime % 60;
		String totalTimeString = String.format("%02d:%02d", totalminute,
				totalsecond);

		mTotalTimeTv.setText(totalTimeString);//时长
		mMusicNameTv.setText(music.musicName);//人物
		mArtistTv.setText(music.artist);//歌手
//		lyricManager.refreshSeekProgress(tempCurTime, tempTotalTime);
	}
	/**
	 * true为播放中
	 * @param flag true:正在播放，false为已经停止
	 */
	public void showPlay(boolean flag) {
		playerAndPause.setSelected(flag);
	}
	@Override
	public void onPause() {
		if(lyricManager!=null){
			lyricManager.onPause();
		}
		super.onPause();
	}

	public void refreshFavorite(int favorite) {
		if (favorite == 1) {
			mIsFavorite = true;
			mFavoriteBtn.setImageResource(R.drawable.icon_favourite_checked);
		} else {
			mIsFavorite = false;
			mFavoriteBtn.setImageResource(R.drawable.icon_favourite_normal);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_player_and_pause:
			playerAndPause.setSelected(!playerAndPause.isSelected());
			if(mServiceManager.getPlayState()==IConstants.MPS_PLAYING){//正在播放中
				mServiceManager.pause();
			}else{
				mServiceManager.rePlay();
			}
			break;
		case R.id.btn_playPre://上一首
			if (mCurrentMusicInfo == null) {
				return;
			}
			mServiceManager.prev();
			
			break;
		case R.id.btn_playNext://下一首
			if (mCurrentMusicInfo == null) {
				return;
			}
			mServiceManager.next();
			break;
		case R.id.btn_volume://声音控制
			mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//当前音量
			mVolumeSeekBar.setMax(200);
			float cur = mCurVolume/(mMaxVolume+0.0f)*200;
			mVolumeSeekBar.setProgress((int) cur);
			if (mVolumeLayout.isShown()) {
				mVolumeLayout.setVisibility(View.INVISIBLE);
				mVolumeLayout.startAnimation(view_out);
			} else {
				mVolumeLayout.setVisibility(View.VISIBLE);
				mVolumeLayout.startAnimation(view_in);
			}
			break;
		case R.id.btn_more://列表
			mActivity.startActivity(new Intent(mActivity,
					PlayQueueActivity.class));
			break;
		case R.id.btn_favorite://收藏
			if (mCurrentMusicInfo == null) {
				return;
			}
			mListNeedRefresh = true;
			if (!mIsFavorite) {
				startAnimation(mMoveIv);
				mFavoriteDao.saveMusicInfo(mCurrentMusicInfo);
				mMusicDao.setFavoriteStateById(mCurrentMusicInfo._id, 1);
				mFavoriteBtn.setImageResource(R.drawable.icon_favorite_on);
			} else {
				mFavoriteDao.deleteById(mCurrentMusicInfo._id);
				mMusicDao.setFavoriteStateById(mCurrentMusicInfo._id, 0);
				mFavoriteBtn.setImageResource(R.drawable.icon_favorite);
			}
			mIsFavorite = !mIsFavorite;
			break;
		case R.id.lyric_empty:
			// 点击下载歌词
			if (mCurrentMusicInfo == null) {
				return;
			}
			break;
		}
	}

	public void setMusicTimer(MusicTimer musicTimer) {
		this.mMusicTimer = musicTimer;
	}
	/**
	 * 声音控制
	 */
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			float f = progress/200f;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(mMaxVolume*f),0);
		}
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			mVolumeLayout.setVisibility(View.INVISIBLE);
			mVolumeLayout.startAnimation(view_out);
		}
		
	};
	
	private void startAnimation(View view) {
		view.setVisibility(View.VISIBLE);
		int fromX = view.getLeft();
		int fromY = view.getTop();

		AnimationSet animSet = new AnimationSet(true);
		// 注：ABSOLUTE表示离当前自己的View绝对的像素单位
		// 使用RELATIVE_TO_SELF和RELATIVE_TO_PARENT时一般用倍数关系 一般用1f 0f
		// 表示相对于自身或父控件几倍的移动
		TranslateAnimation transAnim = new TranslateAnimation(
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromX,
				Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -fromY);

		AlphaAnimation alphaAnim1 = new AlphaAnimation(0f, 1f);
		ScaleAnimation scaleAnim1 = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

		AlphaAnimation alphaAnim2 = new AlphaAnimation(1f, 0f);
		ScaleAnimation scaleAnim2 = new ScaleAnimation(1, 0, 1, 0,
				Animation.RELATIVE_TO_PARENT, Animation.RELATIVE_TO_PARENT);

		transAnim.setDuration(600);

		scaleAnim1.setDuration(600);
		alphaAnim1.setDuration(600);

		scaleAnim2.setDuration(800);
		alphaAnim2.setDuration(800);
		scaleAnim2.setStartOffset(600);
		alphaAnim2.setStartOffset(600);
		transAnim.setStartOffset(600);

		animSet.addAnimation(scaleAnim1);
		animSet.addAnimation(alphaAnim1);

		animSet.addAnimation(scaleAnim2);
		animSet.addAnimation(alphaAnim2);
		animSet.addAnimation(transAnim);
		view.startAnimation(animSet);
		view.setVisibility(View.GONE);
	}

	public void setListViewAdapter(MusicAdapter adapter) {
		mAdapter = adapter;
	}

	@Override
	public void onDrawerClosed() {
//		if (mListView != null) {
//			mListView.setVisibility(View.VISIBLE);
//		}
		if (mGridView != null) {
			mGridView.setVisibility(View.VISIBLE);
		}
		//mSliding.setVisibility(View.GONE);//隐藏当前界面
		if (mListNeedRefresh) {
			if (mIsFavorite) {
				mAdapter.refreshFavoriteById(mCurrentMusicInfo.songId, 1);
			} else {
				mAdapter.refreshFavoriteById(mCurrentMusicInfo.songId, 0);
			}
		}
	}
	@Override
	public void onDrawerOpened() {
//		if (mListView != null) {
//			mListView.setVisibility(View.INVISIBLE);
//		}
		if (mGridView != null) {
			mGridView.setVisibility(View.INVISIBLE);
		}
		if (!mIsLyricDownloading) {
			// 读取歌词文件
			//lyricManager.loadLyric(mCurrentMusicInfo);
		}
	}
	/**
	 * 监听界面滑动时是否隐藏
	 */
	private ScrollDrawerLayout.ScrollStateListener scrollListener = new ScrollDrawerLayout.ScrollStateListener(){

		@Override
		public void scrollState(float rawXDown, float rawXMove) {
			
		}

		@Override
		public void scrollEnd(float rawXDown,float rawYMove,boolean b) {
			if(!b){//播放界面已关闭
				backStack();
			}
		}
		
	};
	/**
	 * 读取些音乐的歌词
	 * @param music
	 */
	public void loadLyric(MusicInfo music) {
		this.mCurrentMusicInfo =music;
		if(lyricManager!=null)
			lyricManager.loadLyric(music);
		
	}
	
	/**
	 * 设置显示的背景图
	 * @param music
	 * @param imageLoad
	 */
	public void setBackgroundImage(MusicInfo music, ImageLoader imageLoad){
		this.mCurrentMusicInfo =music;
		this.imageLoad= imageLoad;
		mheadImg.setImageResource(R.drawable.sliding_bg);
//		mheadImg.setBackgroundResource(R.drawable.sliding_bg);
		if(music==null||imageLoad==null||music.headUrl==null||music.headUrl.size()<=0)return;
		imageLoad.load(mheadImg, music.headUrl.get(photoIndex));
	}
	
	/**
	 * 更新图片播放的进度
	 */
	Handler mHandlerPhoto = new Handler() {
		public void handleMessage(Message msg) {
			if(!isVisible()||mCurrentMusicInfo==null||imageLoad==null||mCurrentMusicInfo.headUrl==null||mCurrentMusicInfo.headUrl.size()<=0)return;
			photoIndex++;
			photoIndex = photoIndex>=mCurrentMusicInfo.headUrl.size()?0:photoIndex;
			imageLoad.load(mheadImg, mCurrentMusicInfo.headUrl.get(photoIndex));
		};
	};
	private TimerTasks timeTaskPhoto;
	/**
	 * 结束播放背景图片
	 */
	public void stopPhotoPlayer(){
		if(timeTaskPhoto!=null){
			timeTaskPhoto.stop();
			timeTaskPhoto = null;
		}
	}
	/**
	 * 开始播放背景图片
	 */
	public void startPhotoPlayer(){
		if(timeTaskPhoto!=null)stopPhotoPlayer();
		if(mCurrentMusicInfo==null||imageLoad==null||mCurrentMusicInfo.headUrl==null||mCurrentMusicInfo.headUrl.size()<=0)return;
		if(mCurrentMusicInfo.headUrl.size()>1){//如果图片多于一张
			timeTaskPhoto = new TimerTasks(8000,10000,new TimerTasks.TaskSchedule() {
				@Override
				public void schedule() {
					mHandlerPhoto.sendEmptyMessage(0);
				}
			});
		}
		
	}
	@Override
	public int createView() {
		// TODO Auto-generated method stub
		return layout_view;
	}
	@Override
	public void initView(Bundle bundle, View view) {
		this.mActivity = getActivity();
		this.mView = (ScrollDrawerLayout) view;
		mFavoriteDao = new FavoriteInfoDao(mActivity);
		mMusicDao = new MusicInfoDao(mActivity);
		DisplayMetrics metric = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;
		view_in = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
		view_out = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
		lyricManager = new LyricPlayerManager(mActivity,this,mServiceManager,mView);//设置歌词显示
		lyricManager.setMusicTimer(mMusicTimer);
		initView();
	}
	public Handler getHandler() {
		if(mHandler==null)
			mHandler = new Handler() {//一秒刷新一次
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					lyricManager.refreshSeekProgress(mServiceManager.position(),mServiceManager.duration());
				}
			};
		return mHandler;
	}
	
	
}
