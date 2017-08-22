/**
 * Copyright (c) www.longdw.com
 */
package com.music.uimanager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.adapter.PopupListAdapter;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.utils.MusicUtils;
import com.music.view.AlwaysMarqueeTextView;

/**
 * 我的音乐底部View控制
 * @author 
 *
 */
public class MainBottomUIManager implements OnClickListener,IConstants {
	
	private Activity mActivity;
	private View mView;
	private ServiceManager mServiceManager;
	private AlwaysMarqueeTextView mMusicNameTv, mArtistTv;
	private TextView mPositionTv, mDurationTv;
	private ImageButton playerAndPause, mNextBtn, mPlayerList;
	private ProgressBar mPlaybackProgress;
	public Handler mHandler;//更新界面的
	private Bitmap mDefaultAlbumIcon;
	private ImageView mHeadIcon;
	private PopupListAdapter popupListAdapter;
	private PopupWindow popView;
	private int mCurMode;
	
	public MainBottomUIManager(Activity a, View view) {
		this.mView = view;
		this.mActivity = a;
		this.mServiceManager = MusicApp.mServiceManager;
		initView();

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				refreshUI(mServiceManager.position(),
						mServiceManager.duration(),mServiceManager.getCurMusic());//更新主界面的播放状态
				updatePopUi();
			}
		};
	}
	
	private void initView() {
		mMusicNameTv = (AlwaysMarqueeTextView) findViewById(R.id.musicname_tv2);
		mArtistTv = (AlwaysMarqueeTextView) findViewById(R.id.artist_tv2);
		mPositionTv = (TextView) findViewById(R.id.position_tv2);
		mDurationTv = (TextView) findViewById(R.id.duration_tv2);

		playerAndPause = (ImageButton) findViewById(R.id.btn_player_and_pause);
		mNextBtn = (ImageButton) findViewById(R.id.btn_playNext2);
		mPlayerList = (ImageButton) findViewById(R.id.btn_player_list);

		playerAndPause.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mPlayerList.setOnClickListener(this);

		mPlaybackProgress = (ProgressBar) findViewById(R.id.playback_seekbar2);

		mDefaultAlbumIcon = BitmapFactory.decodeResource(
				mActivity.getResources(), R.drawable.img_album_background);

		mHeadIcon = (ImageView) findViewById(R.id.headicon_iv);
	}
	
	private View findViewById(int id) {
		return mView.findViewById(id);
	}
	/**
	 * 更新主界面的进度
	 * @param curTime
	 * @param totalTime
	 */
	public void refreshSeekProgress(int curTime, int totalTime) {

		curTime /= 1000;
		totalTime /= 1000;
		int curminute = curTime / 60;
		int cursecond = curTime % 60;

		String curTimeString = String.format("%02d:%02d", curminute, cursecond);
		mPositionTv.setText(curTimeString);

		int rate = 0;
		if (totalTime != 0) {
			rate = (int) ((float) curTime / totalTime * 100);
		}
		mPlaybackProgress.setProgress(rate);
	}
	
	public void refreshUI(int curTime, int totalTime, MusicInfo music) {

		int tempCurTime = curTime;
		int tempTotalTime = totalTime;

		totalTime /= 1000;
		int totalminute = totalTime / 60;
		int totalsecond = totalTime % 60;
		String totalTimeString = String.format("%02d:%02d", totalminute,
				totalsecond);

		mDurationTv.setText(totalTimeString);

		mMusicNameTv.setText(music.musicName);
		mArtistTv.setText(music.artist);

//		mHeadIcon.setBackgroundDrawable(new BitmapDrawable(mActivity
//				.getResources(), bitmap));
		refreshSeekProgress(tempCurTime, tempTotalTime);
	}
	/**
	 * true为播放中
	 * @param flag true:正在播放，false为已经停止
	 */
	public void showPlay(boolean flag) {
		playerAndPause.setSelected(flag);
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
		case R.id.btn_playNext2:
			mServiceManager.next();
			break;
		case R.id.btn_player_list://主界面的弹出播放列表
			showPopMusicList(v);
			break;
		}
	}
	/**
	 * 主界面的弹出最近播放列表
	 */
	private void showPopMusicList(View v) {
		View view = View.inflate(mActivity, R.layout.player_select_list, null);
		view.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				popView.dismiss();
				
			}
		});
		popView = popView!=null?popView: new PopupWindow(mActivity);
		popView.setAnimationStyle(R.style.popwin_anim_style);
		popView.setWidth(LayoutParams.MATCH_PARENT);
		popView.setHeight(LayoutParams.WRAP_CONTENT);
		popView.setBackgroundDrawable(new BitmapDrawable());
		popView.setFocusable(true);
		popView.setOutsideTouchable(true);
		popView.setContentView(view);
		popView.update();
		popView.showAtLocation((View)v.getParent(), Gravity.BOTTOM, 0, 0);
		
		ListView listView = (ListView) view.findViewById(R.id.listview_play_queue);
		popupListAdapter = new PopupListAdapter(mActivity, mServiceManager,this);
		listView.setAdapter(popupListAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MusicInfo musicInfo = popupListAdapter.getMusicCurrId(position);
				mServiceManager.playById(musicInfo.songId);
				popupListAdapter.notifyDataSetChanged();
				popView.dismiss();
			}
		});
		int mPlayingSongPosition = MusicUtils.seekPosInListById(mServiceManager.getMusicList(), mServiceManager.getCurMusicId());
		listView.setSelection(mPlayingSongPosition);
		final ImageView ivPlayMode = (ImageView) view.findViewById(R.id.iv_play_mode);
		//播放模式，单曲或--
		view.findViewById(R.id.ll_play_mode).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				changeMode(ivPlayMode);
				mServiceManager.setPlayMode(mCurMode);
				
			}
		});
		ivPlayMode.getDrawable().setLevel(mServiceManager.getPlayMode()+1);
	}
	private void changeMode(ImageView ivPlayMode) {
		mCurMode = mServiceManager.getPlayMode();
		mCurMode++;
		if (mCurMode > MPM_SINGLE_LOOP_PLAY) {
			mCurMode = MPM_LIST_LOOP_PLAY;
		}
		ivPlayMode.getDrawable().setLevel(mCurMode+1);
//		mPlayModeIv.setBackgroundResource(modeDrawable[mCurMode]);
	}
	public void updatePopUi(){//如果弹出框正在显示更新控件状态
		if(popView!=null&&popView.isShowing()){
			popupListAdapter.notifyDataSetChanged();
		}
	}
	/**
	 * 关闭弹出的展示列表
	 */
	public void hidePop() {
		if(popView!=null){
			popView.dismiss();
		}
		
	}

	public void showImage(MusicInfo music) {
		mHeadIcon.setImageResource(R.drawable.img_album_background);//播放控制区域的图片展示
		Bitmap bitmap = MusicUtils.getCachedArtwork(mActivity, music.albumId,
				mDefaultAlbumIcon);
		mHeadIcon.setImageBitmap(bitmap);

		
	}

}
