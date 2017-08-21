/**
 * Copyright (c) www.longdw.com
 */
package com.music.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants;
import com.music.db.FavoriteInfoDao;
import com.music.db.MusicInfoDao;
import com.music.interfaces.IQueryFinished;
import com.music.model.BaseMusic;
import com.music.model.MusicInfo;
import com.music.service.ServiceManager;
import com.music.utils.ListComparator;
import com.music.utils.MusicUtils;

/**
 * 显示歌曲列表
 * @author ZCS
 *
 */
public class MusicAdapter extends IBaseAdapter implements IConstants {

	private LayoutInflater mLayoutInflater;
	private List<MusicInfo> mMusicList;
//	private ServiceManager mServiceManager;

	private int mPlayState, mCurPlayMusicIndex = -1;
//	private IQueryFinished mIQueryFinished;
	private FavoriteInfoDao mFavoriteDao;
	private MusicInfoDao mMusicDao;
	private int mFrom;

	class ViewHolder {
		TextView musicNameTv, artistTv, durationTv;
		ImageView playStateIconIv, favoriteIv;
	}
	public MusicAdapter(Context context, ServiceManager sm,List<BaseMusic> baseMusic) {
		mMusicList = new ArrayList<MusicInfo>();
		this.mContext = context;
		this.mMList = baseMusic;
//		this.mServiceManager = sm;
		if(baseMusic==null)return;
		mFavoriteDao = new FavoriteInfoDao(context);
		mMusicDao = new MusicInfoDao(context);
		for (BaseMusic bm : baseMusic) {
			if(bm instanceof MusicInfo){
				mMusicList.add((MusicInfo)bm);
			}
		}
//		Collections.sort(mMusicList, comparator);
		mMList.clear();
		for (BaseMusic baseMusic2 : mMusicList) {//
			mMList.add(baseMusic2);
		}
		Collections.sort(mMList, new ListComparator());
		
	}
	
	public List<MusicInfo> getmMusicList() {
		return mMusicList;
	}


	public void setData(List<BaseMusic> list, int from) {
		setData(list);
		this.mFrom = from;
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


	public void setPlayState(int playState, int playIndex) {
		mPlayState = playState;
		mCurPlayMusicIndex = playIndex;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mMusicList.size();
	}

	@Override
	public MusicInfo getItem(int position) {
		return mMusicList.get(position);
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
			convertView.setTag(viewHolder);
		}
		viewHolder = (ViewHolder) convertView.getTag();

		if (position != mCurPlayMusicIndex) {
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
					mFavoriteDao.deleteById(music._id);
					mMusicDao.setFavoriteStateById(music._id, 0);
					if(mFrom == MusicType.START_FROM_FAVORITE.getValue()) {//移除收藏
						mMusicList.remove(position);
						notifyDataSetChanged();
					}
				} else {
					mFavoriteDao.saveMusicInfo(music);
					mMusicDao.setFavoriteStateById(music._id, 1);
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

		return convertView;
	}
}
