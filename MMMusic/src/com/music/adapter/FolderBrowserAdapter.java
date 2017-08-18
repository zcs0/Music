package com.music.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.R;
import com.music.activity.IConstants;
import com.music.model.BaseMusic;
import com.music.model.FolderInfo;
import com.music.service.ServiceManager;

/**
 * @ClassName:     FolderBrowserAdapter.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月8日 上午11:38:04 
 * @Description:   文件夹列表
 */
public class FolderBrowserAdapter extends IBaseAdapter implements IConstants{
	private List<FolderInfo> mMusicList = new ArrayList<FolderInfo>();
	private ServiceManager mServiceManager;
	public FolderBrowserAdapter(Context context, ServiceManager sm,List<BaseMusic> baseMusic) {
		this.mContext = context;
		this.mMList = baseMusic;
		mMusicList = new ArrayList<FolderInfo>();
		this.mServiceManager = sm;
		if(baseMusic==null)return;
		for (BaseMusic bm : baseMusic) {
			if(bm instanceof FolderInfo){
				mMusicList.add((FolderInfo)bm);
			}
		}
	}

	@Override
	public int getCount() {
		return mMusicList.size();
	}

	@Override
	public FolderInfo getItem(int position) {
		return mMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FolderInfo folder = getItem(position);
		ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.folderbrower_listitem, null);
			viewHolder.folderNameTv = (TextView) convertView
					.findViewById(R.id.folder_name_tv);
			viewHolder.folderPathTv = (TextView) convertView
					.findViewById(R.id.folder_path_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.folderNameTv.setText(folder.folderName);
		viewHolder.folderPathTv.setText(folder.folderPath);

		return convertView;
	}

	private class ViewHolder {
		TextView folderNameTv, folderPathTv;
	}
}
