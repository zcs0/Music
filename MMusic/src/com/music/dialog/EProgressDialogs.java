package com.music.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.R;

public class EProgressDialogs extends Dialog {
	public EvenDialog even = EvenDialog.style1;
	private View view;
	private String message;

	public static enum EvenDialog {
		style1, style2
	}

	public EProgressDialogs(Context context) {
		super(context, R.style.dialog_style);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void setMessage(String message) {
		this.message = message;

	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	@Override
	public void show() {
		switch (even) {
		case style1:// 多张图片
			setContentView(R.layout.people_widget_loadings);
			ImageView rocketImage = (ImageView) findViewById(R.id.loading);
			AnimationDrawable rocketAnimation = (AnimationDrawable) rocketImage
					.getBackground();
			rocketAnimation.start();
			break;
		case style2:// 一张图片
			// view = View.inflate(getContext(), R.layout.loading_dialog2,
			// null);
			setContentView(R.layout.loading_dialog2);
			TextView loading_txt = (TextView) findViewById(R.id.loading_txt);
			if (message!=null&&loading_txt!=null) {
				loading_txt.setText(message);
				loading_txt.setVisibility(View.VISIBLE);
			}else if(loading_txt!=null){
				loading_txt.setVisibility(View.GONE);
			}
			break;
		}
		super.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			super.dismiss();
			return true;
		} else
			return super.onKeyDown(keyCode, event);
	}
}