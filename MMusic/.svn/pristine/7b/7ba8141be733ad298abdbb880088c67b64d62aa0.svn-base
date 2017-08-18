package com.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * @ClassName:     TestView.java
 * @author         zcs
 * @version        V1.0  
 * @Date           2017年8月18日 下午2:45:30 
 * @Description:   TODO(用一句话描述该文件做什么) 
 */
public class TestView extends TextView{

	public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public TestView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public TestView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		String str = "用一句话描述该文件做什么";
		Paint paintD = new Paint();
		paintD.setAntiAlias(true);                           //设置画笔为无锯齿
		paintD.setColor(Color.WHITE);                        //设置画笔颜色
		paintD.setTextSize((float) 30.0); 
		Paint paintReg = new Paint();
		paintReg.setAntiAlias(true);                           //设置画笔为无锯齿
		paintReg.setColor(Color.RED);                        //设置画笔颜色
		paintReg.setTextSize((float) 30.0); 
		float measureText = paintD.measureText(str);
		canvas.drawText(str, 0, 30, paintD);
		canvas.clipRect(100, 0, measureText, measureText-200, Region.Op.INTERSECT);//设置显示范围
		canvas.drawText(str, 0, 30, paintReg);
		
		
		
        
//		Paint paint = new Paint();
//		paint.setAntiAlias(true);                           //设置画笔为无锯齿
//        paint.setColor(Color.BLACK);                        //设置画笔颜色
//        paint.setTextSize((float) 30.0); 
//		canvas.clipRect(100, 100, 350, 600, Region.Op.INTERSECT);//设置显示范围
//        canvas.drawColor(Color.RED);
//        canvas.drawCircle(100,100,100,paint);
        
        
        
		super.onDraw(canvas);
	}
}
