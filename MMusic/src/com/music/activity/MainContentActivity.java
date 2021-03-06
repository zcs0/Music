/**
 * Copyright (c) www.longdw.com
 */
package com.music.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.music.MusicApp;
import com.music.R;
import com.music.aidl.IMediaService;
import com.music.fragment.MainFragment;
import com.music.fragment.MenuFragment;
import com.music.interfaces.IOnServiceConnectComplete;
import com.music.model.BaseMusic;
import com.music.model.MusicInfo;
import com.music.service.BluetoothIntentReceiver;
import com.music.service.ServiceManager;
import com.music.slidemenu.SlidingMenu;
import com.music.utils.MusicUtils;
import com.z.utils.LogUtils;

/**
 * 主类，首次进入应用会到这里
 * 该类提供了首页MainFragment的显示和侧滑MenuFragment的显示
 * @author 
 *
 */
public class MainContentActivity extends BaseActivity implements IConstants, IOnServiceConnectComplete {

	public static final String ALARM_CLOCK_BROADCAST = "alarm_clock_broadcast";
	public SlidingMenu mSlidingMenu;
	private List<OnBackListener> mBackListeners = new ArrayList<OnBackListener>();
	public MainFragment mMainFragment;
	private ServiceManager mServiceManager;

	private Handler mHandler;
//	private MusicInfoDao mMusicDao;
//	private SplashScreen mSplashScreen;
	private int mScreenWidth;
	protected String TAG="MainContentActivity";
//	private MusicIntentReceiver bluetoothReceiver;//蓝牙控制
	int isLoadingOk=0;
	public interface OnBackListener {
		public abstract void onBack();
	}
	@Override
	protected void onCreateView(Bundle savedInstanceState) {
		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();//metric.widthPixels;
		initSDCard();//设置SD卡监听
		mServiceManager = MusicApp.mServiceManager;
		IntentFilter filter = new IntentFilter();
		filter.addAction(ALARM_CLOCK_BROADCAST);
		registerReceiver(mAlarmReceiver, filter);

		setContentView(R.layout.activity_main);
		MusicApp.mServiceManager.setOnServiceConnectComplete(this);//设置绑定播放服务的监听
//		mSplashScreen = new SplashScreen(this);//引导界面
//		mSplashScreen.show(R.drawable.image_splash_background,
//				SplashScreen.SLIDE_LEFT);
		// set the Above View
		mMainFragment = new MainFragment();//主界面
//		mMainFragment.setServiceManager(mServiceManager);//设置音乐管理者
		FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
		beginTransaction.add(R.id.frame_main, mMainFragment).commit();
		// configure the SlidingMenu
		mSlidingMenu = new SlidingMenu(this);//侧边框
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.setFadeDegree(0.35f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mSlidingMenu.setMenu(R.layout.frame_menu);
		//测边显示
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_menu, new MenuFragment()).commit();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
//				mSplashScreen.removeSplashScreen();
				mMainFragment.refreshNum();//刷新音乐分类的个数
				if(msg.what==1){
					dismissLoadingDialog();
					isLoadingOk ++;
					setPlayerList();
//					mMainFragment.initData();
				}
			}
		};
//		if(MusicApp.spSD.getIsFirst()){
//			initData();//从数据库中读取
//		}else{
			readData();
//		}
//		sendBluetooth();
	}
	/**
	 * 监听SD的状态
	 */
	private void initSDCard() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.setPriority(1000);// 设置最高优先级
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为
															// USB大容量存储被共享，挂载被解除
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
		// intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
		// intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
		intentFilter.addDataScheme("file");
		registerReceiver(sdCardReceiver, intentFilter);// 注册监听函数
	}
	/**
	 * 读取本地文件到数据库
	 */
	private void initData() {
		showLoadingDialog("初始化数据中...");
//		mHandler.sendMessageDelayed(mHandler.obtainMessage(), 500);
		new Thread(){
			public void run() {
				LogUtils.w(TAG, "开始读取手机中的音乐到数据库中...");
				MusicUtils.initMusic(MainContentActivity.this);
				MusicUtils.initArtist(MainContentActivity.this);
				MusicUtils.initAlbum(MainContentActivity.this);
				mHandler.sendEmptyMessage(1);
			};
			
		}.start();
		
	}
	
	private void readData(){
		showLoadingDialog("数据加载中...");
		//读取数据库中的数据，如果不存在，读取SD中的音乐
		new Thread(new Runnable() {
			@Override
			public void run() {
				MusicUtils.queryByType(MainContentActivity.this, MusicType.START_FROM_LOCAL);
				MusicUtils.queryByType(MainContentActivity.this, MusicType.START_FROM_ALBUM);
				MusicUtils.queryByType(MainContentActivity.this, MusicType.START_FROM_ARTIST);
				MusicUtils.queryByType(MainContentActivity.this, MusicType.START_FROM_FAVORITE);
				MusicUtils.queryByType(MainContentActivity.this, MusicType.START_FROM_FOLDER);
//				MusicUtils.queryMusic(MainContentActivity.this);
//				MusicUtils.queryAlbums(MainContentActivity.this);
//				MusicUtils.queryArtist(MainContentActivity.this);
//				MusicUtils.queryFolder(MainContentActivity.this);
				LogUtils.w(TAG, "读取手机音乐结束...");
				mHandler.sendEmptyMessage(1);
			}
		}).start();
	}

	public void registerBackListener(OnBackListener listener) {
		if (!mBackListeners.contains(listener)) {
			mBackListeners.add(listener);
		}
	}

	public void unRegisterBackListener(OnBackListener listener) {
		mBackListeners.remove(listener);
	}
	//关闭界面
	@Override
	public void onBackPressed() {
		exitCheck();
	}
	
	private void exitCheck(){
		int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
		if(mSlidingMenu.isMenuShowing()){//侧边是否正在显示
			mSlidingMenu.showContent();
		}else if(backStackEntryCount<=0&&mServiceManager.getPlayState()!=MPS_PLAYING){//不在播放中退出程序
			unregisterReceiver(sdCardReceiver);
			unregisterReceiver(mAlarmReceiver);
//			unregisterReceiver(bluetoothReceiver);//蓝牙
			mMainFragment.unPlayBroadcast();//
			MusicApp.mServiceManager.exit();
			MusicApp.mServiceManager = null;
			MusicUtils.clearCache();
			cancleSleepClock();
			System.exit(0);
		}else{
			mMainFragment.refreshNum();
			super.onBackPressed();
		}
		
	}

	private final BroadcastReceiver sdCardReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.MEDIA_REMOVED")// 各种未挂载状态
					|| action.equals("android.intent.action.MEDIA_UNMOUNTED")
					|| action.equals("android.intent.action.MEDIA_BAD_REMOVAL")
					|| action.equals("android.intent.action.MEDIA_SHARED")) {
				finish();
				Toast.makeText(MainContentActivity.this, "SD卡以外拔出，本地数据没法初始化!",
						Toast.LENGTH_SHORT).show();
			}
		}
	};
	/**
	 * 显示睡眠dialog
	 */
	public void showSleepDialog() {
		if (MusicApp.mIsSleepClockSetting) {
			cancleSleepClock();
			Toast.makeText(getApplicationContext(), "已取睡眠模式！",
					Toast.LENGTH_SHORT).show();
			return;
		}

		View view = View.inflate(this, R.layout.sleep_time, null);
		final Dialog dialog = new Dialog(this, R.style.lrc_dialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);

		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		// lp.x = 100; // 新位置X坐标
		// lp.y = 100; // 新位置Y坐标
		lp.width = (int) (mScreenWidth * 0.7); // 宽度
		// lp.height = 400; // 高度

		// 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
		// dialog.onWindowAttributesChanged(lp);
		dialogWindow.setAttributes(lp);

		dialog.show();

		final Button cancleBtn = (Button) view.findViewById(R.id.cancle_btn);
		final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
		final EditText timeEt = (EditText) view.findViewById(R.id.time_et);
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == cancleBtn) {
					dialog.dismiss();
				} else if (v == okBtn) {
					String timeS = timeEt.getText().toString();
					if (TextUtils.isEmpty(timeS)
							|| Integer.parseInt(timeS) == 0) {
						Toast.makeText(getApplicationContext(), "输入无效！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					setSleepClock(timeS);
					dialog.dismiss();
				}
			}
		};

		cancleBtn.setOnClickListener(listener);
		okBtn.setOnClickListener(listener);
	}

	/**
	 * 设置睡眠闹钟
	 * 
	 * @param timeS
	 */
	private void setSleepClock(String timeS) {
		Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				MainContentActivity.this, 0, intent, 0);
		// 设置time时间之后退出程序
		int time = Integer.parseInt(timeS);
		long longTime = time * 60 * 1000L;
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC, System.currentTimeMillis() + longTime,
				pendingIntent);
		MusicApp.mIsSleepClockSetting = true;
		Toast.makeText(getApplicationContext(), "将在"+timeS+"分钟后退出软件", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 取消睡眠闹钟
	 */
	private void cancleSleepClock() {
		Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				MainContentActivity.this, 0, intent, 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pendingIntent);
		MusicApp.mIsSleepClockSetting = false;
	}
	/**
	 * 程序退出的广播
	 */
	private BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
//			mServiceManager.cancelNotification();
			exitCheck();
			//退出程序
			finish();
		}

	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sdCardReceiver);
		unregisterReceiver(mAlarmReceiver);
		MusicApp.stopTimer();
//		cancleSleepClock();
//		System.exit(0);
	}
	/**
	 * 绑定服务的监听
	 */
	@Override
	public void onServiceConnectComplete(IMediaService service) {
		isLoadingOk++;
		setPlayerList();
	}
	
	/**
	 * 设置播放列表
	 * @param type
	 * @param info
	 */
	private void setPlayerList() {
		if(isLoadingOk<2)return;
		isLoadingOk = 0;
		new AsyncTask<Void, Void, List<MusicInfo> >(){
			@Override
			protected List<MusicInfo>  doInBackground(Void... params) {
				int i = MusicApp.spSD.getLastPlayerListType();//最后次的type
				MusicType type=MusicType.START_FROM_LOCAL;;
				if(i<=0){
					type = MusicType.START_FROM_LOCAL;
				}
				String info = MusicApp.spSD.getLastPlayerMusicInfo();//最后次的type
				LogUtils.d(TAG, "设置播放列表");
				List<BaseMusic> queryMusic = new ArrayList<BaseMusic>();
				switch (type) {
				case START_FROM_LOCAL:// 我的音乐
					queryMusic = MusicUtils.queryMusic(MainContentActivity.this);
					break;
				case START_FROM_FAVORITE://我的最爱
					queryMusic = MusicUtils.queryFavorite(MainContentActivity.this);
					break;
				case START_FROM_FOLDER://文件夹
					queryMusic = MusicUtils.queryMusicByFolder(MainContentActivity.this,info);
					break;
				case START_FROM_ARTIST://歌手
					queryMusic = MusicUtils.queryMusicByArtist(MainContentActivity.this,info);
					break;
				case START_FROM_ALBUM:// 专辑
					queryMusic = MusicUtils.queryMusiceAlbums(MainContentActivity.this,Integer.valueOf(info));
					break;
				}
				List<MusicInfo> musicList = new ArrayList<MusicInfo>();
				for (BaseMusic baseMusic : queryMusic) {
					if(baseMusic instanceof MusicInfo){
						musicList.add((MusicInfo)baseMusic);
					}
				}
				LogUtils.d(TAG, "设置播放列表完毕");
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
	private void sendBluetooth(){
		IntentFilter intent = new IntentFilter();
		intent.setPriority(Integer.MAX_VALUE);
//			intent.addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
		intent.addAction("android.media.AUDIO_BECOMING_NOISY");
		intent.addAction("android.intent.action.MEDIA_BUTTON");
		intent.addAction("android.intent.action.VOICE_COMMAND");
//			intent.addAction("android.intent.action.ACTION_SCREEN_ON");//锁屏时会自动退出程序
//			intent.addAction("android.intent.action.SCREEN_OFF");//锁屏时会自动退出程序
		intent.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
		intent.addAction("android.intent.action.ACTION_SHUTDOWN");
		intent.addAction("android.intent.action.BOOT_COMPLETED");
		intent.addAction("android.permission.BLUETOOTH");
		intent.addAction("android.permission.BLUETOOTH_ADMIN");
		intent.addAction("android.intent.action.UPDATE_SUSPEND_TIME_BY_HAND");
		intent.addAction("android.media.AUDIO_BECOMING_NOISY");
		intent.addAction("android.intent.action.MEDIA_BUTTON");
		intent.addAction("android.intent.action.VOICE_COMMAND");
		intent.addAction("android.intent.action.ACTION_SCREEN_ON");
		intent.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
		intent.addAction("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
		intent.addAction("android.intent.action.ACTION_SHUTDOWN");
		intent.addAction("android.intent.action.BOOT_COMPLETED");
		intent.addAction("android.intent.action.UPDATE_SUSPEND_TIME_BY_HAND");
		BluetoothIntentReceiver bluetoothReceiver = new BluetoothIntentReceiver();
		registerReceiver(bluetoothReceiver, intent);
//			bluetoothReceiver.abortBroadcast();//中断下一个接收
}

}
