package com.music.lrc;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.fragment.SlidingManagerFragment;
import com.music.lrc.LyricLoadHelper.LyricListener;
import com.music.model.LyricSentence;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.storage.SPStorage;
import com.music.utils.LyricReadUtil;
import com.music.utils.MusicTimer;
import com.music.view.LyricsLineView;
import com.z.utils.LogUtils;

/**
 * @ClassName: LyricPlayerManager.java
 * @author zcs
 * @version V1.0
 * @Date 2015年12月14日 下午6:04:25
 * @Description: 播放界面的控件
 */
public class LyricPlayerManager implements IConstants {
	private Context content;
	private LyricsLineView mLrcNumView;
	private ServiceManager mServiceManager;
	private SeekBar mPlaybackSeekBar;
	private List<LyricSentence> lyricList;
	private LyricDownloadManager mLyricDownloadManager;
	/**
	 * 保存正在播放的音乐
	 */
	private MusicInfo mCurrentMusicInfo;
	/**
	 * 搜索歌词按钮
	 */
	private TextView mLrcEmptyView;
	private int mProgress;
	private SPStorage mSp;
	private TextView mCurTimeTv, mTotalTimeTv;
	long l = 300;// 默认动画时长
	protected String TAG = "LyricPlayerManager";
	private MusicTimer mMusicTimer;
	private View mView;
	private LyricLoadHelper mLyricLoadHelper;
	protected int newVals;
	protected boolean mPlayAuto;
	private SlidingManagerFragment smf;
//	private MusicInfo musicInfo;

	String lyricFilePath = null;
	public LyricPlayerManager(Context content, SlidingManagerFragment slidingManagerFragment, ServiceManager sm, View view) {
		this.content = content;
		this.mView = view;
		this.mServiceManager = sm;
		this.smf = slidingManagerFragment;
		// this.mLyricDownloadManager=mLyricDownloadManager;
		this.mLyricDownloadManager = new LyricDownloadManager(content);
		mSp = MusicApp.spSD;
		mLrcNumView = (LyricsLineView) findViewById(R.id.lyricshow);
		mLrcNumView.setWrapSelectorWheel(false);// 设置不循环滚动
		mLrcNumView.startRefreshLine();
		mPlaybackSeekBar = findViewById(R.id.playback_seekbar);
		mCurTimeTv = findViewById(R.id.currentTime_tv);
		mTotalTimeTv = findViewById(R.id.totalTime_tv);
		mLrcEmptyView = findViewById(R.id.lyric_empty);// 搜索歌词
		mLrcEmptyView.setText("没有歌词,点击手动下载");
		mLyricLoadHelper = new LyricLoadHelper();
		initView();
	}

	private <T extends View> T findViewById(int resId) {
		if (content == null)
			return null;
		return (T) mView.findViewById(resId);
	}

	private void initView() {
		mLyricLoadHelper.setLyricListener(mLyricListener);
		mLrcNumView.setOnValueChangedListener(valueChangeListener);
		mLrcNumView.setOnScrollListener(scrollListener);
		mPlaybackSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
		mPlaybackSeekBar.setMax(1000);// 设置进度值最大值为1000
		mLrcNumView.setVisibility(View.GONE);//默认显示歌词部分不显示
		mLrcEmptyView.setVisibility(View.VISIBLE);
		// mPlaybackSeekBar.setOnSeekBarChangeListener(l);
		mLrcEmptyView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mCurrentMusicInfo == null) {
					return;
				}
				showLrcDialog();
				
			}
		});// 搜索歌词

	}

	/**
	 * 设置选中歌词的位置
	 * 
	 * @param indexOfCurSentence
	 *            行数
	 */
	private void setSelectIndex(int indexOfCurSentence) {
		if(!smf.isVisible())return;
		if (mLrcNumView == null ||lyricList==null|| indexOfCurSentence < 0
				|| lyricList.size() <= indexOfCurSentence)
			return;
		LyricSentence lyric = lyricList.get(indexOfCurSentence);// 本次要显示的
		// LyricSentence lyric2 =
		// lyricList.get(indexOfCurSentence>1?indexOfCurSentence-1:indexOfCurSentence);//上次显示的、
		// 如果本次为空
		if (lyric.getContentText() != null
				&& lyric.getContentText().trim().length() <= 0) {
			indexOfCurSentence = indexOfCurSentence + 1 >= lyricList.size() ? indexOfCurSentence
					: indexOfCurSentence + 1;
			lyric = lyricList.get(indexOfCurSentence);// 本次要显示的
		}
		int value = mLrcNumView.getValue();
		if (indexOfCurSentence >= value) {
//			long time = lyric.getDuringTime() - lyric.getStartTime();
//			time = time < 50 ? 300 : time;
			mLrcNumView.smoothScrollToPositionFromTop(indexOfCurSentence,500);
		} else if (indexOfCurSentence == 0) {
			mLrcNumView.smoothScrollToPositionFromTop(indexOfCurSentence, 500);
		}

	}

	/**
	 * 取得歌曲同目录下的歌词文件绝对路径 下载歌词保存路径
	 * 
	 * @param musicName
	 * @param artist
	 */
	public void loadLyricByHand(String musicName, String artist) {
		if(TextUtils.isEmpty(artist)&&TextUtils.isEmpty(artist))return;
		mLrcEmptyView.setText("正在搜索歌词...");
		MusicInfo info = new MusicInfo();
		info.musicName = musicName;
		info.artist = artist;
		isNet = true;
		loadLyric(info);;
	}
	
	boolean isNet = false;
	/**
	 * 读取歌词
	 */
	public synchronized void loadLyric(MusicInfo playingSong) {
		//如果些歌词已加载过
		if (playingSong == null) return;
		if(mCurrentMusicInfo!=null&&mCurrentMusicInfo.songId==playingSong.songId)return;
		readLyric(playingSong);

	}
	private void readLyric(MusicInfo playingSong) {
		mLrcNumView.setVisibility(View.GONE);//默认显示歌词部分不显示
		mLrcEmptyView.setVisibility(View.VISIBLE);
		this.mCurrentMusicInfo = playingSong;
		new AsyncTask<MusicInfo, Void, ArrayList<LyricSentence>>(){
			@Override
			protected ArrayList<LyricSentence> doInBackground(MusicInfo... params) {
				MusicInfo info = params[0];
				String readFileLyrc = readFileLyrc(info);
				ArrayList<LyricSentence> loadLyric = mLyricLoadHelper.loadLyric(readFileLyrc);// 对歌词进行排版(按播放时间排序)
				if(loadLyric==null||loadLyric.size()<=0){
					if (mSp.getAutoLyric()||isNet) {//从网络加载
						LogUtils.i(TAG, "loadLyric()--->本地无歌词，尝试从网络获取");
						//网络歌词保存路径
						String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(info.musicName,info.artist, info.musicName);
						
						if(TextUtils.isEmpty(lyricFilePath)){
							return null;
						}
						return mLyricLoadHelper.loadLyric(lyricFilePath);
					} else {
						// 设置歌词为空
						mLyricLoadHelper.loadLyric(null);
					}
				}
				return loadLyric;
			}
			protected void onPostExecute(java.util.ArrayList<LyricSentence> result) {
				if(isNet){
					mLrcEmptyView.setText("没有找歌词,点击再次搜索");
				}
				isNet = false;
				lyricList = result;
				LogUtils.i(TAG, "加载歌词");
				if(result!=null&&result.size()>0){
					mLrcNumView.setVisibility(View.VISIBLE);
					mLrcEmptyView.setVisibility(View.GONE);
					mLrcNumView.setDisplayedValues(lyricList);
					seekBarccrollToLyric(mServiceManager.position(), true);
				}else{
					mLrcNumView.setVisibility(View.GONE);
					mLrcEmptyView.setVisibility(View.VISIBLE);
					mLrcNumView.setDisplayedValues(null);
				}
				mLrcNumView.setValue(0);
			};
			
			
		}.execute(playingSong);
	}
	
	
	/**
	 * 搜索歌词
	 * @return
	 */
	private String readFileLyrc(MusicInfo musicInfo) {
		// 取得歌曲同目录下的歌词文件绝对路径
		List<String> pathLyric0 = LyricReadUtil.getPathLyric(
				mSp.getUserLyricPath(), musicInfo.musicName);// 用户设置的
		List<String> pathLyric1 = LyricReadUtil.getPathLyric(
				musicInfo.folder, musicInfo.musicName);// 同一目录下
		List<String> pathLyric2 = LyricReadUtil.getPathLyric(
				MusicApp.lrcPath, musicInfo.musicName);// 默认路径下的
		String path = "";
		if (pathLyric0 != null && pathLyric0.size() > 0) {
			path = pathLyric0.get(0);
		} else if (pathLyric1 != null && pathLyric1.size() > 0) {
			path = pathLyric1.get(0);
		} else if (pathLyric2 != null && pathLyric2.size() > 0) {
			path = pathLyric2.get(0);
		}
		return path;
	}

	private LyricListener mLyricListener = new LyricListener() {

		@Override
		public void onLyricSentenceChanged(int indexOfCurSentence) {
			Log.i(TAG, "onLyricSentenceChanged--->当前句子索引=" + indexOfCurSentence);
			if (mScrollState != 1) {// 如果不在拖动时
				setSelectIndex(indexOfCurSentence);
			}
		}
	};

	/**
	 * 设置进度条显示的时间
	 * 
	 * @param curTime
	 *            播放到些位置
	 * @param totalTime
	 *            音乐文件的总长度
	 */
	public void refreshSeekProgress(int curTime, int totalTime) {
		int tempCurTime = curTime;

		curTime /= 1000;
		totalTime /= 1000;
		int curminute = curTime / 60;
		int cursecond = curTime % 60;

		String curTimeString = String.format("%02d:%02d", curminute, cursecond);
		mCurTimeTv.setText(curTimeString);

		int rate = 0;
		if (totalTime != 0) {
			rate = (int) ((float) curTime / totalTime * 1000);
		}
		mPlaybackSeekBar.setProgress(rate);// 拖动条显示到此位置
		mLyricLoadHelper.notifyTime(tempCurTime);// 歌词显示的位置
		// mLrcView.updateIndex(rate < 1 ? 100 : rate * 1000);x
		// mLrcView.updateIndex(tempCurTime);
	}

	boolean isPlayer = false;// 保存在拖动时是否正在播放
	private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			mPlayAuto = true;
			int pos = (int) (mProgress / 1000f * mServiceManager.duration());
			mServiceManager.seekTo(pos);
			refreshSeekProgress(mServiceManager.position(),
					mServiceManager.duration());
			if (isPlayer) {
				mServiceManager.rePlay();
				mMusicTimer.startTimer();
				isPlayer = false;
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			mPlayAuto = false;
			if (MPS_PLAYING == mServiceManager.getPlayState()) {// 正在播放
				isPlayer = true;
			}
			mMusicTimer.stopTimer();
			mServiceManager.pause();
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (!mPlayAuto) {
				mProgress = progress;
				int pos = (int) (progress / 1000f * (mCurrentMusicInfo==null?0:mCurrentMusicInfo.duration));// 歌曲应该所显示的位置
				seekBarccrollToLyric(pos, false);
			}

		}
	};

	/**
	 * 
	 * @param progress
	 */
	/**
	 * 拖动时歌词需要显示的位置
	 * 
	 * @param progress
	 *            所在的时间戳
	 * @param palySt
	 *            正在播放时，是否也要滚动，palySt==true?滚动?不在播放状态
	 */
	private void seekBarccrollToLyric(int progress, boolean palySt) {
		if (lyricList == null || lyricList.size() <= 0)
			return;
		if (palySt || mServiceManager.getPlayState() != MPS_PLAYING) {//
			for (int i = 0; i < lyricList.size(); i++) {
				LyricSentence lyricSentence = lyricList.get(i);
				if (lyricSentence.getStartTime() - progress >= 0) {
					if (i != mLrcNumView.getValue()) {// 是否是当前已显示
						i = i - 1 >= 0 ? i - 1 : 0;
						if (i != mLrcNumView.getValue()) {// 如果不是当前显示的
							mLrcNumView.smoothScrollToPositionFromTop(i, 500);
						}
					}
					break;
				}
			}
		}

	}

	int mScrollState = -1;
	private LyricsLineView.OnValueChangeListener valueChangeListener = new LyricsLineView.OnValueChangeListener() {

		@Override
		public void onValueChange(LyricsLineView picker, int oldVal, int newVal) {
			if (mScrollState == 1&&lyricList!=null&&lyricList.size()>newVal) {// 正在拖动歌词时
				LyricSentence lyricSentence = lyricList.get(newVals);
				mPlayAuto = true;
				refreshSeekProgress((int) lyricSentence.getStartTime(),
						mServiceManager.duration());
				if (MPS_PLAYING == mServiceManager.getPlayState()) {// 正在播放
					isPlayer = true;
				}
				mServiceManager.pause();
				mMusicTimer.stopTimer();
			}
			newVals = newVal;
		}
	};
	private LyricsLineView.OnScrollListener scrollListener = new LyricsLineView.OnScrollListener() {

		@Override
		public void onScrollStateChange(LyricsLineView view, int scrollState) {
			mScrollState = scrollState;
			if (scrollState == 0&&lyricList!=null&&lyricList.size()>newVals) {// 放手时
				LyricSentence lyricSentence = lyricList.get(newVals);
				mServiceManager.seekTo((int) lyricSentence.getStartTime());
				if (isPlayer) {
					mServiceManager.rePlay();
					mMusicTimer.startTimer();
					isPlayer = false;
				}
				if (lyricSentence.getContentText() != null
						&& TextUtils.isEmpty(lyricSentence.getContentText()
								.trim())) {
					setSelectIndex(newVals + 1);
				}
			}
		}
	};

	// private int getCurre(int postion){
	// if(lyricList==null)return 0;
	// LyricSentence lyricSentence = lyricList.get(postion);
	// return (int) lyricSentence.getStartTime();//开始的时间
	// }
	public void setMusicTimer(MusicTimer mMusicTimer) {
		this.mMusicTimer = mMusicTimer;

	}

	/**
	 * 要播放的音乐
	 * 
	 * @param mCurrentMusicInfo
	 */
	public void setCurrentMusicInfo(MusicInfo CurrentMusicInfo) {
		mCurrentMusicInfo = CurrentMusicInfo;
	}

	/**
	 * 搜索的弹出框
	 */
	private void showLrcDialog() {
		View view = View.inflate(content, R.layout.lrc_dialog, null);
		view.setMinimumWidth(510);
		final Dialog dialog = new Dialog(content, R.style.lrc_dialog);

		final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
		final Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
		final EditText artistEt = (EditText) view.findViewById(R.id.artist_tv);
		final EditText musicEt = (EditText) view.findViewById(R.id.music_tv);

		artistEt.setText(mCurrentMusicInfo.artist);
		musicEt.setText(mCurrentMusicInfo.musicName);
		OnClickListener btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == okBtn) {
					String artist = artistEt.getText().toString().trim();
					String music = musicEt.getText().toString().trim();
					if (TextUtils.isEmpty(artist) && TextUtils.isEmpty(music)) {
						Toast.makeText(content, "请输入条件", Toast.LENGTH_SHORT).show();
					} else {
						// 开始搜索
						loadLyricByHand(music, artist);
						dialog.dismiss();
					}
				} else if (v == cancleBtn) {
					dialog.dismiss();
				}
			}
		};
		okBtn.setOnClickListener(btnListener);
		cancleBtn.setOnClickListener(btnListener);
		dialog.setContentView(view);
		dialog.show();
	}

	public void onPause() {
		mLrcNumView.stopRefreshLine();
	}

	public void onResume() {
		mLrcNumView.startRefreshLine();
		
	}

}
