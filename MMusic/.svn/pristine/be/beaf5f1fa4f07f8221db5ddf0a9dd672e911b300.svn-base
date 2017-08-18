package com.music.adapter;

import java.io.File;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.uimanager.MainBottomUIManager;
import com.music.utils.ListComparator;

/**
 * @ClassName: PopAdapter.java
 * @author zcs
 * @version V1.0
 * @Date 2015年12月20日 下午8:41:23
 * @Description: 主界面的弹出播放列表
 */
public class PopupListAdapter extends BaseAdapter implements OnClickListener,
		IConstants {
	protected static final String TAG = "PopupListAdapter";
	private List<MusicInfo> mMusicList;
	private Context context;
	private ServiceManager mServiceManager;
	private final int layoutRes = R.layout.music_player_list_view;
	private final int layoutRes2 = R.layout.delete_file_dialog;
	private MainBottomUIManager mainBottomUIManager;
	public PopupListAdapter(Context context, ServiceManager mServiceManager, MainBottomUIManager mainBottomUIManager) {
		this.context = context;
		this.mainBottomUIManager = mainBottomUIManager;
		this.mServiceManager = mServiceManager;
		this.mMusicList = mServiceManager.getMusicList();
		Collections.sort(mMusicList, new ListComparator());
	}

	@Override
	public void notifyDataSetChanged() {
		if(mMusicList==null||mMusicList.size()<=0){
			mainBottomUIManager.hidePop();
		}
		super.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMusicList == null ? 0 : mMusicList.size();
	}

	@Override
	public Object getItem(int position) {

		return mMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		MusicInfo musicInfo = mMusicList.get(position);
//		final int index = position;
		if (convertView == null) {
			convertView = View.inflate(context,
					layoutRes, null);
			viewHolder = new ViewHolder();
			viewHolder.iv_player_state = (ImageView) convertView
					.findViewById(R.id.iv_player_state);
			viewHolder.tv_sequence_number = (TextView) convertView
					.findViewById(R.id.tv_sequence_number);
			viewHolder.tv_music_name = (TextView) convertView
					.findViewById(R.id.tv_music_name);
			viewHolder.tv_song_name = (TextView) convertView
					.findViewById(R.id.tv_song_name);
			viewHolder.iv_delete = (ImageView) convertView
					.findViewById(R.id.iv_delete);
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		viewHolder.iv_player_state.setSelected(false);
		viewHolder.iv_player_state.setVisibility(View.GONE);
		viewHolder.tv_sequence_number.setVisibility(View.VISIBLE);
		int songId = mServiceManager.getCurMusicId();
		if (songId == musicInfo.songId) {// 正在播放
			viewHolder.iv_player_state.setVisibility(View.VISIBLE);
			viewHolder.tv_sequence_number.setVisibility(View.GONE);
			if (mServiceManager.getPlayState() == MPS_PLAYING) {//如果正在播放
				viewHolder.iv_player_state.setSelected(true);
			} else {
				viewHolder.iv_player_state.setSelected(false);
			}
		} else {
			
			viewHolder.tv_sequence_number.setVisibility(View.VISIBLE);
		}
		viewHolder.tv_sequence_number.setText((position + 1) + "");
		viewHolder.tv_music_name.setText(musicInfo.musicName);
		viewHolder.tv_song_name.setText(musicInfo.artist);
		viewHolder.iv_delete.setOnClickListener(new OnClickListener() {// 删除播放列表
			@Override
			public void onClick(View v) {
				MusicInfo musicInfo2 = mMusicList.get(position);
				mServiceManager.removeSongId(musicInfo2.songId);
				mMusicList.remove(musicInfo2);
				notifyDataSetChanged();
			}
		});
		/**
		 * 长按删除文件
		 */
		viewHolder.iv_delete.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showLrcDialog(mMusicList.get(position),position);
				return false;
			}
		});
		return convertView;
	}

	class ViewHolder {
		public ImageView iv_player_state;// 播放状态
		public TextView tv_sequence_number;// 播放序号
		public TextView tv_music_name;// 音乐name
		public TextView tv_song_name;// 歌曲name
		public ImageView iv_delete;// 删除列表中的一个
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_delete:// 删除播放列表

			break;

		default:
			break;
		}

	}

	/**
	 * 获得选中的
	 * 
	 * @param position
	 * @return
	 */
	public MusicInfo getMusicCurrId(int position) {
		return mMusicList.get(position);
	}
	/**
	 * 删除一个文件
	 * @param musicInfo
	 */
	private void showLrcDialog(final MusicInfo musicInfo,final int position) {
		if(musicInfo==null)return;
		View view = View.inflate(context, layoutRes2, null);
		view.setMinimumWidth(510);
		final Dialog dialog = new Dialog(context, R.style.lrc_dialog);

		Button okBtn = (Button) view.findViewById(R.id.ok_btn);
		Button cancleBtn = (Button) view.findViewById(R.id.cancel_btn);
		TextView artistEt = (TextView) view.findViewById(R.id.artist_tv);
		TextView musicEt = (TextView) view.findViewById(R.id.music_tv);
		TextView tvPath = (TextView) view.findViewById(R.id.music_path);
		artistEt.setText(musicInfo.artist);
		musicEt.setText(musicInfo.musicName);
		tvPath.setText(musicInfo.data);
		OnClickListener btnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int id = v.getId();
				if (R.id.ok_btn == id) {
					if(new File(musicInfo.data).isFile()){
						mServiceManager.removeSongId(musicInfo.songId);
						mMusicList.remove(musicInfo);
						boolean delete = new File(musicInfo.data).delete();
						Log.w(TAG, "删除文件"+musicInfo.data+" success?:"+delete);
					}
					notifyDataSetChanged();
				} else if (R.id.cancel_btn == id) {
				}
				dialog.dismiss();
			}
		};
		okBtn.setOnClickListener(btnListener);//确定删除一个
		cancleBtn.setOnClickListener(btnListener);//关闭删除一个
		dialog.setContentView(view);
		dialog.show();
	}

}
