package cn.leo.testtimershaftview;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by Leo on 2017/8/14.
 */

public class TimerShaftView extends View {
    private TextPaint mTextPaint;
    private Paint mShaftPaint;
    private Paint mIndicatorPaint;
    private int h;
    private int w;
    private int mDis;
    private int mSum;
    private float mLeft;
    private float mDownX;
    private float mDownLeft;
    private Scroller mScroller;
    private String mCurrentTime;
    private FloatEvaluator mFloatEvaluator;
    private OnTimeChangeListener mOnTimeChangeListener;
    private int mHour;
    private int mMinute;
    private int mSecond;
    private int mColor = Color.rgb(0x48, 0x89, 0xdb);


    public TimerShaftView(Context context) {
        this(context, null);
    }

    public TimerShaftView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimerShaftView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDis = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, getResources().getDisplayMetrics());
        mSum = mDis * 240;
        //刻度文字画笔
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(mColor);
        mTextPaint.setTextSize(mDis * 3);
        //刻度画笔
        mShaftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShaftPaint.setColor(mColor);
        //标尺画笔
        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(mColor);
        mIndicatorPaint.setStrokeWidth(mDis / 3);
        //时间刻度计算器
        mFloatEvaluator = new FloatEvaluator();
        //惯性滑动器
        mScroller = new Scroller(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        h = getMeasuredHeight();
        w = getMeasuredWidth();
        mLeft = w / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < 240; i++) {
            //画短刻度
            float startX = i * mDis + mLeft;
            if (startX < -mDis * 10) startX = startX + mSum;
            if (startX > w + mDis * 10) startX = startX - mSum;
            drawShortScale(canvas, startX);
            if (i % 5 == 0) {
                //画长刻度
                drawLongScale(canvas, startX);
                if (i % 10 == 0) {
                    //画时间
                    String formatTime = getFormatTime(i / 10);
                    drawTimeScale(canvas, startX, formatTime);
                }
            }
        }
        //画标尺
        drawIndicator(canvas);
        //画当前时间
        drawCurrentTime(canvas);
    }

    //短刻度
    private void drawShortScale(Canvas canvas, float startX) {
        mShaftPaint.setStrokeWidth(1);
        canvas.drawLine(startX, h / 6, startX, h * 3 / 12, mShaftPaint);
        canvas.drawLine(startX, h * 7 / 12, startX, h * 4 / 6, mShaftPaint);
    }

    //长刻度
    private void drawLongScale(Canvas canvas, float startX) {
        mShaftPaint.setStrokeWidth(2);
        canvas.drawLine(startX, h / 6, startX, h * 2 / 6, mShaftPaint);
        canvas.drawLine(startX, h * 3 / 6, startX, h * 4 / 6, mShaftPaint);
    }

    //刻度时间
    private void drawTimeScale(Canvas canvas, float startX, String formatTime) {
        mTextPaint.setTextSize(mDis * 1.5f);
        mTextPaint.setColor(mColor);
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(formatTime, 0, 1, bounds);
        int half = bounds.height() / 2;
        int y = h * 5 / 12 + half;
        canvas.drawText(formatTime, startX, y, mTextPaint);
    }

    //当前时间
    private void drawCurrentTime(Canvas canvas) {
        mCurrentTime = getCurrentTime();
        mTextPaint.setTextSize(mDis * 2.5f);
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(mCurrentTime, 0, mCurrentTime.length(), bounds);
        int halfHeight = bounds.height() / 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(w / 3, h * 9 / 12, w * 2 / 3, h, mDis, mDis, mShaftPaint);
        } else {
            canvas.drawRect(w / 3, h * 9 / 12, w * 2 / 3, h, mShaftPaint);
        }
        mTextPaint.setColor(Color.WHITE);
        canvas.drawText(mCurrentTime, w / 2, h * 21 / 24 + halfHeight, mTextPaint);
    }

    //时间指示器
    private void drawIndicator(Canvas canvas) {
        mShaftPaint.setStrokeWidth(2);
        canvas.drawLine(w / 2, h / 6, w / 2, h * 4 / 6, mIndicatorPaint);
        canvas.drawCircle(w / 2, h / 6 - mDis, mDis, mIndicatorPaint);
        canvas.drawLine(0, h / 6, w, h / 6, mShaftPaint);
        canvas.drawLine(0, h * 4 / 6, w, h * 4 / 6, mShaftPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownX = event.getX();
            mDownLeft = mLeft;
            mScroller.forceFinished(true);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float dis = x - mDownX;
            mLeft = mDownLeft + dis;
            getSide(x);
            ViewCompat.postInvalidateOnAnimation(this);
        }
        boolean b = mGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP && !b) {
            //时间回调
            if (mOnTimeChangeListener != null) {
                mOnTimeChangeListener.onTimeChange(mCurrentTime);
            }
        }
        return true;
    }

    //手势识别
    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            inertia(velocityX);
            return true;
        }
    });
    //惯性运算
    Runnable inertiaRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScroller.isFinished()) {
                //时间回调
                if (mOnTimeChangeListener != null) {
                    mOnTimeChangeListener.onTimeChange(mCurrentTime);
                }
                return;
            }
            //如果返回true，说明当前的动画还没有结束，我们可以获得当前的x和y的值
            if (mScroller.computeScrollOffset()) {
                //获得当前的x坐标
                mLeft = mScroller.getCurrX();
                getSide(0);
                ViewCompat.postInvalidateOnAnimation(TimerShaftView.this);
                //每16ms调用一次
                postDelayed(this, 16);
            }
        }
    };

    //惯性模拟
    private void inertia(float velocityX) {
        mScroller.fling((int) mLeft, 0, (int) velocityX, 0, -mSum, mSum, 0, 0);
        post(inertiaRunnable);
    }

    //边界限制
    private void getSide(float x) {
        if (mLeft > w / 2) {
            mLeft = mLeft - mSum;
            mDownLeft = mLeft;
            mDownX = x;
            getSide(x);
            return;
        }
        if (mLeft + mSum < w / 2) {
            mLeft = mLeft + mSum;
            mDownX = x;
            mDownLeft = mLeft;
            getSide(x);
        }
    }

    private String getFormatTime(int time) {
        return getFormatNum(time) + ":00";
    }

    private String getFormatNum(int num) {
        if (num < 10) {
            return "0" + num;
        }
        return String.valueOf(num);
    }

    public String getCurrentTime() {
        float per = -(mLeft - w / 2) / mSum;
        Float ss = mFloatEvaluator.evaluate(per, 0, 86399);
        mHour = (int) (ss / 3600);
        mMinute = (int) ((ss - (mHour * 3600)) / 60);
        mSecond = (int) (ss % 60);
        return getFormatNum(mHour) + ":" + getFormatNum(mMinute) + ":" + getFormatNum(mSecond);
    }

    //设置时间变化监听
    public void setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        mOnTimeChangeListener = onTimeChangeListener;
    }

    //获取时
    public int getHour() {
        return mHour;
    }

    //获取分
    public int getMinute() {
        return mMinute;
    }

    //获取秒
    public int getSecond() {
        return mSecond;
    }

    public interface OnTimeChangeListener {
        void onTimeChange(String time);
    }
}
