package com.hzn.easyinputview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 带输入动画的输入框，例如可用于输入密码，卡号等
 * Created by huzn on 2016/11/10.
 */
public class EasyInputView extends View {

    // 文字大小，默认16sp
    private int textSize;
    // 文字颜色，默认Color.WHITE
    private int textColor;
    // 填充颜色，默认Color.GRAY
    private int fillColor;
    // 单个方块宽，大于等于2倍文字宽，默认为2倍文字宽
    private int singleWidth;
    // 单个方块高，大于等于文字高，默认为文字高
    private int singleHeight;
    // 最多输入个数，默认4
    private int textMax;
    // 替代的文字，例如用于密码输入可以设置为 "*"，默认为空
    private String textInstead;

    private Paint paint;
    private TextPaint textPaint;
    private Paint.FontMetrics fm;
    private int startX;
    private int startY;

    private List<String> dataList;
    private int curLen;
    private int speed;

    private static final int ACTION_IDLE = 0;
    private static final int ACTION_ADD = 1;
    private static final int ACTION_REMOVE = 2;
    private int curAction = ACTION_IDLE;

    public EasyInputView(Context context) {
        this(context, null);
    }

    public EasyInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasyInputView, defStyleAttr, 0);
        textSize = a.getDimensionPixelSize(R.styleable.EasyInputView_eivTextSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
        textColor = a.getColor(R.styleable.EasyInputView_eivTextColor, Color.WHITE);
        textInstead = a.getString(R.styleable.EasyInputView_eivTextInstead);
        fillColor = a.getColor(R.styleable.EasyInputView_eivFillColor, Color.GRAY);
        singleWidth = a.getDimensionPixelSize(R.styleable.EasyInputView_eivSingleWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -1, getResources().getDisplayMetrics()));
        singleHeight = a.getDimensionPixelSize(R.styleable.EasyInputView_eivSingleHeight, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -1, getResources().getDisplayMetrics()));
        textMax = a.getInteger(R.styleable.EasyInputView_eivTextMax, 4);
        a.recycle();

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        fm = textPaint.getFontMetrics();

        int textWidth = (int) textPaint.measureText("t");
        int textHeight = (int) (fm.bottom - fm.top);
        singleWidth = singleWidth < textWidth * 2 ? textWidth * 2 : singleWidth;
        singleHeight = singleHeight < textHeight ? textHeight : singleHeight;

        dataList = new ArrayList<>();
        curLen = 0;
        speed = singleWidth / 5;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int contentWidth = textMax * singleWidth;
        if (mode != MeasureSpec.EXACTLY) { // wrap_content
            width = contentWidth + getPaddingLeft() + getPaddingRight();
        }

        mode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mode != MeasureSpec.EXACTLY) { // wrap_content
            height = singleHeight + getPaddingTop() + getPaddingBottom();
        }

        startX = width / 2 - contentWidth / 2;
        startY = height / 2 - singleHeight / 2;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != dataList && dataList.size() >= 0) {
            // 绘制填充层
            canvas.drawRect(startX, startY, startX + curLen, startY + singleHeight, paint);

            // 绘制文字
            float baseLine = getHeight() / 2 - (fm.ascent + fm.descent) / 2;
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                String str = dataList.get(i);
                if (!TextUtils.isEmpty(textInstead))
                    str = textInstead;

                int textWidth = (int) textPaint.measureText(str);
                canvas.drawText(str, startX + singleWidth * i + singleWidth / 2 - textWidth / 2, baseLine, textPaint);
            }
        }
    }

    /**
     * 添加一个字符，字符达到最大值后，不做任何操作
     *
     * @param str 要添加的字符
     */
    public void add(String str) {
        if (TextUtils.isEmpty(str) || dataList.size() >= textMax)
            return;

        if (null != dataList && (curAction == ACTION_ADD || curAction == ACTION_IDLE)) {
            curAction = ACTION_ADD;
            dataList.add(str);
            startAnimation();
        }
    }

    /**
     * 删除当前最后一个字符，字符达到最小值后，不做任何操作
     */
    public void remove() {
        if (null == dataList || dataList.size() <= 0)
            return;

        if (null != dataList && (curAction == ACTION_REMOVE || curAction == ACTION_IDLE)) {
            curAction = ACTION_REMOVE;
            dataList.remove(dataList.size() - 1);
            startAnimation();
        }
    }

    private void startAnimation() {
        new Thread(new Runnable() {
            private int nowLen = dataList.size() * singleWidth;
            private float dir = Math.signum(nowLen - curLen);

            @Override
            public void run() {
                if (dir > 0)
                    curLen = (dataList.size() - 1) * singleWidth;
                else if (dir < 0)
                    curLen = (dataList.size() + 1) * singleWidth;

                while ((dir > 0 && curLen < nowLen) || (dir < 0 && curLen > nowLen)) {
                    try {
                        curLen = (int) (curLen + dir * speed);

                        if ((dir > 0 && curLen > nowLen) || (dir < 0 && curLen < nowLen))
                            curLen = nowLen;

                        Thread.sleep(20);

                        postInvalidate();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (curLen == nowLen)
                    curAction = ACTION_IDLE;
            }
        }).start();
    }
}
