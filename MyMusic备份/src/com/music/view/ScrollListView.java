package com.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * @ClassName:     ScrollListView.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2015年12月11日 下午3:41:05 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class ScrollListView extends ListView{

	public ScrollListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		// TODO Auto-generated method stub
//		return super.dispatchTouchEvent(ev);
//	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		System.out.println("MotionEvent             "+ev.getAction());
		if(ev.getAction()==MotionEvent.ACTION_DOWN){
			ScrollDrawerLayout.isScroll=false;
		}else if(ev.getAction()==MotionEvent.ACTION_UP){
			ScrollDrawerLayout.isScroll=true;
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(ev.getAction()==MotionEvent.ACTION_DOWN){
			ScrollDrawerLayout.isScroll=false;
		}else if(ev.getAction()==MotionEvent.ACTION_UP){
			ScrollDrawerLayout.isScroll=true;
		}
		return super.onTouchEvent(ev);
	}
}
