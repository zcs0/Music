package com.music.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.R;

/**
 * @ClassName:     MyGridViewAdapter.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2017年8月21日 上午10:00:27 
 * @Description:   主页分类显示模块
 */
public class MyGridViewAdapter extends BaseAdapter{
	private Activity mContext;

	public MyGridViewAdapter(Activity mContex){
		this.mContext = mContex;
	}

	private int[] drawable = new int[] { R.drawable.icon_local_music,
			R.drawable.icon_favorites, R.drawable.icon_folder_plus,
			R.drawable.icon_artist_plus, R.drawable.icon_album_plus };
	private String[] name = new String[] { "我的音乐", "我的最爱", "文件夹", "歌手",
			"专辑" };
	private int musicNum = 0, artistNum = 0, albumNum = 0, folderNum = 0, favoriteNum = 0;

	@Override
	public int getCount() {
		return 5;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setNum(int music_num, int artist_num, int album_num,
			int folder_num, int favorite_num) {
		musicNum = music_num;
		artistNum = artist_num;
		albumNum = album_num;
		folderNum = folder_num;
		favoriteNum = favorite_num;
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mContext.getLayoutInflater().inflate(
					R.layout.main_gridview_item, null);
			holder.iv = (ImageView) convertView
					.findViewById(R.id.gridview_item_iv);
			holder.nameTv = (TextView) convertView
					.findViewById(R.id.gridview_item_name);
			holder.numTv = (TextView) convertView
					.findViewById(R.id.gridview_item_num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		switch (position) {
		case 0:// 我的音乐
			holder.numTv.setText(musicNum + "");
			break;
		case 1:// 我的最爱
			holder.numTv.setText(favoriteNum + "");
			break;
		case 2:// 文件夹
			holder.numTv.setText(folderNum + "");
			break;
		case 3:// 歌手
			holder.numTv.setText(artistNum + "");
			break;
		case 4:// 专辑
			holder.numTv.setText(albumNum + "");
			break;
		}
		holder.iv.setImageResource(drawable[position]);
		holder.nameTv.setText(name[position]);

		return convertView;
	}

	private class ViewHolder {
		ImageView iv;
		TextView nameTv, numTv;
	}


}
