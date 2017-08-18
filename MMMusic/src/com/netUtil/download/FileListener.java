package com.netUtil.download;

/**
 * @ClassName:     FileListener.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年9月30日 上午9:46:38 
 * @Description:   下载监听
 */
public interface FileListener {
	/**
	 * 下载完成
	 */
	public void onSuccess(long fileSize,String filePath);
}
