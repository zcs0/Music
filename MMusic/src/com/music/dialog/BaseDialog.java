package com.music.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.music.R;

/**
 * @author zcs
 * @version V2.0
 * @ClassName: FirstDialog.java
 * @Date 2015年11月18日 下午5:25:27
 * @Description: 弹出默认的dialog
 */
public class BaseDialog extends IDialog {
	private CharSequence mPositiveButtonText;
	private OnClickListener mPositiveButtonListener;
	private CharSequence mNegativeButtonText;
	private OnClickListener mNegativeButtonListener;
	private CharSequence mMiddleButtonText;
	private OnClickListener mMiddleButtonListener;
	public BaseDialog(Context context) {
		super(context);
		setContentView(R.layout.dialog_default);
		Window dialogWindow = getWindow();
//		WindowManager m = getActivity().getWindowManager();
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display d = wm.getDefaultDisplay(); // 获取屏幕宽、高度
//		WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//		p.height = (int) (d.getHeight() * 0.9); // 高度设置为屏幕的0.6，根据实际情况调整
//		p.width = (int) (d.getWidth() * 0.5); // 宽度设置为屏幕的0.65，根据实际情况调整
//		dialogWindow.setAttributes(p);
		setWidthHeight((int)(d.getWidth() * 0.7), (int)(d.getHeight() * 0.5));
	}

     public void setWidthHeight(int width,int height){
    	 getWindow().setLayout(width,height);
     }
	/**
	 * 设置标题文字
	 */
	@Override
	public void setTitle(CharSequence string) {
		findViewById(R.id.ll_title).setVisibility(View.VISIBLE);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(TextUtils.isEmpty(string) ? "" : string);
	}
	/**
	 * 设置标题文字颜色
	 */
	public void setTitleColor(int color) {
		findViewById(R.id.ll_title).setVisibility(View.VISIBLE);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setTextColor(color);
	}
	/**
	 * 调置标题大小
	 */
	public void setTitleSize(float size) {
		findViewById(R.id.ll_title).setVisibility(View.VISIBLE);
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setTextSize(size);
	}

	/**
	 * 确认按钮
	 * 
	 * @param string
	 */
	public void setConfirm(CharSequence string) {
		mPositiveButtonText = string;
		TextView btn = (TextView) findViewById(R.id.btn_fm_confirm);
		showBottomView();
		btn.setText(TextUtils.isEmpty(string) ? "确定" : string);
	}
	/**
	 * 确认按钮
	 * 
	 * @param colors
	 */
	public void setConfirmTxtColor(int colors) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_confirm);
		btn.setTextColor(colors);
	}
	/**
	 * 确认按钮字体大小
	 * @param size
	 */
	public void setConfirmSize(float size) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_confirm);
		btn.setTextSize(size);
	}

	/**
	 * 取消按钮
	 * @param string
	 */
	public void setCancel(CharSequence string) {
		mNegativeButtonText = string;
		TextView btn = (TextView) findViewById(R.id.btn_fm_cancel);
		btn.setText(TextUtils.isEmpty(string) ? "取消" : string);
		showBottomView();
		btn.setVisibility(View.VISIBLE);
	}
	/**
	 * 取消按钮字体颜色
	 * @param color
	 */
	public void setCancelTxtColor(int  color) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_cancel);
		btn.setTextColor(color);
	}
	/**
	 * 取消按钮是否可点
	 * @param bl
	 */
	public void setCancelIsCanClick(Boolean  bl) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_cancel);
		btn.setEnabled(bl);
	}
	/**
	 * 取消按钮字体大小
	 * @param size
	 */
	public void setCancelSize(float size) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_cancel);
		btn.setTextSize(size);
	}
	/**
	 * 中间按钮
	 * @param string
	 */
	public void setMiddle(CharSequence string) {
		mNegativeButtonText = string;
		TextView btn = (TextView) findViewById(R.id.btn_fm_middle);
		btn.setText(TextUtils.isEmpty(string) ? "中间" : string);
		showBottomView();
		btn.setVisibility(View.VISIBLE);
	}
	/**
	 * 中间按钮字体颜色
	 * @param color
	 */
	public void setMiddleTxtColor(int  color) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_middle);
		btn.setTextColor(color);
	}
	/**
	 * 中间按钮是否可点
	 * @param bl
	 */
	public void setMiddleIsCanClick(Boolean  bl) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_middle);
		btn.setEnabled(bl);
	}
	/**
	 * 中间按钮字体大小
	 * @param size
	 */
	public void setMiddleSize(float size) {
		TextView btn = (TextView) findViewById(R.id.btn_fm_middle);
		btn.setTextSize(size);
	}

	/**
	 * 设置中间提示信息
	 * 
	 * @param string
	 */
	public void setContentTxt(CharSequence string) {
		TextView tv = (TextView) findViewById(R.id.tv_content);
		tv.setText(TextUtils.isEmpty(string) ? "" : string);
		findViewById(R.id.rl_content_view).setVisibility(View.VISIBLE);
		
	}
	/**
	 * 设置中间提示信息
	 * 
	 * @param string
	 */
	public void setMessage(CharSequence string) {
		setContentTxt(string);
	}
	/**
	 * 设置中间提示颜色
	 * 
	 * @param color
	 */
	public void setMessageColor(int color) {
		TextView tv = (TextView) findViewById(R.id.tv_content);
		tv.setTextColor(color);
	}
	/**
	 * 设置中间提示文字
	 * 
	 * @param txtId
	 */
	public void setMessage(int txtId) {
		String txt = context.getResources().getString(txtId);
		TextView tv = (TextView) findViewById(R.id.tv_content);
		tv.setText(txt);
	}
	/**
	 * 设置中间显示的内容
	 */
	public void setMiddleView(View view){
		ViewGroup rl = (ViewGroup) findViewById(R.id.rl_content_view);
		rl.removeAllViews();
		if(view!=null)
			rl.addView(view);
		rl.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 设置中间显示的内容
	 */
	public View setMiddleView(int layoutId){
		ViewGroup rl = (ViewGroup) findViewById(R.id.rl_content_view);
		rl.removeAllViews();
		View inflate = View.inflate(getContext(), layoutId, rl);
		return rl.getChildAt(0);
	}
	/**
	 * 确定按钮 点击事件
	 * 
	 * @param click
	 */
	public Dialog setConfirmListener(OnClickListener click) {
		return setPositiveButton(mPositiveButtonText, click);

	}
	/**
	 * 取消按钮 点击事件
	 * 
	 * @param click
	 */
	public Dialog setCancelListener(OnClickListener click) {
		return setNegativeButton(mNegativeButtonText,click);
	}
	/**
	 * 取消按钮 点击事件
	 * 
	 * @param click
	 */
	public Dialog setMiddleListener(OnClickListener click) {
		return setMiddleButton(mMiddleButtonText,click);
	}
	/**
	 * 中间按钮 是否可点
	 * 
	 * @param bl
	 */
	public void setMiddleButtonIsCanClick(Boolean bl){
		TextView btn_fm_confirm = (TextView)findViewById(R.id.btn_fm_middle);
		btn_fm_confirm.setEnabled(bl);
	}
	public Dialog setMiddleButton(CharSequence text,OnClickListener listener){
		 mMiddleButtonText = text;
         mMiddleButtonListener = listener;
         setMiddle(text); 
         findViewById(R.id.middle_view).setVisibility(View.VISIBLE);
         TextView btn_fm_middle = (TextView)findViewById(R.id.btn_fm_middle);
         btn_fm_middle.setVisibility(View.VISIBLE);
        
         return this;
	}
	public Dialog setMiddleButton(int txtId, final OnClickListener listener){
		String text = context.getResources().getString(txtId);
		return setMiddleButton(text, listener);
	}
	/**
	 * 确认按钮 是否可点
	 * 
	 * @param bl
	 */
	public void setPositiveButtonIsCanClick(Boolean bl){
		TextView btn_fm_confirm = (TextView)findViewById(R.id.btn_fm_confirm);
		btn_fm_confirm.setEnabled(bl);
	}
	/**
	 * 设置确认
	 * @param text
	 * @param listener
	 * @return
	 */
	public Dialog setPositiveButton(CharSequence text,OnClickListener listener){
		 mPositiveButtonText = text;
         mPositiveButtonListener = listener;
         setConfirm(text);
         return this;
	}
	public Dialog setPositiveButton(int txtId, final OnClickListener listener){
		String text = context.getResources().getString(txtId);
		return setPositiveButton(text, listener);
	}
	public Dialog setNegativeButton(CharSequence text, OnClickListener listener){
		mNegativeButtonText = text;
        mNegativeButtonListener = listener;
        setCancel(text);
        return this;
    }

    public Dialog setNegativeButton(int txtId, final OnClickListener listener) {
        String text = context.getResources().getString(txtId);
        return setNegativeButton(text, listener);
    }

    public interface OnClickListener {
        /**
         * This method will be invoked when a button in the dialog is clicked.
         *
         * @param dialog The dialog that received the click.
         * @param which  The button that was clicked (e.g.
         *               {@link DialogInterface#BUTTON1}) or the position
         *               of the item clicked.
         */
        /* TODO: Change to use BUTTON_POSITIVE after API council */
        public void onClick(Dialog dialog, int which);
    }

    private void showBottomView() {
        findViewById(R.id.v_divice).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_bottom_btn).setVisibility(View.VISIBLE);
    }
    @Override
    public void show() {
    	super.show();
    	View view = findViewById(R.id.btn_fm_confirm);
    	if(view!=null)
    		view.setOnClickListener(new View.OnClickListener() {
			
				@Override
				public void onClick(View v) {
					if(mPositiveButtonListener!=null){
						mPositiveButtonListener.onClick(BaseDialog.this, 2);
					}else{
						dismiss();
					}
				}
			});
    	view = findViewById(R.id.btn_fm_cancel);
    	if(view!=null)
    	view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mNegativeButtonListener != null) {
                    mNegativeButtonListener.onClick(BaseDialog.this, 1);
                } else {
                    dismiss();
                }
            }
        });
    	
    	view = findViewById(R.id.btn_fm_middle);
    	if(view!=null)
    	view.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(mMiddleButtonListener!=null){
 					mMiddleButtonListener.onClick(BaseDialog.this, 3);
 				}else{
 					dismiss();
 				}
 			}
 		});
    }

    public void setNegativeView(int View) {
        findViewById(R.id.btn_fm_cancel).setVisibility(View);
        findViewById(R.id.view_dialog).setVisibility(View);
    }
    /**
     * 
     * @param view
     * @param b true:修改自定义的View
     */
    public void setContentView(View view,boolean b) {
    	if(b){
    		setMiddleView(view);
    	}else{
    		super.setContentView(view);
    		
    	}
    }
    /**
     * 中间字体显示格式Gravity
     * @param gravity
     */
    public void setCenterGravity(int gravity){
    	TextView tv = (TextView) findViewById(R.id.tv_content);
    	if(tv!=null)
    		tv.setGravity(gravity);
    }

}
