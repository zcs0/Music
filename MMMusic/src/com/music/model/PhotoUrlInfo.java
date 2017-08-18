package com.music.model;

import java.util.List;

/**
 * @ClassName:     PhotoUrlInfo.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月26日 下午1:30:17 
 * @Description:   图片信息
 */
public class PhotoUrlInfo {
	public List<Photo> data;
	public String code;
	public String ttl;
	public class Photo{
		public String name;
		public String _id;
		public List<PicUrl> picUrls;
	}
	public class PicUrl{
		public String picUrl;
		public int height;
		public String quality;
		public int width;
		public String _id;
	}
	
	
}
