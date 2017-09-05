/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.music.service;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.model.MusicInfo;
import com.music.utils.MusicUtils;
import com.z.utils.LogUtils;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY and android.intent.action.MEDIA_BUTTON
 * intents, which is broadcast, for example, when the user disconnects the
 * headphones. This class works because we are declaring it in a
 * &lt;receiver&gt; tag in AndroidManifest.xml.
 */
public class BluetoothIntentReceiver extends BroadcastReceiver  implements IConstants{
	private static final String LOG_TAG = "MusicIntentReceiver";
	private Context mContext;
//	private KeyService mKeyService;
	private ServiceManager mServiceManager;
	private String TAG;

	@Override
	public void onReceive(Context context, Intent intent) {
		mServiceManager = MusicApp.mServiceManager;
		mContext = context;
		if(intent==null||context==null||intent.getExtras()==null)return;
//		mKeyService = new KeyService(mContext);
		KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
				Intent.EXTRA_KEY_EVENT);
//		System.out.println("测试             "+keyEvent.getKeyCode());
		if (intent.getAction().equals(
				android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			Toast.makeText(context, "Headphones disconnected.",
					Toast.LENGTH_SHORT).show();

			// send an intent to our MusicService to telling it to pause the
			// audio
			// context.startService(new Intent(MusicService.ACTION_PAUSE));

		} else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
			if(mServiceManager==null)return;
			abortBroadcast();
			Log.i(LOG_TAG, "ACTION_MEDIA_BUTTON!");
//			KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(
//					Intent.EXTRA_KEY_EVENT);
			if (keyEvent.getAction() != KeyEvent.ACTION_UP)//手未抬起
				return;
//			System.out.println("测试             "+KeyService.parseKeyCode(keyEvent.getKeyCode()));

			switch (keyEvent.getKeyCode()) {
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				LogUtils.d(TAG,"KEYCODE_MEDIA_PLAY_PAUSE");
				break;
			case KeyEvent.KEYCODE_MEDIA_PLAY://播放
				LogUtils.d(TAG,"KEYCODE_MEDIA_PLAY");
//				if(mServiceManager.getPlayState()==MPS_PLAYING){
//					mServiceManager.pause();
//				}else{
					mServiceManager.rePlay();
//				}
				System.out.println("KEYCODE_MEDIA_PLAY");
				break;
			case KeyEvent.KEYCODE_MEDIA_PAUSE:
				mServiceManager.pause();
				System.out.println("KEYCODE_MEDIA_PAUSE");
				break;
			case KeyEvent.KEYCODE_MEDIA_STOP:
				mServiceManager.seekTo(0);
				System.out.println("KEYCODE_MEDIA_STOP");
				break;
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				mServiceManager.next();
				System.out.println("KEYCODE_MEDIA_NEXT");//下一首
				break;
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
				mServiceManager.prev();
				System.out.println("KEYCODE_MEDIA_PREVIOUS");//上一首
				break;
			}
			MusicInfo curMusic = mServiceManager.getCurMusic();
			Bitmap defaultArtwork = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.img_album_background);
			Bitmap bitmap = MusicUtils.getCachedArtwork(context,curMusic.albumId, defaultArtwork);
			MusicApp.spSD.setLastPlayerId(curMusic._id);
			mServiceManager.updateNotification(bitmap, curMusic.musicName,curMusic.artist, mServiceManager.getPlayState());
		} else if (intent.getAction().equals(
				BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
			Log.i(LOG_TAG, "BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED");

			int state = intent.getIntExtra(
					BluetoothAdapter.EXTRA_CONNECTION_STATE, 0);

			if (state == BluetoothAdapter.STATE_CONNECTED
					|| state == BluetoothAdapter.STATE_DISCONNECTED) {
//				updateTime();
			}
			
		} else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.i(LOG_TAG, "Intent.ACTION_BOOT_COMPLETED");
//			updateTime();
		} else if (intent.getAction().equals("android.intent.action.UPDATE_SUSPEND_TIME_BY_HAND")) {
			Log.i(LOG_TAG, "Intent.UPDATE_SUSPEND_TIME_BY_HAND");
//			updateTime();
		} else {
			Log.i(LOG_TAG, "other intent");
		}
	}

}
