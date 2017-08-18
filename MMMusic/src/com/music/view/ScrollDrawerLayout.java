package com.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * @ClassName: UpLayout.java
 * @author zcs
 * @version V1.0
 * @Date 2015年9月24日 下午4:03:10
 * @Description: 滑动升起
 */
public class ScrollDrawerLayout extends LinearLayout {

	public static boolean isScroll = true;// 当前是否可以滚动，否为不拦截字控件的触摸事件
	private GestureDetector detector;
	private int screenWidth;
	private int screenHeight;
	private float downX;
	private float downY;
	private float moveX;
	private float moveY;
	private float upX;
	private float upY;
	private Scroller scroller;
	private boolean isTouch;//是否推动触摸
	public ScrollDrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		WindowManager wm = (WindowManager) this.getContext().getSystemService(
				Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();
		detector = new GestureDetector(getContext(),
				new GestureDetectorListene());
		scroller = new Scroller(getContext());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		scrollTo(0, 0);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
	}


	public boolean isScroll() {
		return isScroll;
	}
	/**
	 * 是否可以滑动
	 * @param isScroll
	 */
	public void setScroll(boolean isScroll) {
		ScrollDrawerLayout.isScroll = isScroll;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			detector.onTouchEvent(event);
			downX = event.getRawX();
			downY = event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			moveX = event.getRawX();
			moveY = event.getRawY();
			float x = downX - event.getRawX();
			float y = downY - event.getRawY();
			
			//如果在不最上，且移动10
			if(Math.abs(moveY - downY) > 5){//不在最上且大于5全拦截
				//Log.i("onInterceptTouchEvent Top","Math.abs(y): Math.abs(x)"+ Math.abs(y) +":"+Math.abs(x));
				return isScroll;
				//如果在最上-----向下滑，IScrollView在最顶，y方向大于x方向
			}else if(Math.abs(y) > Math.abs(x)&&moveY - downY > 5){//上下滑大于左右且大于5
				return isScroll;
			}
			break;
		case MotionEvent.ACTION_UP:

			break;
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//Log.i("touch", event.getAction()+"----------------------");
		detector.onTouchEvent(event);
		isTouch = true;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = event.getRawX();
			downY = event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			float x = downX - event.getRawX();
			float y = downY - event.getRawY();
			if(getScrollY()>0){
				scrollTo(0, 0);
				return super.onTouchEvent(event);
			}
			if (Math.abs(x) > Math.abs(y) && Math.abs(y) > 5) {// Y轴的方法大于X轴，且已经移动5个，里面可以获得左右滑动
				return false;// 不处理
			}
			break;
		case MotionEvent.ACTION_UP:
			upX = event.getRawX();
			upY = event.getRawY();
			isTouch = false;
			updateView();
			break;
		}
		return isScroll;
	}
	private void updateView(){
		int scrollY = Math.abs(getScrollY());//所在屏幕的位置，screenHeight屏幕的高度
		if(screenHeight/2.5>scrollY){
			moveByLocation(0, 0);
		}else{
			moveByLocation(0, screenHeight);
		}
		
	}

	/**
	 * 要移动的位置
	 * 
	 * @param x
	 *            距离
	 * @param y
	 *            距离（正值）
	 */
	public void moveByLocation(float x, final float y) {
		int distanceY = (int) (y + getScrollY());
		// x位置，y位置，x要移动的距离，y要移动的距离,动画时间
		scroller.startScroll(getScrollX(), getScrollY(), (int) x, -distanceY,
				Math.abs(distanceY));
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			int currY = scroller.getCurrY();//速度下的距离
//			System.out.println("速度下的距离   "+currY);
			scrollTo(0, currY);
			invalidate();// 继续刷新页面
		}else{//移动完成
			if(scrollStateListener!=null&&!isTouch){//滑动停止时
				boolean isShow = true;
				if(Math.abs(getScrollY())>=screenHeight-5){
					isShow=false;
				}
				scrollStateListener.scrollEnd(getScrollX(),getScrollY(),isShow);
			}
		}
	}

	class GestureDetectorListene implements GestureDetector.OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			// System.out.println("onDown");
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// System.out.println("onShowPress");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// System.out.println("onSingleTapUp");
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if(getScrollY()>0){
				scrollTo(0, 0);
				return false;
			}else{
				scrollBy(0, (int) distanceY);
			}
			if(scrollStateListener!=null){
				scrollStateListener.scrollState(e1.getRawY(), e2.getRawY());
			}
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

	}
	
	private View titleView;
	private View dataView;
	private ScrollStopStateListener stateListener;
	private ScrollStateListener scrollStateListener;
	
	/**
	 * 添加监听滑动后到达的位置
	 * @param listener
	 */
	public void setOnStopStateListener(ScrollStopStateListener listener){
		this.stateListener = listener;
	}
	public interface ScrollStopStateListener{
		//public void stopScroll(MoveLocation state);
	}
	
	/**
	 * 滑动时的监听
	 * @param listener
	 */
	public void setOnScrollListener(ScrollStateListener listener){
		this.scrollStateListener = listener;
	}
	public interface ScrollStateListener{
		/**
		 * 滑动时监听
		 * @param rawXDown 按下时的位置
		 * @param rawXMove 移动到的位置
		 */
		public void scrollState(float rawXDown,float rawXMove);
		/**
		 * 
		 * @param rawXDown
		 * @param rawXMove
		 * @param b 是否显示
		 */
		public void scrollEnd(float rawXDown,float rawXMove,boolean isShow);
	}
	
	
	
	

}
