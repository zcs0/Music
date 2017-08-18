package com.music.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

/**
 * @ClassName:     LyricRead.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月16日 下午5:57:56 
 * @Description:   用来读取本地歌词的操作
 */
public class LyricReadUtil {
	private static List lyric=new ArrayList<String>();
	private static String fileName;
	public static List<String> getPathLyric(String path,String fileName){
		lyric=new ArrayList<String>();
		if(TextUtils.isEmpty(path)||TextUtils.isEmpty(fileName)||!new File(path).isDirectory()) return null;
		LyricReadUtil.fileName = fileName;
		File file = new File(path);
		
		return getPath(new File(path));
	}
	
	private static List<String> getPath(File file){
//		System.out.println("检察   "+file.getPath());
		if(file.isFile()){
			String name = file.getName();
			
			if(name.contains(fileName)&&(name.lastIndexOf(".trc")!=-1||name.lastIndexOf(".lrc")!=-1||name.lastIndexOf(".txt")!=-1)){
				lyric.add(file.getPath());
			}
		}else if(file.isDirectory()){
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				getPath(file2);
			}
		}
		return lyric;
		
	}
	
}
