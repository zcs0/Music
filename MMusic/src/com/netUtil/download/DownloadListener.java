package com.netUtil.download;
/**
 * @ClassName:     DownloadListener.java
 * @author         zcs
 * @version        V2.0  
 * @Date           2015年9月23日 上午11:21:02 
 * @Description:   文件下载时的监听
 */
public interface DownloadListener extends FileListener{
	/**
	 * 开始下载
	 * @param start
	 * @param end
	 */
	public void onStart(long start,long end);
	/**
	 * 下载失败
	 * @param start 
	 * @param end
	 * @param error
	 */
	public void onFailure(long start,long end,String error);
	
	/**
	 * 正在下载中
	 * @param start
	 * @param end
	 * @param size 千分之一
	 */
	public void onLoading(long start,long end,int size);
	
	/**
	 * 下载出错
	 * @param msg
	 */
	public void error(String msg);
	/**
	 * 暂停下载
	 * @param start
	 * @param end
	 */
	public void onStop(long start,long end);
	
	/**
	 * 下载结束，但不一定下载成功
	 * @param msg
	 */
	public void onFinish(String msg);
	
}
