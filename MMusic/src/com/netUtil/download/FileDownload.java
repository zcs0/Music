package com.netUtil.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * @ClassName: FileDownLoad.java
 * @author zcs
 * @version V2.0
 * @Date 2015年9月22日 下午2:26:49
 * @Description: 文件下载，可断点下载
 */
public class FileDownload {
	private  boolean breakDown = true;// 是否是断点下载
	private  boolean stopDown = false;// 暂停下载
	private static final int cost = 2;
	private static final int SUCCESS = cost + 1;// 完成
	private static final int ERROR = cost + 0;// 错误
	protected static final int NET_ERROR = cost + 2;// 网络有错误
	private static final int DOWNUP_FINISH = cost + 3;// 结束
	private static final int DOWNUP_LOADING = cost + 4;// 正在下载
	private static final int DOWNUP_STOP = cost + 5;// 停止/暂停
	private static final int DOWNUP_START = cost + 6;// 开始下载/得到文件大小
	private DownloadListener downListener;// 文件下载的监听
	// private SharedPreferences sp;// 保存文件下载的进度
	private int netRequestTime = 800;// 连接网络超时
	private String error = "成功下载";// 下载时的错误信息
	private Context contexxt;
	/**
	 * 下载路径url
	 */
	private String urlPath;
	/**
	 * 下载到本地路径
	 */
	private String downloadPath = Environment.getExternalStorageDirectory()
			+ "/";
	public FileDownload(Context contexxt) {
		this.contexxt = contexxt;
	}
	public FileDownload(String downPath, Context contexxt) {
		this.contexxt = contexxt;
		downloadPath = downPath + "Download/";
	}
	
	public DownloadListener getDownListener() {
		return downListener;
	}
	public void setDownListener(DownloadListener downListener) {
		this.downListener = downListener;
	}
	public String getDownloadPath() {
		return downloadPath;
	}
	
	public int getNetRequestTime() {
		return netRequestTime;
	}
	public void setNetRequestTime(int netRequestTime) {
		this.netRequestTime = netRequestTime;
	}
	/**
	 * 设置下载本地和路径
	 * @param downloadPath
	 */
	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}
	/**
	 * 开始下载
	 * 
	 * @param url
	 *            url路径
	 */
	public void start(final String urlPath) {
		this.urlPath = urlPath;
		stopDown = false;
		File file = new File(downloadPath);
		if (!file.isDirectory()) {
			file.mkdirs();
		}

		new Thread() {
			public void run() {
				FileSize fileMsg = new FileSize();
				String path = downloadPath + getFileName(urlPath);
				long start = 0;
				if (new File(path).isFile()) {
					start = new File(path).length();
				}
				try {
					URL url = new URL(urlPath);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(netRequestTime);// 请求最长时间
					int code = conn.getResponseCode();
					if (code == 200) {
						long len = conn.getContentLength();// 文件长度
						start = start == len ? 0 : start;// 如果已下载完成，下载新的
						start = breakDown ? start : 0;// 如果是断点下载
						new Thread(new DownLoadThread(start, len)).start();
						Message msg = new Message();
						msg.what = DOWNUP_START;
						fileMsg.start = start;
						fileMsg.end = len;
						msg.obj = fileMsg;
						handler.sendMessage(msg);
					} else {
						Message msg = new Message();
						error = "网络连接code" + code;
						fileMsg.msg = error;
						msg.what = NET_ERROR;
						msg.obj = fileMsg;
						handler.sendMessage(msg);
					}
				} catch (Exception e) {
					// Log.v("download", FileDownload.this + "..download出错");
					Message msg = new Message();
					msg.what = NET_ERROR;
					error = e.toString();
					fileMsg.msg = error;
					msg.obj = fileMsg;
					handler.sendMessage(msg);
					e.printStackTrace();
				}
			};
		}.start();
	}

	private class DownLoadThread implements Runnable {

		long start = 0;
		long end = 0;

		public DownLoadThread(long start, long end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			Message msg = new Message();
			FileSize fileMsg = new FileSize();
			fileMsg.start = start;// ga
			fileMsg.end = end;
			try {
				URL url = new URL(urlPath);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(500);
				conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
				RandomAccessFile raf = new RandomAccessFile(downloadPath
						+ getFileName(urlPath), "rw");
				raf.seek(start);// 存放到文件的起始位置
				InputStream is = conn.getInputStream();
				byte[] b = new byte[1024 * 2];
				int len = 0;
				while ((len = is.read(b)) != -1) {
					if (stopDown) {// 暂停下载
						return;
					}
					raf.write(b, 0, len);
					start += len;
					fileMsg.start = start;
					msg = new Message();
					msg.obj = fileMsg;
					msg.what = DOWNUP_LOADING;
					handler.sendMessage(msg);
				}
				is.close();
				raf.close();
			} catch (Exception e) {
				error = e.toString();
				msg = new Message();
				msg.what = ERROR;
				fileMsg.msg = error;
				msg.obj = fileMsg;
				handler.sendMessage(msg);
				e.printStackTrace();
			} finally {
				msg = new Message();
				if (stopDown) {// 暂停下载
					msg.what = DOWNUP_STOP;
				} else if (end == start) {// 成功下载
					msg.what = SUCCESS;
				} else {// 下载结束，但不一定下载成功
					msg.what = DOWNUP_FINISH;
				}
				fileMsg.msg = error;
				fileMsg.end = end;
				msg.obj = fileMsg;
				handler.sendMessage(msg);

			}
		}
	}

	/**
	 * 设置断点下载，默认true
	 * 
	 * @param breakDow
	 */
	public void setLoadingMethod(boolean breakDow) {
		this.breakDown = breakDow;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			FileSize size = msg.obj != null ? (FileSize) msg.obj
					: new FileSize();
			if (downListener == null) {// 没有下载监听
				return;
			}
			switch (msg.what) {
			case SUCCESS:// 完成
				// break;
			case DOWNUP_FINISH:// 结束
				downListener.onFinish(size.msg);
				downListener.onSuccess(size.end, downloadPath);
				break;
			case ERROR:// 错误
			case NET_ERROR:// 网络有错误
				downListener.error(size.msg);
				break;
			case DOWNUP_LOADING:// 正在下载
				float f = Float.valueOf(size.start);
				int s = size.start == 0 || size.end == 0 ? 0
						: (int) ((f / size.end) * 1000);
				downListener.onLoading(size.start, size.end, s);
				break;
			case DOWNUP_STOP:// 停止/暂停
				downListener.onStop(size.start, size.end);
				break;
			case DOWNUP_START:// 开始下载/得到文件大小
				downListener.onStart(size.start, size.end);
				break;

			}
		};
	};

	/**
	 * 保存文件下的开始位置和结束位置
	 * 
	 * @author Administrator
	 *
	 */
	class FileSize {
		long start;
		long end;
		String msg;

	}

	/**
	 * 文件的名字
	 * 
	 * @param path
	 *            url路径
	 * @return 只有名字和扩展名
	 */
	private static String getFileName(String path) {
		String name = path.substring(path.lastIndexOf('/') + 1);
		return name;

	}
	
	public boolean isStopDown() {
		return stopDown;
	}
	/**
	 * 暂停下载
	 * @param stopDown
	 */
	public void setStopDown(boolean stopDown) {
		this.stopDown = stopDown;
	}
	public boolean getBreakDown() {
		return breakDown;
	}
	/**
	 * 是否断点下载
	 * @param breakDown
	 */
	public void setBreakDown(boolean breakDown) {
		this.breakDown = breakDown;
	}
	

}
