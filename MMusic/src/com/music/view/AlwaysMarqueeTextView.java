/**
 * Copyright (c) www.longdw.com
 */
package com.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * 自动跑马灯
 * @author zcs
 *
 *<com.music.view.AlwaysMarqueeTextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:ellipsize="marquee"
            android:focusable="true"
            android:focusableInTouchMode="true"
            android:marqueeRepeatLimit="marquee_forever"
            android:scrollHorizontally="true"
            android:singleLine="true"
            android:textColor="@color/white"
            android:textSize="12sp" />
 */
public class AlwaysMarqueeTextView extends TextView {

	public AlwaysMarqueeTextView(Context context) {
		super(context);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlwaysMarqueeTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean isFocused() {
		return true;
	}
	

}
