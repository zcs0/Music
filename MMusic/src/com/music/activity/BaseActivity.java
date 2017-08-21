package com.music.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.music.dialog.EProgressDialogs;
import com.music.dialog.EProgressDialogs.EvenDialog;
import com.z.BaseMain;
import com.z.netUtil.HttpRequest;
import com.z.netUtil.HttpResponse;
import com.z.netUtil.HttpUtils;
import com.z.utils.LogUtils;

/**
 * @ClassName: BaseActivity.java
 * @author zcs
 * @version V1.0
 * @Date 2017年3月7日 下午4:05:00 
 * @Description: 所在Activity的父类(onCreateView代替onCreate)
 */
public abstract class BaseActivity extends FragmentActivity implements BaseMain,OnClickListener {
	protected Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String message = (String) msg.obj;
			MessageUtil util = new MessageUtil();
			
		}
	};

	private EProgressDialogs dialog;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		dialog= new EProgressDialogs(this);
		if (arg0 == null) {
			onCreateView(arg0);
			
		}
	}

	protected abstract void onCreateView(Bundle savedInstanceState);

	public int createView(int layoutResID) {
		
		createView(View.inflate(this, layoutResID, null));
		
		return layoutResID;
	}

	/**
	 * 加载布局到当前Activity
	 */
	public View createView(View v) {
		this.setContentView(v);
		return v;
	}


	/**
	 * 设置多个控件点击事件
	 * @param ids
	 */
	protected void setOnClick(int... ids){
		int[] id = ids;
		if(id!=null&&id.length>0){
			for (int i : id) {
				getView(i).setOnClickListener(this);;
			}
		}
	}

	public <T extends View> T setGone(int layoutResId) {
		View view = getView(layoutResId);
		view.setVisibility(View.GONE);
		return (T) view;
	}

	public <T extends View> T setGone(View view) {
		if (view != null) {
			view.setVisibility(View.GONE);
		}
		return (T) view;
	}

	public <T extends View> T setVisible(int layoutResId) {
		View view = getView(layoutResId);
		view.setVisibility(View.VISIBLE);
		return (T) view;
	}

	public <T extends View> T setVisible(View view) {
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		return (T) view;
	}

	public <T extends View> T setInvisible(int layoutResId) {
		View view = getView(layoutResId);
		view.setVisibility(View.INVISIBLE);
		return (T) view;

	}

	public <T extends View> T setInvisible(View view) {
		if (view != null) {
			view.setVisibility(View.INVISIBLE);
		}
		return (T) view;
	}

	@Override
	public void onClick(View v) {

	}

	/**
	 * 根据一个布局查找一个控件
	 * 
	 * @param layoutResID
	 * @param viewId
	 * @return 返回这查找到的控件
	 */
	@SuppressWarnings("unchecked")
	protected <T extends View> T getViewById(int layoutResID, int viewId) {
		View otherV = findViewById(viewId);
		if (otherV == null) {
			otherV = View.inflate(this, layoutResID, null);
		}
		return (T) otherV.findViewById(viewId);
	}

	/**
	 * 根据一个View查找一个控件
	 * 
	 * @param layoutResID
	 * @param viewId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T extends View> T getViewByView(View view, int viewId) {
		return (T) view.findViewById(viewId);
	}

	/**
	 * 得到控件
	 * 
	 * @param layoutId
	 *            要查找的控件id
	 * @return
	 */
	public <T extends View> T getView(int layoutId) {
			return (T) findViewById(layoutId);
	}

	/**
	 * 跳转到指定的Activity
	 * 
	 * @param cls
	 */
	protected void toActivity(Class<? extends Activity> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
	}

	/**
	 * 跳转到指定的Activity
	 * 
	 * @param <T>
	 * @param cls
	 */
	protected void toActivityForResult(Class<? extends Activity> cls,
			Intent intent, int requestCode) {
		// intent = new Intent(this, cls);
		startActivityForResult(intent, requestCode);
	}

	public <T extends View> T setOnClick(int layoutResId) {
		View view = getView(layoutResId);
		view.setOnClickListener(this);
		return (T) view;
	}
	public <T extends View> T findViewById2(int layoutId) {
		return (T) super.findViewById(layoutId);
	}
	@Override
	public <T extends View> T setOnClick(View view) {
		if (view != null) {
			view.setOnClickListener(this);
		}
		return (T) view;
	}
	/**
	 * GET Need to be added Thread,Run in thread
	 * @param request
	 * @return
	 */
	public HttpResponse httpGet(HttpRequest request){
		return HttpUtils.httpGet(request);
	}
	/**
	 * POST Need to be added Thread,Run in thread
	 * @param request
	 * @return
	 */
	public HttpResponse httpPost(HttpRequest request){
		return HttpUtils.httpPost(request);
	}
	/**
	 * POST Need to be added Thread,Run in thread
	 * @param url
	 * @return
	 */
	public HttpResponse httpGet(String url){
		return HttpUtils.httpPost(url);
	}
	/**
	 * POST Need to be added Thread,Run in thread
	 * @param url
	 * @return
	 */
	public HttpResponse httpPost(String url){
		return HttpUtils.httpPost(url);
	}
	/**
	 * 显示加载等待
	 * @param message
	 */
	protected void showLoadingDialog(String... message){
		dialog.even = EvenDialog.style2;//显示的样式
		if(message!=null&&message.length>0){
			dialog.setMessage(message[0]);
		}
		dialog.setCancelable(false);
		dialog.show();
		
	}
	/**
	 * 关闭加载等待
	 */
	protected void dismissLoadingDialog(){
		dialog.dismiss();
	}
	public void netRequest(MessageUtil util){
		
	}
	
	public void setTextViewValue(View view,String str){
		if(TextUtils.isEmpty(str)){
			LogUtils.e("BaseActivity", "not null");
			return;
		}
		if(view instanceof TextView){
			TextView tv = (TextView) view;
			tv.setText(str);
		}else{
			LogUtils.e("BaseActivity", "is not TextView");
		}
		
	}
	public void requestHttp(int arg0, String url, String params){
//		params = EncryptionUtils.string2enc(params);
	}

}
