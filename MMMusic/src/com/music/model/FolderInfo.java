/**
 * Copyright (c) www.longdw.com
 */
package com.music.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
/*
 * 文件夹信息
 */
public class FolderInfo extends BaseMusic implements Parcelable {

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle bundle = new Bundle();
		bundle.putString(KEY_FOLDER_NAME, folderName);
		bundle.putString(KEY_FOLDER_PATH, folderPath);
		dest.writeBundle(bundle);
	}
	
	// 用来创建自定义的Parcelable的对象
	public static Parcelable.Creator<FolderInfo> CREATOR = new Parcelable.Creator<FolderInfo>() {

		@Override
		public FolderInfo createFromParcel(Parcel source) {
			FolderInfo info = new FolderInfo();
			Bundle bundle = source.readBundle();
			info.folderName = bundle.getString(KEY_FOLDER_NAME);
			info.folderPath = bundle.getString(KEY_FOLDER_PATH);
			return info;
		}

		@Override
		public FolderInfo[] newArray(int size) {
			return new FolderInfo[size];
		}
	};

}
