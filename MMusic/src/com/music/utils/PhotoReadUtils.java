package com.music.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.music.model.MusicInfo;
import com.music.model.PhotoUrlInfo;
import com.music.model.PhotoUrlInfo.PicUrl;
import com.z.utils.JSONUtils;

/**
 * @ClassName: LyricRead.java
 * @author zcs
 * @version V1.0
 * @Date 2015年12月16日 下午5:57:56
 * @Description: 用来读取图片列表
 */
public class PhotoReadUtils {
	private static List lyric = new ArrayList<String>();
	private static String musicName;
	private static String artist;
	/**
	 * 所有符合的文件
	 * @param path
	 * @param musicName
	 * @param artist {@link MusicInfo#artist}
	 * @return
	 */
	public static List<String> getPath(String path, String musicName,
			String artist) {
		lyric = new ArrayList<String>();
		if (TextUtils.isEmpty(path) || TextUtils.isEmpty(musicName)
				|| !new File(path).isDirectory())
			return null;
//		System.out.println("检察头像   " + path);
		PhotoReadUtils.musicName = musicName;
		PhotoReadUtils.artist = artist;
		File file = new File(path);
		return getPath(new File(path));
	}

	private static List<String> getPath(File file) {
		
		if (file.isFile()) {
			String name = file.getName();
			String parent = file.getParent();
			if (parent.contains(musicName) || parent.contains(artist)) {// 父类名
				if ((name.lastIndexOf(".json") != -1 || name
						.lastIndexOf(".txt") != -1)) {
					lyric.add(file.getPath());
				}
			}
		} else if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				getPath(file2);
			}
		}
		return lyric;
	}

	/**
	 * 解析此文件里的json
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> fileToUrl(File file) {
		String url = null;
		List<String> urlList = new ArrayList<String>();
		if (file == null || !file.isFile())
			return null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			StringBuffer sb = new StringBuffer();
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			url = sb.toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		PhotoUrlInfo jsonToUrlList = jsonToUrlList(url);
		if (jsonToUrlList != null && jsonToUrlList.data != null
				&& jsonToUrlList.data.size() > 0) {
			List<PicUrl> picUrls = jsonToUrlList.data.get(0).picUrls;
			for (PicUrl picUrl : picUrls) {
				urlList.add(picUrl.picUrl);
			}
		}

		return urlList;
	}

	public static PhotoUrlInfo jsonToUrlList(String str) {
		return JSONUtils.jsonToObj(str, PhotoUrlInfo.class);
	}

}
