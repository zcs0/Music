/**
 * Copyright (c) www.longdw.com
 */
package com.music.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.db.MusicInfoDao;
import com.music.dialog.BaseDialog;
import com.music.interfaces.IQueryFinished;
import com.music.model.BaseMusic;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.utils.MusicUtils;
import com.z.utils.FileUtils;
import com.z.utils.SizeUtils;

/**
 * 显示歌曲列表
 * @author ZCS
 *
 */
public class MusicAdapter extends IBaseAdapter implements IConstants {

	private LayoutInflater mLayoutInflater;
	private List<MusicInfo> mMusicList;
//	private ServiceManager mServiceManager;

	private int mPlayState;
//	private IQueryFinished mIQueryFinished;
//	private FavoriteInfoDao mFavoriteDao;
	private MusicInfoDao mMusicDao;
	private ServiceManager smMang;
	

	class ViewHolder {
		TextView musicNameTv, artistTv, durationTv;
		ImageView playStateIconIv, favoriteIv,musicinfo;
	}
	public MusicAdapter(Context context, ServiceManager sm) {
		this.mContext = context;
//		mFavoriteDao = new FavoriteInfoDao(context);
		mMusicDao = new MusicInfoDao(context);
		mMList = null;
		this.smMang = sm;
	}
	
	public List<MusicInfo> getmMusicList() {
		return mMusicList;
	}
	@Override
	public void setData(List<BaseMusic> list) {
		super.setData(list);
		mMusicList = new ArrayList<MusicInfo>();
		if(list==null)return;
		for (BaseMusic bm : list) {
			if(bm instanceof MusicInfo){
				mMusicList.add((MusicInfo)bm);
			}
		}
	}
	public void setData(List<BaseMusic> list, IConstants.MusicType from) {
		setData(list);
		super.setData(list,from);
	}
//
//	public void refreshPlayingList() {
//		if(mMusicList.size() > 0) {
//			mServiceManager.refreshMusicList(mMusicList);
//		}
//	}
	
	public void refreshFavoriteById(int id, int favorite) {
		int position = MusicUtils.seekPosInListById(mMusicList, id);
		mMusicList.get(position).favorite = favorite;
		notifyDataSetChanged();
	}


	public void setQueryFinished(IQueryFinished finish) {
//		mIQueryFinished = finish;
	}

	/**
	 * @param playState
	 * @param playId
	 */
	public void setPlayState(int playState, int playId) {
		mPlayState = playState;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mMusicList==null?0:mMusicList.size();
	}

	@Override
	public MusicInfo getItem(int position) {
		return mMusicList==null?null:mMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		final MusicInfo music = getItem(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.music_list_item, null);
			viewHolder.musicNameTv = (TextView) convertView
					.findViewById(R.id.musicname_tv);
			viewHolder.artistTv = (TextView) convertView
					.findViewById(R.id.artist_tv);
			viewHolder.durationTv = (TextView) convertView
					.findViewById(R.id.duration_tv);
			viewHolder.playStateIconIv = (ImageView) convertView
					.findViewById(R.id.playstate_iv);
			viewHolder.favoriteIv = (ImageView) convertView
					.findViewById(R.id.favorite_iv);
			viewHolder.musicinfo = (ImageView) convertView.findViewById(R.id.btn_music_info);
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();
		int musicId = music._id;
//		MusicInfo curMusic = smMang.getCurMusic();
		int curMusicId = smMang.getCurMusicId();//正在播放的id
//		System.out.println(id);
//		int curMusicId = curMusic._id;//smMang.getCurMusicId();
		if (musicId != curMusicId) {
			viewHolder.playStateIconIv.setVisibility(View.GONE);
		} else {
			viewHolder.playStateIconIv.setVisibility(View.VISIBLE);
			if (mPlayState == MPS_PAUSE) {
				viewHolder.playStateIconIv
						.setBackgroundResource(R.drawable.list_pause_state);
			} else {
				viewHolder.playStateIconIv
						.setBackgroundResource(R.drawable.list_play_state);
			}
		}
		//收藏为喜欢的
		viewHolder.favoriteIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(music.favorite == 1) {//如果当前展示的为的收藏的
					mMusicList.get(position).favorite = 0;
					MusicUtils.removeFavoriteStateById(mContext, music._id);;//添加收藏
//					mMusicDao.setFavoriteStateById(music._id, 0);
					if(mFrom == MusicType.START_FROM_FAVORITE) {//移除当前显示收藏
						mMusicList.remove(position);
						notifyDataSetChanged();
					}
				} else {
//					MusicUtils.addFavoriteStateById(mContext,music);
//					mFavoriteDao.saveMusicInfo(music);
					MusicUtils.addFavoriteStateById(mContext,music._id);
//					mMusicDao.setFavoriteStateById(music._id, 1);
					mMusicList.get(position).favorite = 1;//设置为收藏
				}
				notifyDataSetChanged();
			}
		});
		viewHolder.favoriteIv.setSelected(false);
		if(music.favorite == 1) {//如果已是收藏的
			viewHolder.favoriteIv.setSelected(true);
		}

		viewHolder.musicNameTv.setText((position + 1) + "." + music.musicName);
		viewHolder.artistTv.setText(music.artist);
		viewHolder.durationTv
				.setText(MusicUtils.makeTimeString(music.duration));
		showDialog(viewHolder.musicinfo,music,position);
		return convertView;
	}
	
	private void showDialog(View view,final MusicInfo mu,final int position){
		view.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//dalog_musicinfo_layout
//				View view = View.inflate(mContext, R.layout.dalog_musicinfo_layout, null);
//				TextView tv = (TextView) view.findViewById(R.id.tv_info);
				BaseDialog baseDialog = new BaseDialog(mContext);
				String str = "标题:"+mu.musicName+"\n";
				str += "歌手:"+mu.artist+"\n";
				str += "时长:"+getTime(mu.duration)+"\n";
				str += "大小:"+getSize(mu.data)+"\n";
				str += "路径:"+mu.data+"\n";
//				baseDialog.setContentTxt(str);
//				tv.setText(str);
//				baseDialog.setContentView(view,true);
				baseDialog.setMessage(str);
				baseDialog.setCenterGravity(Gravity.LEFT);
				baseDialog.show();
				baseDialog.setPositiveButton("删除", new BaseDialog.OnClickListener() {
					@Override
					public void onClick(Dialog dialog, int which) {
						int curMusicId = smMang.getCurMusicId();
						int id = mu._id;
						if(id==curMusicId&&(smMang.getPlayState()==MPS_PLAYING||smMang.getPlayState()==MPS_PREPARE)){//如果删除的正是播放的音乐
							int index = mMusicList.size();
							if(position+1>=index){
								index=position-1;
							}else{
								index = position+1;
							}
							if(index>0&&index<mMusicList.size()){
								MusicInfo musicInfo = mMusicList.get(index);
								smMang.playById(musicInfo._id);
							}
						}
						mMusicList.remove(position);
						remove(mu._id);
						dialog.dismiss();
					}
				});
				baseDialog.setCancel("取消");
				
			}
		});
	}
	/**
	 * 从文件中删除一个
	 */
	private void remove(int id){
		List<MusicInfo> musicList = smMang.getMusicList();
		int index = 0;
		for (MusicInfo musicInfo : musicList) {
			index++;
			if(musicInfo._id == id){
				musicList.remove(musicInfo);
				break;
			}
		}
		MusicApp.refreshMusicList(musicList, index);
		MusicUtils.delete(mContext, id);
		notifyDataSetChanged();
		
	}
	private String getSize(String path){
		return SizeUtils.fileSize(FileUtils.getFileSize(path));
	}
	private String getTime(int totalTime){
		totalTime /= 1000;
		int totalminute = totalTime / 60;
		int totalsecond = totalTime % 60;
		String totalTimeString = String.format("%02d:%02d", totalminute,totalsecond);
		return totalTimeString;
	}
	
}
