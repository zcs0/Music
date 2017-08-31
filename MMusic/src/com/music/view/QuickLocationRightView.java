package com.music.view;

//Download by http://www.codefans.net
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.music.R;

/**
 * 右侧字母表，快速定位
 *
 * @author zuo
 * @email 782287169@qq.com
 */
public class QuickLocationRightView extends View {

	int mBackgroundColor      =0x10000000;
	int mBackgroundSelectColor=0x08000000;
	boolean mleftFillet       ;
	boolean mRightFillet       ;
	int mFillet          =0;
	int mTextColor            =0x00000000;
	int mTextSelectColor      =0xffff0000;
	int mTextSize             =20;
	int mTextSelectSize       =40;
//	private String[] b = null;
	private Handler handler;
	int choose = -1;
	Paint paint = new Paint();
	boolean showBkg = false;
	private Context mContext;
	private TextView txtOverlay;
	private DisapearThread disapearThread;
	private TextView title;
	private Map<String,String> map;
	private List<String> stringArr;
	private ListView listView;
	private List<String> quickList;//要右侧显示的列表

	public QuickLocationRightView(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
		TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.QuickView, defStyle, 0);
		init(context, attributesArray);
	}

	public QuickLocationRightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.QuickView);
		init(context, attributesArray);
	}

	public QuickLocationRightView(Context context) {
		super(context);
	}
	private void init(Context cxt,TypedArray attributesArray){
		this.mContext = cxt;
//		int QuickView_backgroundColor = attributesArray.getColor(R.styleable.QuickView_backgroundColor, 0);
		mBackgroundColor      =attributesArray.getColor(R.styleable.QuickView_backgroundColor, mBackgroundColor);
		mBackgroundSelectColor=attributesArray.getColor(R.styleable.QuickView_backgroundSelectColor, mBackgroundSelectColor);
		mleftFillet              =attributesArray.getBoolean(R.styleable.QuickView_leftFillet, false);
		mRightFillet             =attributesArray.getBoolean(R.styleable.QuickView_rightFillet, false);
		mFillet             =attributesArray.getDimensionPixelSize(R.styleable.QuickView_fillet, mFillet);
		mTextColor            =attributesArray.getColor(R.styleable.QuickView_textColorQ, mTextColor);
		mTextSelectColor      =attributesArray.getColor(R.styleable.QuickView_textSelectColor, mTextSelectColor);
		mTextSize             =attributesArray.getDimensionPixelSize(R.styleable.QuickView_textSizeQ, mTextSize);
		mTextSelectSize       =attributesArray.getDimensionPixelSize(R.styleable.QuickView_textSelectSize, mTextSelectSize);
		handler = new Handler();
		disapearThread = new DisapearThread();
		initPop();
		setVisibility(View.GONE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//画背景
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);//充满
		if(showBkg){//选中的颜色
			p.setColor(mBackgroundSelectColor);
		}else{
			p.setColor(mBackgroundColor);
			
		}
		int left = getWidth();
		int right = 0;
		
		if(!mRightFillet){//左圆角
			left+=mFillet;
		}
		if(!mleftFillet){
			right+=mFillet;
		}
		RectF rect = new RectF(right, 0,left, getHeight());
		p.setAntiAlias(true);// 设置画笔的锯齿效果
		canvas.drawRoundRect(rect, 40, 40, p);
		
		if (quickList == null)
			return;
		
		int height = getHeight();//1512
		int width = getWidth();//60
		int singleHeight = height / quickList.size();//总高度/个数---1512/9=168
		int textHeight = 0;
		for (int i = 0; i < quickList.size(); i++) {
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setAntiAlias(true);
			paint.setTextSize(mTextSize);
			paint.setTextAlign(Align.CENTER);
			if (i == choose) {
				textHeight = mTextSelectSize;
				paint.setTextSize(mTextSelectSize);
				paint.setColor(mTextSelectColor);
				paint.setFakeBoldText(true);
			}else{
				textHeight = mTextSize;
				paint.setColor(mTextColor);
				paint.setTextSize(mTextSize);
			}
			float xPos = width / 2;// - paint.measureText(quickList.get(i)) / 2;
			float yPos = singleHeight * i + singleHeight-(singleHeight/2);
			yPos += textHeight/2;//y+字体高度/2
			canvas.drawText(quickList.get(i), xPos, yPos, paint);
			paint.reset();
		}

	}
	boolean isOnTouch = false;
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if(quickList==null||quickList.size()<=0)
			return super.dispatchTouchEvent(event);
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final int index = (int) (y / getHeight() * quickList.size()); // 字母位置
		isOnTouch = true;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			showBkg = true;
			if (oldChoose != index) {
				if (index > 0 && index <= quickList.size()) { // 如果第一个字母是#，无效点击的话，条件变为c>0
					onTouchingLetterChanged(quickList.get(index));
					choose = index; // 处理重复
					invalidate();
				}
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (oldChoose != index) {
				if (index > 0 && index < quickList.size()) { // 如果第一个字母是#，无效点击的话，条件变为c>0
					onTouchingLetterChanged(quickList.get(index));
					choose = index;
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			showBkg = false;
			isOnTouch = false;
			choose = -1;
			invalidate();
			break;
			default:
				isOnTouch = false;
				break;
		}
		return true;
	}
	
	private void onTouchingLetterChanged(String s) {
		int num = 0;
		if("*".equals(s)){
			num = stringArr.size();
		}else if("#".equals(s)){
			num = 0;
		}else{
			for (int i = 0; i < stringArr.size(); i++) {
				String string = stringArr.get(i);
				if(string.toUpperCase().startsWith(s)){
					num=i;
					break;
				}
			}
		}
		if(txtOverlay!=null){
			txtOverlay.setVisibility(View.VISIBLE);
			txtOverlay.setText(s.toLowerCase());
			handler.removeCallbacks(disapearThread);
			// 提示延迟1.0s再消失
			boolean bool = handler.postDelayed(disapearThread, 1000);
		}
		if(listView==null) return;
		if (num < 2) {//
			listView.setSelectionFromTop(num, 0);
		} else {
			listView.setSelectionFromTop(num, 5); // 留点间隔
		}
		
	}
	/**
	 * 把单个英文字母或者字符串转换成数字ASCII码
	 *
	 * @param input
	 * @return
	 */
	private static int character2ASCII(String input) {
		char[] temp = input.toCharArray();
		StringBuilder builder = new StringBuilder();
		for (char each : temp) {
			builder.append((int) each);
		}
		String result = builder.toString();
		return Integer.parseInt(result);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setListView(ListView listView) {
		if(listView==null)return;
		if(quickList!=null&&quickList.size()>5)
			setVisibility(View.VISIBLE);
		this.listView = listView;
		listView.setOnScrollListener(new ListViewScroll());//listView滑动监听
		invalidate();
	}
	private void initPop(){
		txtOverlay = (TextView) View.inflate(mContext, R.layout.popup_char, null);
		txtOverlay.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(txtOverlay, lp);
	}
	public Map<String, String> setData(List<String> list){
		if(list==null||list.size()<=0){
			return null;
		}
		List<String> arrayList = new ArrayList<String>();//拼音
		List<String> arrayList2 = new ArrayList<String>();//字母
		List<String> arrayList3 = new ArrayList<String>();//右侧显示
		map = new HashMap<String, String>();
		for (int i = 0; i < list.size(); i++) {
			String chinese = list.get(i);
			if(chinese==null||chinese.length()<=0) continue;
			String pinyin = converterToPinYin(chinese);
			arrayList.add(pinyin); // 此列表增加拼音
			if (!arrayList2.contains(pinyin.substring(0, 1))
					&& isWord(pinyin.substring(0, 1))) {
				arrayList2.add(pinyin.substring(0, 1)); // 此列表添加拼音首字母
			}
			map.put(pinyin, list.get(i));
		}
//		Collections.sort(arrayList, new MixComparator());//排序默认的
//		Collections.sort(arrayList2, new MixComparator());
		stringArr = arrayList;
		arrayList3.add("#"); // 此列表添加不规则字符
		for (int i = 0; i < arrayList2.size(); i++) {
			String string = (String) arrayList2.get(i);
			String s = string.toUpperCase();
			if(!arrayList3.contains(s))
				arrayList3.add(string.toUpperCase()); // toUpperCase大写字母
		}
		arrayList3.add("*");
		this.quickList = arrayList3;
		return map;
	}
	public List<String> getData(){
		return stringArr;
	}
	public void setTitleView(TextView tv){
		this.title = tv;
	}
	private int scrollState1;
	private WindowManager windowManager;
	class ListViewScroll implements AbsListView.OnScrollListener{
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			scrollState1 = scrollState;
			if(getVisibility()==View.GONE)return;
			if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
				handler.removeCallbacks(disapearThread);
				// 提示延迟1.0s再消失
				boolean bool = handler.postDelayed(disapearThread, 1000);
			} else {
				txtOverlay.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if(quickList!=null&&listView.getChildCount()>=quickList.size()){//如果当前ListView的没有显示全
				setVisibility(View.VISIBLE);
			}
			if(getVisibility()==View.GONE)return;
			if(title!=null){
				title.setVisibility(View.VISIBLE);
				if (firstVisibleItem != 0) {
					title.setText(map.get(stringArr.get(firstVisibleItem)));
				} else {
					title.setText("a");
				}
				title.setText(map.get(stringArr.get(firstVisibleItem)));
			}
			if(!isOnTouch&&txtOverlay!=null&&stringArr!=null&&stringArr.size()>firstVisibleItem){
//				txtOverlay.setVisibility(View.VISIBLE);
				txtOverlay.setText(String.valueOf(stringArr.get(firstVisibleItem).toUpperCase().charAt(0)));// 泡泡文字以第一个可见列表为准
			}
			
		}
		
	}
	/**
	 * 汉语拼音转换工具
	 *
	 * @param chinese
	 * @return
	 */
	private String converterToPinYin(String chinese) {
		String pinyinString = "";
		char[] charArray = chinese.toCharArray();
		// 根据需要定制输出格式，我用默认的即可
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		try {
			// 遍历数组，ASC码大于128进行转换
			for (int i = 0; i < charArray.length; i++) {
				if (charArray[i] > 128) {
					// charAt(0)取出首字母
					if (charArray[i] >= 0x4e00 && charArray[i] <= 0x9fa5) { // 判断是否中文
						pinyinString += PinyinHelper.toHanyuPinyinStringArray(
								charArray[i], defaultFormat)[0].charAt(0);
					} else { // 不是中文的打上未知，所以无法处理韩文日本等等其他文字
						pinyinString += "?";
					}
				} else {
					pinyinString += charArray[i];
				}
			}
			return pinyinString;
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 判读字母
	 *
	 * @param str
	 * @return
	 */
	private boolean isWord(String str) {
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * 混合排序工具
	 */
	public class MixComparator implements Comparator<String> {
		public int compare(String o1, String o2) {
			// 判断是否为空""
			if (isEmpty(o1) && isEmpty(o2))
				return 0;
			if (isEmpty(o1))
				return -1;
			if (isEmpty(o2))
				return 1;
			String str1 = "";
			String str2 = "";
			try {
				str1 = (o1.toUpperCase()).substring(0, 1);
				str2 = (o2.toUpperCase()).substring(0, 1);
			} catch (Exception e) {
				System.out.println("某个str为\" \" 空");
			}
			if (isWord(str1) && isWord(str2)) { // 字母
				return str1.compareTo(str2);
			} else if (isNumeric(str1) && isWord(str2)) { // 数字字母
				return 1;
			} else if (isNumeric(str2) && isWord(str1)) {
				return -1;
			} else if (isNumeric(str1) && isNumeric(str2)) { // 数字数字
				if (Integer.parseInt(str1) > Integer.parseInt(str2)) {
					return 1;
				} else {
					return -1;
				}
			} else if (isAllWord(str1) && (!isAllWord(str2))) { // 数字字母 其他字符
				return -1;
			} else if ((!isAllWord(str1)) && isWord(str2)) {
				return 1;
			} else {
				return 1;
			}
		}
	}

	/**
	 * 判断字母数字混合
	 *
	 * @param str
	 * @return
	 */
	private boolean isAllWord(String str) {
		Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * 判断空
	 *
	 * @param str
	 * @return
	 */
	private boolean isEmpty(String str) {
		return "".equals(str.trim());
	}

	/**
	 * 判断数字
	 *
	 * @param str
	 * @return
	 */
	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("^[0-9]*$");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		} else {
			return true;
		}
	}
	private class DisapearThread implements Runnable {
		public void run() {
			// 避免在1.5s内，用户再次拖动时提示框又执行隐藏命令。
			if (scrollState1 == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
				if(txtOverlay!=null)
					txtOverlay.setVisibility(View.INVISIBLE);
			}
		}
	}
	@Override
	protected void onDetachedFromWindow() {
		if(txtOverlay!=null&&windowManager!=null)
			windowManager.removeView(txtOverlay);
		super.onDetachedFromWindow();
	}

}