package com.music.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.music.dialog.EProgressDialogs;
import com.music.dialog.EProgressDialogs.EvenDialog;
import com.z.BaseMain;

/**
 * v4下的Frament第二级别 使用frament时可以继承于此
 * 
 * @author ZCS
 *
 */
public abstract class BaseFragment extends Fragment implements
		View.OnClickListener, BaseMain {
	protected View view;
	protected Context context = getActivity();
//	private int time_sys_what = ++SystemConstants.handlerMessageBasis;
//	protected ArticleData dataSource;
	private EProgressDialogs dialog;
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			dismissLoadingDialog();
			MessageUtil util = new MessageUtil();
			String message = (String) msg.obj;
			util.setMessage(message);
			util.setWhat(msg.what);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		dialog= new EProgressDialogs(context);
		view = View.inflate(getActivity(), createView(), null);
		initView(savedInstanceState,view);
		return view;
	}

	/**
	 * 此frament的布局
	 * 
	 * @return
	 */
	public abstract int createView();


	public abstract void initView(Bundle bundle,View view);

	/*
	 * public <T extends View> T getView(int layoutId) { if (view != null) {
	 * return (T) view.findViewById(layoutId); } else { Log.e(TAG,
	 * "获得此控件失败（id无效），"); return null; } }
	 */

	@Override
	public <T extends View> T setOnClick(View view) {
		if (view != null) {
			view.setOnClickListener(this);
		}
		return (T) view;
	}

	@Override
	public <T extends View> T setOnClick(int layoutResId) {
		View view = getView(layoutResId);
		view.setOnClickListener(this);
		return (T) view;
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
		View otherV = view.findViewById(viewId);
		if (otherV == null) {
			otherV = View.inflate(getActivity(), layoutResID, null);
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
	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int layoutId) {
		return (T) view.findViewById(layoutId);
	}

	@Override
	public int createView(int layoutResID) {
		// TODO Auto-generated method stub
		return layoutResID;
	}

	@Override
	public View createView(View v) {
		// TODO Auto-generated method stub
		return v;
	}
	public <T extends View> T findViewById(int layoutId) {
		return (T) view.findViewById(layoutId);
	}
	protected int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

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
	public void requestHttp(int arg0, String url, String params){
		requestHttp(arg0, url, params, true);
	}
	public void requestHttp(int arg0, String url, String params,boolean isShowLoading){
//		if(isShowLoading){
//			showLoadingDialog("");
//		}
//		String param = params;
//		try {
//			params = HttpParameUtils.encode(params);
//			LogUtils.w("BaseActivity", "\n what->"+arg0+" "+url+"  加密前-->"+param+"\n 加密后-->"+params);
//			ThreadUtil.thread(arg0, url, params, mHandler,5);//出现异常，请求10次
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}
	
	
	
}
