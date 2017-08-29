package com.music.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants.MusicType;
import com.music.model.AlbumInfo;
import com.music.model.BaseMusic;
import com.music.service.ServiceManager;

/**
 * @ClassName:     AlbumBrowserAdapter.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月9日 下午4:00:41 
 * @Description:   专辑
 */
public class AlbumBrowserAdapter extends IBaseAdapter{
	private List<AlbumInfo> mAlbumList = new ArrayList<AlbumInfo>();
	private ServiceManager mServiceManager;
	public AlbumBrowserAdapter(Context context, ServiceManager sm) {
		this.mContext = context;
		this.mServiceManager = sm;
		mAlbumList.clear();
		
		
	}
	@Override
	public void setData(List<BaseMusic> list, MusicType from) {
		for (BaseMusic bm : list) {
			if(bm instanceof AlbumInfo){
				mAlbumList.add((AlbumInfo)bm);
			}
		}
		
		super.setData(list, from);
	}
	@Override
	public int getCount() {
		return mAlbumList.size();
	}

	@Override
	public AlbumInfo getItem(int position) {
		return mAlbumList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		AlbumInfo album = getItem(position);

		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.albumbrower_listitem, null);
			viewHolder.albumNameTv = (TextView) convertView
					.findViewById(R.id.album_name_tv);
			viewHolder.numberTv = (TextView) convertView
					.findViewById(R.id.number_of_songs_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.albumNameTv.setText(album.album_name);
		viewHolder.numberTv.setText(album.number_of_songs + "首歌");

		return convertView;
	}

	private class ViewHolder {
		TextView albumNameTv, numberTv;
	}

}
