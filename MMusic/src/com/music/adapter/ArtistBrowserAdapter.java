package com.music.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.R;
import com.music.model.ArtistInfo;
import com.music.model.BaseMusic;
import com.music.service.ServiceManager;

/**
 * @ClassName:     ArtistBrowserAdapter.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月9日 下午2:22:16 
 * @Description:   歌手列表
 */
public class ArtistBrowserAdapter extends IBaseAdapter {

	List<ArtistInfo> mArtistList = new ArrayList<ArtistInfo>();
	private ServiceManager mServiceManager;
	public ArtistBrowserAdapter(Context context, ServiceManager sm,List<BaseMusic> baseMusic) {
		this.mContext = context;
		this.mServiceManager = sm;
		this.mMList = baseMusic;
		mArtistList.clear();
		for (BaseMusic bm : baseMusic) {
			if(bm instanceof ArtistInfo){
				mArtistList.add((ArtistInfo)bm);
			}
		}
	}

	@Override
	public int getCount() {
		return mArtistList.size();
	}

	@Override
	public BaseMusic getItem(int position) {
		return mArtistList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ArtistInfo artist = (ArtistInfo) getItem(position);
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.artistbrower_listitem, null);
			viewHolder.artistNameTv = (TextView) convertView
					.findViewById(R.id.artist_name_tv);
			viewHolder.numberTv = (TextView) convertView
					.findViewById(R.id.number_of_tracks_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.artistNameTv.setText(artist.artist_name);
		viewHolder.numberTv.setText(artist.number_of_tracks + "");

		return convertView;
	}

	private class ViewHolder {
		TextView artistNameTv, numberTv;
	}



}
