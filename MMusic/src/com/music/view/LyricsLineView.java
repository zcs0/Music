
package com.music.view;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Filter;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.music.MusicApp;
import com.music.R;
import com.music.activity.IConstants;
import com.music.model.LyricSentence;
public class LyricsLineView extends LinearLayout {

    /**
     * The number of items show in the selector wheel.
     * 所在显示的数量
     */
    private static int SELECTOR_WHEEL_ITEM_COUNT = 5;

    /**
     * The default update interval during long press.
     * 他默认时更新时间间隔。
     */
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;

    /**
     * The index of the middle selector item.
     * 中间选择项目的索引。
     */
    private static int SELECTOR_MIDDLE_ITEM_INDEX = SELECTOR_WHEEL_ITEM_COUNT / 2;

    /**
     * The coefficient by which to adjust (divide) the max fling velocity.
     * 由系数调整（除）最大一扔速度。
     */
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 8;

    /**
     * The the duration for adjusting the selector wheel.
     * 的持续时间为调整所述选择轮。
     */
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;

    /**
     * The duration of scrolling while snapping to a given position.
     * 滚动到上一下，或下一个时的动画时长
     */
    private static final int SNAP_SCROLL_DURATION = 800;

    /**
     * The strength of fading in the top and bottom while drawing the selector.
     * 衰落中的顶部和底部同时提请选择器的强度。
     */
    private static float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;

    /**
     * The default unscaled height of the selection divider.
     * 衰落中的顶部和底部同时提请选择器的强度。
     */
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT = 2;

    /**
     * The default unscaled distance between the selection dividers.
     * 选择分频器之间的默认缩放的距离。
     */
    private static final int UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE = 48;

    /**
     * The resource id for the default layout.
     */
//    private static final int DEFAULT_LAYOUT_RESOURCE_ID = R.layout.number_picker;

    /**
     * Constant for unspecified size.
     * 常数未指定的大小。
     */
    private static final int SIZE_UNSPECIFIED = -1;
    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     *
     * @hide
     */
    public static final LyricsLineView.Formatter TWO_DIGIT_FORMATTER = new LyricsLineView.Formatter() {
        final StringBuilder mBuilder = new StringBuilder();

        final java.util.Formatter mFmt = new java.util.Formatter(mBuilder, java.util.Locale.US);

        final Object[] mArgs = new Object[1];

        public String format(int value) {
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            mFmt.format("%02d", mArgs);
            return mFmt.toString();
        }
    };
    
    /**
     * Use a custom NumberPicker formatting callback to use two-digit minutes
     * strings like "01". Keeping a static formatter etc. is the most efficient
     * way to do this; it avoids creating temporary objects on every call to
     * format().
     */
    private static class TwoDigitFormatter implements LyricsLineView.Formatter {

        final StringBuilder mBuilder = new StringBuilder();

        //char mZeroDigit;
        java.util.Formatter mFmt;

        final Object[] mArgs = new Object[1];

        TwoDigitFormatter() {
            final Locale locale = Locale.getDefault();
            init(locale);
        }

        private void init(Locale locale) {
            mFmt = createFormatter(locale);
            //mZeroDigit = getZeroDigit(locale);
        }

        public String format(int value) {
            final Locale currentLocale = Locale.getDefault();
//            if (mZeroDigit != getZeroDigit(currentLocale)) {
//                init(currentLocale);
//            }
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            mFmt.format("%02d", mArgs);
            return mFmt.toString();
        }

        /*private static char getZeroDigit(Locale locale) {
            return LocaleData.get(locale).zeroDigit;
        }*/

        private java.util.Formatter createFormatter(Locale locale) {
            return new java.util.Formatter(mBuilder, locale);
        }
    }

    private static final TwoDigitFormatter sTwoDigitFormatter = new TwoDigitFormatter();

	protected static boolean isShowLineLyrice = false;

    /**
     * @hide
     */
    public static final Formatter getTwoDigitFormatter() {
        return sTwoDigitFormatter;
    }

    /**
     * The increment button.
     */
    //private final ImageButton mIncrementButton;

    /**
     * The decrement button.
     */
    //private final ImageButton mDecrementButton;

    /**
     * The text for showing the current value.
     */
//    private final EditText mInputText;

    /**
     * The distance between the two selection dividers.
     */
    private final int mSelectionDividersDistance;

    /**
     * The min height of this widget.
     */
    private final int mMinHeight;

    /**
     * The max height of this widget.
     */
    private final int mMaxHeight;

    /**
     * The max width of this widget.
     */
    private final int mMinWidth;

    /**
     * The max width of this widget.
     */
    private int mMaxWidth;

    /**
     * Flag whether to compute the max width.
     */
    private final boolean mComputeMaxWidth;

    /**
     * The height of the text.
     */
    private final int mTextSize;

    /**
     * The height of the gap between text elements if the selector wheel.
     * 每个条目中所持有的空白高度
     */
    private int mSelectorTextGapHeight;

    /**
     * The values to be displayed instead the indices.
     */
    private List<LyricSentence> mDisplayedValues;

    /**
     * Lower value of the range of numbers allowed for the NumberPicker
     * 数字选择器允许的数字范围的较低值。
     */
    private int mMinValue;

    /**
     * Upper value of the range of numbers allowed for the NumberPicker
     * 数字选择器允许的数字范围的上限值。
     */
    private int mMaxValue;

    /**
     * Current value of this NumberPicker
     * 这NumberPicker的当前值,第几行的值
     */
    private int mValue;

    /**
     * 滚动时的监听
     */
    private OnValueChangeListener mOnValueChangeListener;

    /**
     * Listener to be notified upon scroll state change.
     */
    private OnScrollListener mOnScrollListener;

    /**
     * Formatter for for displaying the current value.
     */
    private Formatter mFormatter;

    /**
     * The speed for updating the value form long press.
     */
    private long mLongPressUpdateInterval = DEFAULT_LONG_PRESS_UPDATE_INTERVAL;

    /**
     * Cache for the string representation of selector indices.
     */
    private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<String>();

    /**
     * The selector indices whose value are show by the selector.
     * 选择指数，它的值显示由选择。
     */
    private int[] mSelectorIndices = new int[SELECTOR_WHEEL_ITEM_COUNT];

    /**
     * The {@link Paint} for drawing the selector.
     */
    private final Paint mSelectorWheelPaint;

    /**
     * The {@link Drawable} for pressed virtual (increment/decrement) buttons.
     */
//    private final Drawable mVirtualButtonPressedDrawable;

    /**
     * The height of a selector element (text + gap).
     * 保存要滚动到达的位置
     */
    private int mSelectorElementHeight;

    /**
     * The initial offset of the scroll selector.
     * 滚动选择器的初始偏移。
     */
    private int mInitialScrollOffset = Integer.MIN_VALUE;
    

    /**
     * The current offset of the scroll selector.
     * 滚动选择器的当前偏移。
     */
    private int mCurrentScrollOffset;

    /**
     * The {@link Scroller} responsible for flinging the selector.
     */
    private final Scroller mFlingScroller;

    /**
     * The {@link Scroller} responsible for adjusting the selector.
     */
    private final Scroller mAdjustScroller;

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private int mPreviousScrollerY;

    /**
     * Handle to the reusable command for setting the input text selection.
     */
//    private SetSelectionCommand mSetSelectionCommand;

    /**
     * Handle to the reusable command for changing the current value from long
     * press by one.
     */
//    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;

    /**
     * Command for beginning an edit of the current value via IME on long press.
     */
//    private BeginSoftInputOnLongPressCommand mBeginSoftInputOnLongPressCommand;

    /**
     * The Y position of the last down event.
     */
    private float mLastDownEventY;

    /**
     * The time of the last down event.
     */
    private long mLastDownEventTime;

    /**
     * The Y position of the last down or move event.
     */
    private float mLastDownOrMoveEventY;

    /**
     * Determines speed during touch scrolling.
     */
    private VelocityTracker mVelocityTracker;

    /**
     * @see ViewConfiguration#getScaledTouchSlop()
     */
    private int mTouchSlop;

    /**
     * @see ViewConfiguration#getScaledMinimumFlingVelocity()
     */
    private int mMinimumFlingVelocity;

    /**
     * @see ViewConfiguration#getScaledMaximumFlingVelocity()
     */
    private int mMaximumFlingVelocity;//扔的速度

    /**
     * Flag whether the selector should wrap around.
     * 是否是一直转
     */
    private boolean mWrapSelectorWheel;

    /**
     * The back ground color used to optimize scroller fading.
     */
    private final int mSolidColor;

    /**
     * Flag whether this widget has a selector wheel.
     * 标志是否该部件有选择轮。
     */
    private final boolean mHasSelectorWheel;

    /**
     * Divider for showing item to be selected while scrolling
     */
    private final Drawable mSelectionDivider;

    /**
     * The height of the selection divider.
     */
    private final int mSelectionDividerHeight;

    /**
     * The current scroll state of the number picker.
     */
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * Flag whether to ignore move events - we ignore such when we show in IME
     * to prevent the content from scrolling.
     */
//    private boolean mIngonreMoveEvents;

    /**
     * Flag whether to show soft input on tap.
     */
    private boolean mShowSoftInputOnTap;

    /**
     * The top of the top selection divider.
     */
    private int mTopSelectionDividerTop;

    /**
     * The bottom of the bottom selection divider.
     */
    private int mBottomSelectionDividerBottom;

    /**
     * The virtual id of the last hovered child.
     */
    private int mLastHoveredChildVirtualViewId;

    /**
     * Whether the increment virtual button is pressed.
     */
    private boolean mIncrementVirtualButtonPressed;

    /**
     * Whether the decrement virtual button is pressed.
     */
    private boolean mDecrementVirtualButtonPressed;

    /**
     * Provider to report to clients the semantic structure of this widget.
     */
//    private AccessibilityNodeProviderImpl mAccessibilityNodeProvider;

    /**
     * Helper class for managing pressed state of the virtual buttons.
     */
    //private final PressedStateHelper mPressedStateHelper;

    /**
     * The keycode of the last handled DPAD down event.
     */
    private int mLastHandledDownDpadKeyCode = -1;
    /**选中后的颜色*/
	private int mSelectTextColor;
	/** 选中后进度颜色*/
	private int mSelectTextOldColor;

	private int mTextColor;

	private int mSetlectTextSize;

//	private Scroller mFlingScroller2;
	
	/**
     * 滚动时的监听
     */
    public interface OnValueChangeListener {

        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onValueChange(LyricsLineView picker, int oldVal, int newVal);
    }

    /**
     * Interface to listen for the picker scroll state.
     */
    public interface OnScrollListener {

        /**
         * 停止滚动时且失去焦点，没有没有滚动。停止滚动
         */
        public static int SCROLL_STATE_IDLE = 0;

        /**
         * 得到触摸的焦点，用户正在滚动采用触摸，并且他的手指仍然在屏幕上。
         */
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         *  用户以前一直采用触摸式滚动，并进行了一扔。
         */
        public static int SCROLL_STATE_FLING = 2;

        /**
         * Callback invoked while the number picker scroll state has changed.
         *
         * @param view The view whose scroll state is being reported.
         * @param scrollState The current scroll state. One of
         *            {@link #SCROLL_STATE_IDLE},
         *            {@link #SCROLL_STATE_TOUCH_SCROLL} or
         *            {@link #SCROLL_STATE_IDLE}.
         */
        public void onScrollStateChange(LyricsLineView view, int scrollState);
    }

    /**
     * Interface used to format current value into a string for presentation.
     */
    public interface Formatter {

        /**
         * Formats a string representation of the current value.
         *
         * @param value The currently selected value.
         * @return A formatted string representation.
         */
        public String format(int value);
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     */
    public LyricsLineView(Context context) {
        this(context, null);
        init(context);
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public LyricsLineView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.numberPickerStyle);
        init(context);
    }

    /**
     * Create a new number picker
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public LyricsLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        // process style attributes
        TypedArray attributesArray = context.obtainStyledAttributes(
                attrs, R.styleable.NumberPicker, defStyle, 0);
        mHasSelectorWheel=true;

        mSolidColor = attributesArray.getColor(R.styleable.NumberPicker_solidColor, 0);//上下过渡颜色//tool:solidColor="#00ff00"
        
        mTextColor = attributesArray.getColor(R.styleable.NumberPicker_textColor, 0);//字体颜色
        if(mTextColor==0){
        	mTextColor= Color.BLACK;
        }
        TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = attributesArray.getFloat(R.styleable.NumberPicker_solidColoraAtenuation, 0);////上下颜色过度衰减强度0-1
        SELECTOR_WHEEL_ITEM_COUNT = attributesArray.getInt(R.styleable.NumberPicker_itemCount, 1);//默认显示条数
        SELECTOR_MIDDLE_ITEM_INDEX = SELECTOR_WHEEL_ITEM_COUNT/2;
        mSelectorIndices = new int[SELECTOR_WHEEL_ITEM_COUNT];
        mSelectTextColor = attributesArray.getColor(R.styleable.NumberPicker_selectTextColor, -1);//选中后字体颜色
        mSelectTextOldColor = attributesArray.getColor(R.styleable.NumberPicker_selectTextOldColor, -1);//选中后字体颜色
        mSelectTextColor = mSelectTextColor==-1?mTextColor:mSelectTextColor;
        mSelectTextOldColor = mSelectTextOldColor==-1?mTextColor:mSelectTextOldColor;
        mTextSize=  attributesArray.getDimensionPixelSize(R.styleable.NumberPicker_textSize, 15);//字体大小
        mSetlectTextSize=  attributesArray.getDimensionPixelSize(R.styleable.NumberPicker_setlectTextSize, -1);//选中时字体大小
        mSetlectTextSize = mSetlectTextSize<=0?mTextSize:mSetlectTextSize;
        mSelectionDivider = attributesArray.getDrawable(R.styleable.NumberPicker_selectionDivider);//selectionDivider中间字体的上下分割线
        final int defSelectionDividerHeight = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT,
                getResources().getDisplayMetrics());
        mSelectionDividerHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_selectionDividerHeight, defSelectionDividerHeight);

        final int defSelectionDividerDistance = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DEFAULT_SELECTION_DIVIDERS_DISTANCE,
                getResources().getDisplayMetrics());
        mSelectionDividersDistance = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_selectionDividersDistance, defSelectionDividerDistance);// 中间字体上下分割线的距离tool:selectionDividersDistance="5dp"

        mMinHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMinHeight, SIZE_UNSPECIFIED);// 最大高度tool:internalMinHeight="20dp"
//        System.out.println(" mMinHeight  "+mMinHeight);
        mMaxHeight = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMaxHeight, SIZE_UNSPECIFIED);//最小高度
        if (mMinHeight != SIZE_UNSPECIFIED && mMaxHeight != SIZE_UNSPECIFIED
                && mMinHeight > mMaxHeight) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }

        mMinWidth = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMinWidth, SIZE_UNSPECIFIED);//最小宽度

        mMaxWidth = attributesArray.getDimensionPixelSize(
                R.styleable.NumberPicker_internalMaxWidth, SIZE_UNSPECIFIED);//最大宽度
        if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED
                && mMinWidth > mMaxWidth) {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }

        mComputeMaxWidth = (mMaxWidth == SIZE_UNSPECIFIED);

//        mVirtualButtonPressedDrawable = attributesArray.getDrawable(
//                R.styleable.NumberPicker_virtualButtonPressedDrawable);

        attributesArray.recycle();

//        mPressedStateHelper = new PressedStateHelper();

        // By default Linearlayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(!mHasSelectorWheel);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
//        inflater.inflate(layoutResId, this, true);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity()
                / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT;
        //mTextSize = (int) mInputText.getTextSize();
       

        // create the selector wheel paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(mTextSize);
        paint.setColor(mTextColor);
        mSelectorWheelPaint = paint;

        // create the fling and adjust scrollers
        mFlingScroller = new Scroller(getContext(), null, true);
//        mFlingScroller2 = new Scroller(getContext());
        mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));
        updateInputTextView();

        // If not explicitly specified this view is important for accessibility.
//        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
//            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
//        }
    }
    /**
	 * 高亮歌词画笔
	 */
	private Paint paintHL;
	private Paint paintHLED;
    private void init(Context cxt){
    	mPaintForTimeLine = new Paint();
		mPaintForTimeLine.setDither(true);
		mPaintForTimeLine.setAntiAlias(true);
		paintHL = new Paint();
//		paintHL.setColor(Color.rgb(255, 255, 255));
		paintHL.setDither(true);
		paintHL.setAntiAlias(true);
		paintHL.setColor(mSelectTextColor);
		paintHL.setTextSize(mSetlectTextSize);
		
		paintHLED = new Paint();
		paintHLED.setDither(true);
		paintHLED.setAntiAlias(true);
		
		paintHLED.setColor(mSelectTextOldColor);
		paintHLED.setTextSize(mSetlectTextSize);
    }
//    int position = 0;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel();
            initializeFadingEdges();
            mTopSelectionDividerTop = (getHeight() - mSelectionDividersDistance) / 2
                    - mSelectionDividerHeight;
            mBottomSelectionDividerBottom = mTopSelectionDividerTop + 2 * mSelectionDividerHeight
                    + mSelectionDividersDistance;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mHasSelectorWheel) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Try greedily to fit the max width and height.
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        // Flag if we are measured with width or height less than the respective min.
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, getMeasuredWidth(),
                widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, getMeasuredHeight(),
                heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
        if(amountToScroll==0 && mCurrentScrollOffset==0&&mSelectorElementHeight==0)return false;
//        System.out.println("moveToFinalScrollerPosition mCurrentScrollOffset"+mCurrentScrollOffset+"amountToScroll"+amountToScroll+"mSelectorElementHeight"+mSelectorElementHeight);
        int futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementHeight;
        int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
        if (overshootAdjustment != 0) {
            if (Math.abs(overshootAdjustment) > mSelectorElementHeight / 2) {
                if (overshootAdjustment > 0) {
                    overshootAdjustment -= mSelectorElementHeight;
                } else {
                    overshootAdjustment += mSelectorElementHeight;
                }
            }
            amountToScroll += overshootAdjustment;
            scrollBy(0, amountToScroll);
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mHasSelectorWheel || !isEnabled()) {
            return false;
        }
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastDownOrMoveEventY = mLastDownEventY = event.getY();
                mLastDownEventTime = event.getEventTime();
//                mIngonreMoveEvents = false;
                mShowSoftInputOnTap = false;
                // Handle pressed state before any state change.
//                if (mLastDownEventY < mTopSelectionDividerTop) {
//                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
////                        mPressedStateHelper.buttonPressDelayed(
////                                PressedStateHelper.BUTTON_DECREMENT);
//                    }
//                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
//                    if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
////                        mPressedStateHelper.buttonPressDelayed(
////                                PressedStateHelper.BUTTON_INCREMENT);
//                    }
//                }
                // Make sure we support flinging inside scrollables.
                getParent().requestDisallowInterceptTouchEvent(true);//屏蔽父类的触摸事件
                if (!mFlingScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                } else if (!mAdjustScroller.isFinished()) {
                    mFlingScroller.forceFinished(true);
                    mAdjustScroller.forceFinished(true);
                } else if (mLastDownEventY < mTopSelectionDividerTop) {
                    hideSoftInput();
                  //长按滚动
//                    postChangeCurrentByOneFromLongPress(
//                            false, ViewConfiguration.getLongPressTimeout());
                } else if (mLastDownEventY > mBottomSelectionDividerBottom) {
                    hideSoftInput();
                  //长按滚动
//                    postChangeCurrentByOneFromLongPress(
//                            true, ViewConfiguration.getLongPressTimeout());
                } else {
                    mShowSoftInputOnTap = true;
//                    postBeginSoftInputOnLongPressCommand();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mHasSelectorWheel) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getActionMasked();
        switch (action) {
	           case MotionEvent.ACTION_MOVE: {
	            float currentMoveY = event.getY();
	            if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
	                int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
	                if (deltaDownY > mTouchSlop) {
	                    onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
	                }
	            } else {
	                int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
	                //super.scrollBy(0, -deltaMoveY);
	                scrollBy(0, deltaMoveY);
	                invalidate();
	            }
	            mLastDownOrMoveEventY = currentMoveY;
            } break;
            case MotionEvent.ACTION_UP: {
//                removeBeginSoftInputCommand();
//                removeChangeCurrentByOneFromLongPress();
//                mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                } else {
                    int eventY = (int) event.getY();
                    int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);//移动的距离
                    long deltaTime = event.getEventTime() - mLastDownEventTime;//最后一次按下的时间
                    if (deltaMoveY <= mTouchSlop && deltaTime < ViewConfiguration.getTapTimeout()) {
                        if (mShowSoftInputOnTap) {
                            mShowSoftInputOnTap = false;
                        } else {//上按一下，下按一下
//                            int selectorIndexOffset = (eventY / mSelectorElementHeight)
//                                    - SELECTOR_MIDDLE_ITEM_INDEX;
//                            if (selectorIndexOffset > 0) {
//                                changeValueByOne(true);
////                                mPressedStateHelper.buttonTapped(
////                                        PressedStateHelper.BUTTON_INCREMENT);
//                            } else if (selectorIndexOffset < 0) {
//                                changeValueByOne(false);
////                                mPressedStateHelper.buttonTapped(
////                                        PressedStateHelper.BUTTON_DECREMENT);
//                            }
                        }
                    } else {
                        ensureScrollWheelAdjusted();
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            } break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
//        System.out.println("dispatchTouchEvent  "+event.getAction());
//        switch (action) {
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                removeAllCallbacks();
//                break;
//        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	System.out.println("dispatchKeyEvent  "+event.getAction());
        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!mHasSelectorWheel) {
                    break;
                }
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        if (mWrapSelectorWheel || (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                                ? getValue() < getMaxValue() : getValue() > getMinValue()) {
                            requestFocus();
                            mLastHandledDownDpadKeyCode = keyCode;
                            if (mFlingScroller.isFinished()) {
                                changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                            return true;
                        }
                        break;
                    case KeyEvent.ACTION_UP:
                        if (mLastHandledDownDpadKeyCode == keyCode) {
                            mLastHandledDownDpadKeyCode = -1;
                            return true;
                        }
                        break;
                }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
    	System.out.println("dispatchTrackballEvent  "+event.getAction());
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTrackballEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
    	System.out.println("dispatchHoverEvent  "+event.getAction());
        if (!mHasSelectorWheel) {
            return super.dispatchHoverEvent(event);
        }
        if(true){
        //if (AccessibilityManager.getInstance(getContext()).isEnabled()) {
            final int eventY = (int) event.getY();
            final int hoveredVirtualViewId;
//            if (eventY < mTopSelectionDividerTop) {
//                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_DECREMENT;
//            } else if (eventY > mBottomSelectionDividerBottom) {
//                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INCREMENT;
//            } else {
//                hoveredVirtualViewId = AccessibilityNodeProviderImpl.VIRTUAL_VIEW_ID_INPUT;
//            }
//            final int action = event.getActionMasked();
//            AccessibilityNodeProviderImpl provider =
//                (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
//            switch (action) {
//                case MotionEvent.ACTION_HOVER_ENTER: {
//                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
//                            AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
//                    mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
//                    provider.performAction(hoveredVirtualViewId,
//                            AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
//                } break;
//                case MotionEvent.ACTION_HOVER_MOVE: {
//                    if (mLastHoveredChildVirtualViewId != hoveredVirtualViewId
//                            && mLastHoveredChildVirtualViewId != View.NO_ID) {
//                        provider.sendAccessibilityEventForVirtualView(
//                                mLastHoveredChildVirtualViewId,
//                                AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
//                        provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
//                                AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
//                        mLastHoveredChildVirtualViewId = hoveredVirtualViewId;
//                        provider.performAction(hoveredVirtualViewId,
//                                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
//                    }
//                } break;
//                case MotionEvent.ACTION_HOVER_EXIT: {
//                    provider.sendAccessibilityEventForVirtualView(hoveredVirtualViewId,
//                            AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
//                    mLastHoveredChildVirtualViewId = View.NO_ID;
//                } break;
//            }
        }
        return false;
    }

    @Override
    public void computeScroll() {
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY);
        mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    @Override
    public void scrollBy(int x, int y) {
    	int[] selectorIndices = mSelectorIndices;//显示的个数
        mCurrentScrollOffset += y;//文字所占的高度
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight;
            incrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
        }
    }

   

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }

    /**
     * Set listener to be notified for scroll state changes.
     *
     * @param onScrollListener The listener.
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    /**
     * Set the formatter to be used for formatting the current value.
     * <p>
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     * </p>
     *
     * @param formatter The formatter object. If formatter is <code>null</code>,
     *            {@link String#valueOf(int)} will be used.
     *@see #setDisplayedValues(String[])
     */
    public void setFormatter(Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

   


    /**
     * Hides the soft input if it is active for the input text.
     */
    private void hideSoftInput() {
//        InputMethodManager inputMethodManager = InputMethodManager.peekInstance();
//        if (inputMethodManager != null && inputMethodManager.isActive(mInputText)) {
//            inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
//            if (mHasSelectorWheel) {
//                mInputText.setVisibility(View.INVISIBLE);
//            }
//        }
    }

    /**
     * Computes the max width if no such specified as an attribute.
     * 计算的最大宽度，如果没有这样的规定为属性
     */
    private void tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return;
        }
        int maxTextWidth = 0;
        if (mDisplayedValues == null) {
            float maxDigitWidth = 0;
            for (int i = 0; i <= 9; i++) {
                final float digitWidth = mSelectorWheelPaint.measureText(formatNumberWithLocale(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            int current = mMaxValue;
            while (current > 0) {
                numberOfDigits++;
                current = current / 10;
            }
            maxTextWidth = (int) (numberOfDigits * maxDigitWidth);
        } else {
            final int valueCount = mDisplayedValues.size();
            for (int i = 0; i < valueCount; i++) {
                final float textWidth = mSelectorWheelPaint.measureText(mDisplayedValues.get(i).getContentText());
                if (textWidth > maxTextWidth) {
                    maxTextWidth = (int) textWidth;
                }
            }
        }
//        maxTextWidth += mInputText.getPaddingLeft() + mInputText.getPaddingRight();
        if (mMaxWidth != maxTextWidth) {
            if (maxTextWidth > mMinWidth) {
                mMaxWidth = maxTextWidth;
            } else {
                mMaxWidth = mMinWidth;
            }
            invalidate();
        }
    }

    /**
     * Gets whether the selector wheel wraps when reaching the min/max value.
     *
     * @return True if the selector wheel wraps.
     *
     * @see #getMinValue()
     * @see #getMaxValue()
     */
    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }

    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the {@link LyricsLineView#getMinValue()} and
     * {@link LyricsLineView#getMaxValue()} values.
     * <p>
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     * </p>
     * <p>
     * <strong>Note:</strong> If the number of items, i.e. the range (
     * {@link #getMaxValue()} - {@link #getMinValue()}) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     * </p>
     *
     * @param wrapSelectorWheel Whether to wrap.是否循环滚动
     */
    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        final boolean wrappingAllowed = (mMaxValue - mMinValue) >= mSelectorIndices.length;
        if ((!wrapSelectorWheel || wrappingAllowed) && wrapSelectorWheel != mWrapSelectorWheel) {
            mWrapSelectorWheel = wrapSelectorWheel;
        }
    }

    /**
     * Sets the speed at which the numbers be incremented and decremented when
     * the up and down buttons are long pressed respectively.
     * <p>
     * The default value is 300 ms.
     * </p>
     *
     * @param intervalMillis The speed (in milliseconds) at which the numbers
     *            will be incremented and decremented.
     */
    public void setOnLongPressUpdateInterval(long intervalMillis) {
        mLongPressUpdateInterval = intervalMillis;
    }

    /**
     * Returns the value of the picker.
     *
     * @return The value.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Returns the min value of the picker.
     *
     * @return The min value
     */
    public int getMinValue() {
        return mMinValue;
    }

    /**
     * Sets the min value of the picker.
     *
     * @param minValue The min value inclusive.
     *
     * <strong>Note:</strong> The length of the displayed values array
     * set via {@link #setDisplayedValues(String[])} must be equal to the
     * range of selectable numbers which is equal to
     * {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setMinValue(int minValue) {
    	
        if (mMinValue == minValue) {
            return;
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        mMinValue = minValue;
        if (mMinValue > mValue) {
            mValue = mMinValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * Returns the max value of the picker.
     *
     * @return The max value.
     */
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * Sets the max value of the picker.
     *
     * @param maxValue The max value inclusive.
     *
     * <strong>Note:</strong> The length of the displayed values array
     * set via {@link #setDisplayedValues(String[])} must be equal to the
     * range of selectable numbers which is equal to
     * {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setMaxValue(int maxValue) {
        if (mMaxValue == maxValue) {
            return;
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        mMaxValue = maxValue;
        if (mMaxValue < mValue) {
            mValue = mMaxValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * Gets the values to be displayed instead of string values.
     *
     * @return The displayed values.
     */
    public List<LyricSentence> getDisplayedValues() {
        return mDisplayedValues;
    }

    /**
     * Sets the values to be displayed.
     *
     * @param displayedValues The displayed values.
     *
     * <strong>Note:</strong> The length of the displayed values array
     * must be equal to the range of selectable numbers which is equal to
     * {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     * 设置要显示的值
     */
    public void setDisplayedValues(List<LyricSentence> displayedValues) {
        if (mDisplayedValues == displayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
//        if (mDisplayedValues != null) {
//            // Allow text entry rather than strictly numeric entry.
//            mInputText.setRawInputType(InputType.TYPE_CLASS_TEXT
//                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//        } else {
//            mInputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
//        }
        setMinValue(0);
        if(displayedValues!=null&&displayedValues.size()>0)
        	setMaxValue(displayedValues.size() - 1);
        updateInputTextView();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
    }

    @Override
    protected void onDetachedFromWindow() {
    }
    /** 画时间线的画时间线 ***/
	private Paint mPaintForTimeLine;
    @Override
    protected void onDraw(Canvas canvas) {
        if (!mHasSelectorWheel) {
            super.onDraw(canvas);
            return;
        }
        float x = (getRight() - getLeft()) / 2;
        float y = mCurrentScrollOffset;//从y开始画

        // draw the selector wheel
        int[] selectorIndices = mSelectorIndices;
        if(false){
        	String text = "没有信息没有信息没有信息没有信息没有信息没有信息没有信息没有信息没有信息没有信息没有信息没有信息";
        	Paint p = new Paint();
        	int width = getWidth();
        	p.setTextAlign(Align.CENTER);
        	float measureText = p.measureText(text);
        	p.setColor(Color.RED);
        	canvas.drawText(text, x, y, p);
        	if(width>measureText){//屏幕大于字体宽度
        		canvas.clipRect(width/2, 0, width, 50f);
        	}else{
        		width = (int) (measureText-width);//字体宽度-屏幕宽度
        		canvas.clipRect(width/2, 0, width, 50f);
        	}
        	p.setColor(Color.CYAN);
        	canvas.drawText(text, x, y, p);
        	return;
        }
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorIndex = selectorIndices[i];
            String scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex);
            if(mValue==selectorIndex){//被选中的
            	mSelectorWheelPaint.setColor(mSelectTextColor);//设置被选中的颜色
            	mSelectorWheelPaint.setTextSize(mSetlectTextSize);//设置被选中的大小
            	canvas.save();
            	drawNowLyrice(canvas,scrollSelectorValue,selectorIndex,x,y);//绘制被选中的一行，字体
            }else{
            	mSelectorWheelPaint.setColor(mTextColor);//设置默认颜色
            	mSelectorWheelPaint.setTextSize(mTextSize);//设置默认大小
            	canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint);
            }
            y += mSelectorElementHeight;
            
        }
        // 画时间线和时间线
 		if (true) {
 			mPaintForTimeLine.setColor(Color.rgb(110, 232, 77));
 			mPaintForTimeLine.setTextSize(40);
 			float yy = getHeight() / 2 + getScrollY();
 			canvas.drawLine(0, yy, getWidth(), yy, mPaintForTimeLine);
 		}

        // 中间字体的上下分割线
        if (mSelectionDivider != null) {
            // draw the top divider
            int topOfTopDivider = mTopSelectionDividerTop;
            int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), bottomOfTopDivider);
            mSelectionDivider.draw(canvas);

            // draw the bottom divider
            int bottomOfBottomDivider = mBottomSelectionDividerBottom;
            int topOfBottomDivider = bottomOfBottomDivider - mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(), bottomOfBottomDivider);
            mSelectionDivider.draw(canvas);
        }
    }
    
	LyricSentence lyricSentence;
	private int progress = 0;
    private void drawNowLyrice(Canvas canvas,String lyric, int position, float x, float y) {
    	if(mDisplayedValues!=null&&mDisplayedValues.size()>position){
    		lyricSentence = mDisplayedValues.get(position);
    	}else{
    		lyricSentence = null;
    	}
    	paintHL.setTextSize(mSetlectTextSize);
		paintHLED.setTextSize(mSetlectTextSize);
		
		paintHL.setTextAlign(Align.CENTER);
		paintHLED.setTextAlign(Align.CENTER);
		float measureText = paintHL.measureText(lyric);
		//int width = (int) (measureText*pri);//字体需显示的百分比宽度
		int width = getWidth();
		canvas.drawText(lyric, x, y, paintHL);
		
		int position2 = MusicApp.mServiceManager.position();//播放位置
		float pp = 0;
		//如果每句有进度
		if(mScrollState == OnScrollListener.SCROLL_STATE_IDLE&&lyricSentence!=null&&lyricSentence.getIntervalTime()!=null&&lyricSentence.getIntervalTime().length>0){
			int lyriceLong = 0;//一句话的总时长//  作曲：程振兴 1270
			for (String i : lyricSentence.getIntervalTime()) {
				if(i!=null&&!i.isEmpty())
					lyriceLong+=Integer.valueOf(i);
			}
			long startTime = lyricSentence.getStartTime();//开始时间//2124
			int pos = (int) (position2 - startTime);//当前句进度//3210
			float f = pos/(float)lyriceLong;//2
			pp = width*f;
			//canvas.clipRect(width*f, 0, width,  y+mSelectorElementHeight,Region.Op.INTERSECT);
			//canvas.clipRect(width*f, 0, width,  y+mSelectorElementHeight,Region.Op.INTERSECT);
		}else if(lyricSentence!=null){
			long startTime = lyricSentence.getStartTime();
			long duringTime = lyricSentence.getDuringTime();
			int pos = (int) (position2 - startTime);//当前句进度//3210
			float f = pos/(float)(duringTime-startTime);//2
			pp = width*f;
		}
		if(width>measureText){//屏幕大于字体宽度
			canvas.clipRect(pp, 0, width,  y+mSelectorElementHeight,Region.Op.INTERSECT);
		}else{
			width = (int) (measureText-width);//字体宽度-屏幕宽度
			canvas.clipRect(pp, 0, width+measureText,  y+mSelectorElementHeight,Region.Op.INTERSECT);
		}
		canvas.drawText(lyric, x, y, paintHLED);
		canvas.restore();
		
	}

	@Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(LyricsLineView.class.getName());
        event.setScrollable(true);
        event.setScrollY((mMinValue + mValue) * mSelectorElementHeight);
        event.setMaxScrollY((mMaxValue - mMinValue) * mSelectorElementHeight);
    }


    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec The current measure spec.
     * @return The resolved size and state.
     */
    private int resolveSizeAndStateRespectingMinSize(
            int minSize, int measuredSize, int measureSpec) {
        if (minSize != SIZE_UNSPECIFIED) {
            final int desiredWidth = Math.max(minSize, measuredSize);
            return resolveSizeAndState(desiredWidth, measureSpec, 0);
        } else {
            return measuredSize;
        }
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.重置选择指数和清除这些指标的缓存字符串表示
     */
    private void initializeSelectorWheelIndices() {
//    	System.out.println("initializeSelectorWheelIndices--------------------");
        mSelectorIndexToStringCache.clear();
        int[] selectorIndices = mSelectorIndices;
        int current = getValue();
        for (int i = 0; i < mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - SELECTOR_MIDDLE_ITEM_INDEX);
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }
    /**
     * 设置当前选中的值位置
     * Set the current value for the number picker.
     * <p>
     * If the argument is less than the {@link LyricsLineView#getMinValue()} and
     * {@link LyricsLineView#getWrapSelectorWheel()} is <code>false</code> the
     * current value is set to the {@link LyricsLineView#getMinValue()} value.
     * </p>
     * <p>
     * If the argument is less than the {@link LyricsLineView#getMinValue()} and
     * {@link LyricsLineView#getWrapSelectorWheel()} is <code>true</code> the
     * current value is set to the {@link LyricsLineView#getMaxValue()} value.
     * </p>
     * <p>
     * If the argument is less than the {@link LyricsLineView#getMaxValue()} and
     * {@link LyricsLineView#getWrapSelectorWheel()} is <code>false</code> the
     * current value is set to the {@link LyricsLineView#getMaxValue()} value.
     * </p>
     * <p>
     * If the argument is less than the {@link LyricsLineView#getMaxValue()} and
     * {@link LyricsLineView#getWrapSelectorWheel()} is <code>true</code> the
     * current value is set to the {@link LyricsLineView#getMinValue()} value.
     * </p>
     *
     * @param value The current value.
     * @see #setWrapSelectorWheel(boolean)
     * @see #setMinValue(int)
     * @see #setMaxValue(int)
     */
    public void setValue(int value) {
        setValueInternal(value, true);
        
    }
    
    /**
     * Sets the current value of this NumberPicker.
     *
     * @param current The new value of the NumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     * 			是否通知如果当前值改变
     */
    private void setValueInternal(int current, boolean notifyChange) {
        if (mValue == current) {//如果选中是的同一个
            return;
        }
//        position = 0;
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            current = getWrappedSelectorIndex(current);
        } else {
            current = Math.max(current, mMinValue);
            current = Math.min(current, mMaxValue);
        }
        int previous = mValue;
        mValue = current;
        updateInputTextView();
        if (notifyChange) {//如果有监听，就飞起来
            notifyChange(previous, current);//发布监听
        }else{//直接到达指定位置
        }
        initializeSelectorWheelIndices();
        invalidate();
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passes argument.
     * decrement the current value.
     *
     * @param 上或下移动一个
     */
     private void changeValueByOne(boolean increment) {
//    	 System.out.println("changeValueByOne  "+increment);
        if (mHasSelectorWheel) {
            if (!moveToFinalScrollerPosition(mFlingScroller)) {
                moveToFinalScrollerPosition(mAdjustScroller);
            }
            mPreviousScrollerY = 0;
            if (increment) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementHeight, SNAP_SCROLL_DURATION);
            } else {
                mFlingScroller.startScroll(0, 0, 0, mSelectorElementHeight, SNAP_SCROLL_DURATION);
            }
            invalidate();
        } else {
            if (increment) {
                setValueInternal(mValue + 1, true);
            } else {
                setValueInternal(mValue - 1, true);
            }
        }
    }
     /**
      * 设置显示位置
      * @param position 行号
      * @param duration 时长
      */
     public void smoothScrollToPositionFromTop(int position,int duration){
    	 if (mValue == position) {//如果选中是的同一个
             return;
         }
//    	 this.position = 0;
    	 if (mWrapSelectorWheel) {//如是循环显示获得真正的位置
    		 position = getWrappedSelectorIndex(position);
         } else {
        	 position = Math.max(position, mMinValue);
        	 position = Math.min(position, mMaxValue);
         }
        if (mHasSelectorWheel) {
            if (!moveToFinalScrollerPosition(mFlingScroller)) {
                moveToFinalScrollerPosition(mAdjustScroller);
            }
            mPreviousScrollerY = 0;
            if (mValue<position) {
                mFlingScroller.startScroll(0, 0, 0, -mSelectorElementHeight*(position-mValue), duration);
            } else {
                mFlingScroller.startScroll(0, 0, 0, Math.abs(mSelectorElementHeight*(position-mValue)), duration);
            }
            invalidate();
        } 
    	 
    	 
//    	 int pos = position-mValue;//所在到达的位置-已所在的位置
////    	 scrollTo(0, 3*mSelectorElementHeight);
//    	 mValue = position;//记录选中的值
//    	 pos = pos*mSelectorElementHeight+(mSelectorTextGapHeight/2);//要移动的距离
////    	//pos= pos+(mSelectorTextGapHeight/2);//得到一个item的中部
////    	 
////    	 pos+=getScrollY();
////    	// x位置，y位置，x要移动的距离，y要移动的距离,动画时间
//    	 mFlingScroller2.startScroll(getScrollX(), getScrollY(), 0, pos,Math.abs(duration));
//    	 invalidate();
//    	 
    	 
     }
     //TODO 问题所在
    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = mSelectorIndices;
        int totalTextHeight = selectorIndices.length * mTextSize;//文字总高度108-144
        int viewHeight = getBottom() - getTop();//控件总高度
        float totalTextGapHeight = viewHeight - totalTextHeight;//底-上-总高度 92.0 计算除所有显示的文字外，剩余空间
        float textGapCount = selectorIndices.length;//显示个数3.0
        mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount + 0.5f);//92/个数+0.5  92.0/3.0+0.5f=31
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;//一个条目的所占的高度 字体大小+空白间距
        // Ensure that the middle item is positioned the same as the text in
        //控件高度的一半 - 单行文字所占的高度 + 文字高度/2 - 单行文字持有的空白区域/2
        mInitialScrollOffset = (int) (viewHeight/2-(mSelectorElementHeight * (SELECTOR_MIDDLE_ITEM_INDEX-1))-mSelectorElementHeight+mTextSize/2.5);//+mTextSize/2;//-mSelectorTextGapHeight/2;
        mCurrentScrollOffset = mInitialScrollOffset;
//        System.out.println("偏移量   "+mCurrentScrollOffset);
        updateInputTextView();
        
        
        
//        int editTextTextPosition = mInputText.getBaseline() + mInputText.getTop();
//        mInitialScrollOffset = editTextTextPosition- (mSelectorElementHeight * SELECTOR_MIDDLE_ITEM_INDEX);
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength((getBottom() - getTop() - mTextSize) / 2);
    }
    /**
     * Callback invoked upon completion of a given <code>scroller</code>.
     */
    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            if (!ensureScrollWheelAdjusted()) {
                updateInputTextView();
            }
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        } else {
            if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                updateInputTextView();
            }
        }
    }

    /**
     * Handles transition to a given <code>scrollState</code>
     * 设置监听
     */
    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    /**
     * Flings the selector with the given <code>velocityY</code>.
     * 飞到指定位置
     */
    private void fling(int velocityY) {
        mPreviousScrollerY = 0;

        if (velocityY > 0) {
            mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }

        invalidate();
    }

    /**
     * @return The wrapped index <code>selectorIndex</code> value.
     */
    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1;
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1;
        }
        return selectorIndex;
    }

    /**
     * Increments the <code>selectorIndices</code> whose string representations
     * will be displayed in the selector.
     */
    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * Decrements the <code>selectorIndices</code> whose string representations
     * will be displayed in the selector.
     */
    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * Ensures we have a cached string representation of the given <code>
     * selectorIndex</code> to avoid multiple instantiations of the same string.
     * 把数据封闭到一个集合中去
     */
    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = mSelectorIndexToStringCache;
        String scrollSelectorValue = cache.get(selectorIndex);
        if (scrollSelectorValue != null) {
            return;
        }
        if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            scrollSelectorValue = "";
        } else {
            if (mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - mMinValue;
                displayedValueIndex = displayedValueIndex>=mDisplayedValues.size()?mDisplayedValues.size()-1:displayedValueIndex;
                scrollSelectorValue = mDisplayedValues.get(displayedValueIndex).getContentText();
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    private String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.format(value) : formatNumberWithLocale(value);
    }

    private void validateInputTextView(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if (TextUtils.isEmpty(str)) {
            // Restore to the old value as we don't allow empty values
            updateInputTextView();
        } else {
            // Check the new value and ensure it's in range
            int current = getSelectedPos(str.toString());
            setValueInternal(current, true);
        }
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in {@link #setFormatter}
     * will be used to format the number.
     * 更新这个NumberPicker的看法。如果displayValues分别对应于由当前值指定的索引字符串中指定将被退回。否则，在setFormatter指定的格式化器将用于格式化数。
     *
     * @return Whether the text was updated.
     */
    private boolean updateInputTextView() {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
    	mValue=(mDisplayedValues!=null&&mValue>=mDisplayedValues.size())?mDisplayedValues.size()-1:mValue;
        String text = (mDisplayedValues == null) ? formatNumber(mValue)
                : mDisplayedValues.get(mValue - mMinValue).getContentText();
        
        
        if (!TextUtils.isEmpty(text) /*&& !text.equals(mInputText.getText().toString())*/) {
//            mInputText.setText(text);
            return true;
        }

        return false;
    }

    /**
     * 值发生改变时的监听通知
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private void notifyChange(int previous, int current) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, previous, mValue);
        }
    }

    /**
     * Posts a command for changing the current value by one.
     * 在上下边缘时长按时，自动滚动
     *
     * @param increment Whether to increment or decrement the value.
     */
//    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
//        if (mChangeCurrentByOneFromLongPressCommand == null) {
//            mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
//        } else {
//            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
//        }
//        mChangeCurrentByOneFromLongPressCommand.setStep(increment);
//        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis);
//    }



    /**
     * @return The selected index given its displayed <code>value</code>.
     */
    private int getSelectedPos(String value) {
        if (mDisplayedValues == null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Ignore as if it's not a number we don't care
            }
        } else {
            for (int i = 0; i < mDisplayedValues.size(); i++) {
                // Don't force the user to type in jan when ja will do
                value = value.toLowerCase();
                if (mDisplayedValues.get(i).getContentText().toLowerCase().startsWith(value)) {
                    return mMinValue + i;
                }
            }

            /*
             * The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {

                // Ignore as if it's not a number we don't care
            }
        }
        return mMinValue;
    }

    /**
     * Posts an {@link SetSelectionCommand} from the given <code>selectionStart
     * </code> to <code>selectionEnd</code>.
     */
//    private void postSetSelectionCommand(int selectionStart, int selectionEnd) {
//        if (mSetSelectionCommand == null) {
//            mSetSelectionCommand = new SetSelectionCommand();
//        } else {
//            removeCallbacks(mSetSelectionCommand);
//        }
//        mSetSelectionCommand.mSelectionStart = selectionStart;
//        mSetSelectionCommand.mSelectionEnd = selectionEnd;
//        post(mSetSelectionCommand);
//    }

    /**
     * The numbers accepted by the input text's {@link Filter}
     */
    private static final char[] DIGIT_CHARACTERS = new char[] {
            // Latin digits are the common case
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            // Arabic-Indic
            '\u0660', '\u0661', '\u0662', '\u0663', '\u0664', '\u0665', '\u0666', '\u0667', '\u0668'
            , '\u0669',
            // Extended Arabic-Indic
            '\u06f0', '\u06f1', '\u06f2', '\u06f3', '\u06f4', '\u06f5', '\u06f6', '\u06f7', '\u06f8'
            , '\u06f9'
    };

    /**
     * Filter for accepting only valid indices or prefixes of the string
     * representation of valid indices.
     */
    class InputTextFilter extends NumberKeyListener {

        // XXX This doesn't allow for range limits when controlled by a
        // soft input method!
        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT;
        }

        @Override
        protected char[] getAcceptedChars() {
            return DIGIT_CHARACTERS;
        }

        @Override
        public CharSequence filter(
                CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (mDisplayedValues == null) {
                CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }

                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());

                if ("".equals(result)) {
                    return result;
                }
                int val = getSelectedPos(result);

                /*
                 * Ensure the user can't type in a value greater than the max
                 * allowed. We have to allow less than min as the user might
                 * want to delete some numbers and then type a new number.
                 * And prevent multiple-"0" that exceeds the length of upper
                 * bound number.
                 */
                if (val > mMaxValue || result.length() > String.valueOf(mMaxValue).length()) {
                    return "";
                } else {
                    return filtered;
                }
            } else {
                CharSequence filtered = String.valueOf(source.subSequence(start, end));
                if (TextUtils.isEmpty(filtered)) {
                    return "";
                }
                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());
                String str = String.valueOf(result).toLowerCase();
                for (LyricSentence val : mDisplayedValues) {
                    String valLowerCase = val.getContentText().toLowerCase();
                    if (valLowerCase.startsWith(str)) {
//                        postSetSelectionCommand(result.length(), valLowerCase.length());
                        return valLowerCase.subSequence(dstart, valLowerCase.length());
                    }
                }
                return "";
            }
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     * 确保了滚轮被调整，即有没有偏移和中间元件是在小部件的中间。
     *
     * @return Whether an adjustment has been made.
     * 是否一个作了调整。
     */
    private boolean ensureScrollWheelAdjusted() {
        // adjust to the closest value
        int deltaY = mInitialScrollOffset - mCurrentScrollOffset;
        if (deltaY != 0) {
            mPreviousScrollerY = 0;
            if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
                deltaY += (deltaY > 0) ? -mSelectorElementHeight : mSelectorElementHeight;
            }
            mAdjustScroller.startScroll(0, 0, 0, deltaY, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
            invalidate();
            return true;
        }
        return false;
    }

    /**
     * Command for setting the input text selection.
     */
//    class SetSelectionCommand implements Runnable {
//        private int mSelectionStart;
//
//        private int mSelectionEnd;
//
//        public void run() {
//            //mInputText.setSelection(mSelectionStart, mSelectionEnd);
//        }
//    }

    /**
     * Command for changing the current value from a long press by one.
     */
//    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
//        private boolean mIncrement;
//
//        private void setStep(boolean increment) {
//            mIncrement = increment;
//        }
//
//        @Override
//        public void run() {
//            changeValueByOne(mIncrement);
//            postDelayed(this, mLongPressUpdateInterval);
//        }
//    }

    /**
     * @hide
     */
    public static class CustomEditText extends EditText {

        public CustomEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
            if (actionCode == EditorInfo.IME_ACTION_DONE) {
                clearFocus();
            }
        }
    }

    /**
     * Command for beginning soft input on long press.
     */
//    class BeginSoftInputOnLongPressCommand implements Runnable {
//
//        @Override
//        public void run() {
//            mIngonreMoveEvents = true;
//        }
//    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }
	/**
	 * 字体颜色
	 * @return
	 */
    public int getTextColor() {
		return mTextColor;
	}
    /**
     * 字体颜色
     * @param mTextColor
     */
	public void setTextColor(int mTextColor) {
		this.mTextColor = mTextColor;
	}
	/**
	 * 选中时的字体大小
	 * @return
	 */
	public int getSsetlectTextSize() {
		return mSetlectTextSize;
	}
	/**
	 * 被选中时的大小
	 * @param mSetlectTextSize
	 */
	public void setSetlectTextSize(int mSetlectTextSize) {
		this.mSetlectTextSize = mSetlectTextSize;
	}
	/**
	 * 字体大小
	 * @return
	 */
	public int getTextSize() {
		return mTextSize;
	}
	/**
	 * 过渡背景色
	 */
	public int getSolidColor() {
		return mSolidColor;
	}

	/**
	 * 选中时的颜色
	 * @return
	 */
    public int getSelectTextColor() {
		return mSelectTextColor;
	}
    /**
	 * 选中时的颜色
	 * @return
	 */
	public void setSelectTextColor(int mSelectTextColor) {
		this.mSelectTextColor = mSelectTextColor;
	}

	static private String formatNumberWithLocale(int value) {
        return String.format(Locale.getDefault(), "%d", value);
    }
	/**
	 * 刷新每一行的进度
	 */
	public void startRefreshLine(){
		isShowLineLyrice = true;
		post(new Runnable() {
			@Override
			public void run() {
				int position2 = MusicApp.mServiceManager.position();//播放位置
				if(position2>0&&MusicApp.mServiceManager.getPlayState()==IConstants.MPS_PLAYING){
					updateShow();
				}
				if(isShowLineLyrice){//如果显示每一句的进度
					postDelayed(this, 50);
				}
				
			}
		});
	}
	public void stopRefreshLine(){
		isShowLineLyrice = false;
	}
	private void updateShow(){
		if(mScrollState==OnScrollListener.SCROLL_STATE_IDLE){
			invalidate();
		}
	}
	
}
