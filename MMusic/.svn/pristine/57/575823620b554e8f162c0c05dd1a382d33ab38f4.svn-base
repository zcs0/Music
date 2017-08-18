package com.netUtil.ImageUtil;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.music.storage.SPStorage;
import com.netUtil.NetUtil;

/**
 * @ClassName:     ImageLoad.java
 * @author         zcs
 * @version        V2.0  
 * @Date           2015年9月28日 下午1:37:37 
 * @Description:   用于显示图片的类
 */
public class ImageLoad {
	
	private Context context;
	private View view;//显示图片的View
	private String urlStr;//图片所在位置
	private Bitmap bitmap;
	private String defautlImg;//默认显示图片
	private boolean isCache;//是否开启缓存
	private long maxByteSize=1024*1024*10;//缓存大小
	private DiskLruCache cacheManager;//管理缓存
	private String cachePath=Environment.getExternalStorageDirectory()+"/"+"Download/";//缓存路径
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			int code = msg.what;
			switch (code) {
			case 200:
				if(msg.obj!=null){
					Bitmap bmp = (Bitmap) msg.obj;
//					ImageView iv = (ImageView) view;
//					iv.setImageBitmap(bmp);
					setImage(view,bmp);
				}
				break;
			default:
				break;
			}
			
		};
	};

	/**
	 * 图片显示，
	 * @param context
	 */
	public ImageLoad(Context context){
		this.context = context;
		
	}
	
	/**
	 * 
	 * @param view 要显示的view
	 * @param url 网络路径
	 * @param defauleImg 默认图片(本地图片)
	 */
	public void load(View view,String url,String defauleImg){
		this.defautlImg = defauleImg;
		load(view, defauleImg);
		load(view, url);
	}
	/**
	 * 
	 * @param view 要显示的view
	 * @param url 网络路径
	 * @param bitmap 默认图片(本地图片)
	 */
	public void load(View view,String url,Bitmap bitmap){
		setImage(view, bitmap);
		load(view, url);
	}
	/**
	 * 
	 * @param view 要显示的view
	 * @param url 网络路径
	 * @param bitmap 默认图片(本地图片)
	 * @param listener 监听下载完成
	 */
	public void load(View view,String url,Bitmap bitmap,ImageLoadListener listener){
		this.listener = listener;
		setImage(view, bitmap);
		load(view, url);
	}
	/**
	 * 
	 * @param view 要显示的view
	 * @param url 网络路径
	 * @param listener 监听下载完成
	 */
	public void load(View view,String url,ImageLoadListener listener){
		this.listener = listener;
		load(view, url);
	}
	
	/**
	 * 
	 * @param view 要显示的view
	 * @param url 网络路径
	 * @param defauleImg 默认图片(本地图片)
	 * @param listener 监听下载完成
	 */
	public void load(View view,String url,String defauleImg,ImageLoadListener listener){
		this.listener = listener;
		this.defautlImg = defauleImg;
		load(view, defauleImg);
		load(view, url);
	}
	
	/**
	 * 
	 * @param view 要显示的view
	 * @param url 网络路径
	 */
	public void load(View view,String url){
		this.urlStr = url;
		this.view = view;
		if(view==null||url==null){
			return;
		}
		if(isCache){//使用缓存
			bitmap = cacheManager.get(url);//从缓存中获取
			if(bitmap!=null){
				setImage(view, bitmap);
			}else if(url.startsWith("http")){//从网络中获取
				downLoadImage(url);
			}else{							
				if(new File(url).isFile()){//本地获取
					setImage(view, url);
				}else{
					Log.e(this.getClass().toString(), "失败："+url);
				}
			}
		}else if(new File(url).isFile()){//本地获取
			setImage(view, url);
		}else if(url.startsWith("http")){//网络加载
			downLoadImage(url);
		}else{
			Log.e(this.getClass().toString(), "失败："+url);
		}
	}
	/**
	 * 加载本地图片
	 * @param view
	 * @param path
	 */
	private void setImage(View view, String path){
		if(view==null||path==null){
			return;
		}
		if(view.getWidth()==0&&view.getHeight()==0){//如果是包裹
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			Display defaultDisplay = wm.getDefaultDisplay();
			int WinWidth = defaultDisplay.getWidth();
			int WinHeight = defaultDisplay.getHeight();
			BitmapFactory.Options options = new Options();
			options.inJustDecodeBounds = true;// 只解析头文件
			BitmapFactory.decodeFile(path, options);
			//图片的高和宽
			int height = options.outHeight;
			int width = options.outWidth;
			
			//用于计算最大的缩放的比例
			int scaleX = height / WinHeight;
			int scaleY = width / WinWidth;
			int scale = scaleX>scaleY?scaleX:scaleY;
			options.inSampleSize = scale;
			options.inJustDecodeBounds = false;
			Bitmap decodeFile = BitmapFactory.decodeFile(path,options);
			setImage(view, decodeFile);
		}else{
			Bitmap decodeFile = BitmapFactory.decodeFile(path);
			setImage(view, decodeFile);
		}
	}
	
	
	/**
	 * 得到bimat设置到View中
	 * @param view
	 * @param bmp
	 */
	public void setImage(View view,Bitmap bmp){
		if(view==null||bmp==null){
			Log.e(this.getClass().toString(), "view is null or bmp is null");
			return;
		}
//		view.setAnimation(getInAlphaAnimation(2000));
		if(view instanceof ImageView){
			ImageView iv = (ImageView) view;
			iv.setVisibility(View.GONE);
			iv.setAnimation(getInAlphaAnimation(300));
			bmp = scaleImage(bmp, view.getWidth(), view.getHeight());
			iv.setImageBitmap(bmp);
			iv.setVisibility(View.VISIBLE);
		}else{
			bmp = scaleImage(bmp, view.getWidth(), view.getHeight());
			view.setBackgroundDrawable(new BitmapDrawable(bmp));
		}
		
	}
	
	/**
	 * 当前图片
	 * @return
	 */
	public Bitmap getImg(){
		return bitmap;
	}
	
	/**
	 * 根据key获得图片
	 * @param key
	 * @return null：未获得
	 */
	public Bitmap getImgByName(String key){
		return cacheManager.get(key);
	}
	public boolean getBoolCache() {
		return isCache;
	}
	
	/**
	 * 缓存管理
	 * @return
	 */
	public DiskLruCache getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(DiskLruCache cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * 获得设置的缓存大小
	 * @return
	 */
	public long getMaxByteSize() {
		return maxByteSize;
	}
	/**
	 * 设置缓存大小
	 * @param maxByteSize
	 */
	public void setMaxByteSize(long maxByteSize) {
		this.maxByteSize = maxByteSize;
	}
	/**
	 * 是否缓存
	 * @param isCache
	 */
	public void setBoolCache(boolean isCache) {
		this.isCache = isCache;
		cacheManager = cacheManager!=null?cacheManager:DiskLruCache.openCache(context, new File(cachePath), maxByteSize);
	}

	/**
	 * 获得缓存路径
	 */
	public String getCachePath() {
		return cachePath;
	}

	/**
	 * 设置缓存路径
	 * @param cachePath
	 */
	public void setCachePath(String cachePath) {
		if(!this.cachePath.equals(cachePath)){
			cacheManager = DiskLruCache.openCache(context, new File(cachePath), maxByteSize);
		}
		this.cachePath = cachePath;
		cacheManager = cacheManager!=null?cacheManager:DiskLruCache.openCache(context, new File(cachePath), maxByteSize);
	}

	/**
	 * 显示图片动画
	 * @param durationMillis
	 * @return
	 */
	private static AlphaAnimation getInAlphaAnimation(long durationMillis) {
        AlphaAnimation inAlphaAnimation = new AlphaAnimation(0.6f, 1);
        inAlphaAnimation.setDuration(durationMillis);
        return inAlphaAnimation;
    }
	/**
	 * 
	 * @param bitmmap
	 * @param scaleWidth 缩放宽度
	 * @param scaleHeight 缩放高度
	 * @return
	 */
	private static Bitmap scaleImage(Bitmap bitmmap, float scaleWidth, float scaleHeight) {
        if (bitmmap == null||(scaleWidth==0&&scaleWidth==scaleHeight)) {
            return bitmmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth/bitmmap.getWidth(), scaleHeight/bitmmap.getHeight());
        if(scaleWidth==0||scaleHeight==0){
        	return bitmmap;
        }
        
        return Bitmap.createBitmap(bitmmap, 0, 0, bitmmap.getWidth(), bitmmap.getHeight(), matrix, true);
    }
	
	
	private void downLoadImage(final String urlStr){
		SPStorage mSp = new SPStorage(context);
		if(mSp.getFilterWifi()&&!NetUtil.getIsWifi(context)){//是只在wifi时才可下载，如果tru,且当前为其它网络
			System.out.println("ImageLoad----->只在wifi状态时下载");
			return;
		}
		if (TextUtils.isEmpty(urlStr)||!NetUtil.getNetOpen(context)) {//路径无效，或无网
			return;
		}
		new Thread(){
			public void run() {
				try {
					URL url = new URL(urlStr);
		            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		            if (conn.getResponseCode()==200) {
		                conn.setReadTimeout(300);
		                InputStream inputStream = conn.getInputStream();
			            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
						Message msg = new Message();
						msg.obj =bmp;
						msg.what = 200;
						if(isCache){//是否缓存
							if(cacheManager.get(urlStr)==null){
								cacheManager.put(urlStr, bmp);
							}
						}
						handler.sendMessage(msg);
						inputStream.close();
		            }
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			
		}.start();
		
	}
	private ImageLoadListener listener;
	interface ImageLoadListener{
		public void onSuccess(Bitmap bimap);
		
		
	}

}
