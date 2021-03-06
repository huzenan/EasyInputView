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
    // 文字颜色，默认Color.BLACK
    private int textColor;
    // 文字在填充状态时的颜色，默认Color.WHITE
    private int textFillColor;
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
    // 底部线条颜色，默认不显示线条
    private int strokeColor;
    // 底部线条填充时颜色，默认不显示线条
    private int strokeFillColor;
    // 底部线条粗细，默认1dp
    private int strokeWidth;
    // 底部线条相对于字符的间隔，默认0
    private int strokePadding;
    // 每多少字符进行一次分隔，默认4
    private int separateNum;
    // 间隔的距离，默认10dp
    private int separateWidth;
    // 模式，默认normal
    private int textMode;
    // 填充模式，默认normal
    private int fillMode;

    // 正常模式
    private static final int MODE_NORMAL = 0;
    // 分离模式（例如用于银行卡号的显示）
    private static final int MODE_SEPARATE = 1;

    // 正常填充模式，当填充满后，填充色消失
    private static final int FILL_MODE_NORMAL = 0;
    // 填充色总是不消失模式
    private static final int FILL_MODE_ALWAYS = 1;
    // 无填充色
    private static final int FILL_MODE_NONE = 2;

    private Paint paint;
    private Paint strokePaint;
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
    private int contentWidth;

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
        textColor = a.getColor(R.styleable.EasyInputView_eivTextColor, Color.BLACK);
        textFillColor = a.getColor(R.styleable.EasyInputView_eivTextFillColor, Color.WHITE);
        textInstead = a.getString(R.styleable.EasyInputView_eivTextInstead);
        strokeColor = a.getColor(R.styleable.EasyInputView_eivStrokeColor, -1);
        strokeFillColor = a.getColor(R.styleable.EasyInputView_eivStrokeFillColor, -1);
        strokeWidth = a.getDimensionPixelSize(R.styleable.EasyInputView_eivStrokeWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        strokePadding = a.getDimensionPixelSize(R.styleable.EasyInputView_eivStrokePadding, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()));
        fillColor = a.getColor(R.styleable.EasyInputView_eivFillColor, Color.GRAY);
        singleWidth = a.getDimensionPixelSize(R.styleable.EasyInputView_eivSingleWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -1, getResources().getDisplayMetrics()));
        singleHeight = a.getDimensionPixelSize(R.styleable.EasyInputView_eivSingleHeight, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, -1, getResources().getDisplayMetrics()));
        textMax = a.getInteger(R.styleable.EasyInputView_eivTextMax, 4);
        separateNum = a.getInteger(R.styleable.EasyInputView_eivSeparateNum, 4);
        separateWidth = a.getDimensionPixelSize(R.styleable.EasyInputView_eivSeparateWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
        textMode = a.getInteger(R.styleable.EasyInputView_eivTextMode, MODE_NORMAL);
        fillMode = a.getInteger(R.styleable.EasyInputView_eivFillMode, FILL_MODE_NORMAL);
        a.recycle();

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(strokeWidth);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
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
        contentWidth = textMax * singleWidth;
        if (textMode == MODE_SEPARATE) {
            contentWidth += ((textMax - 1) / separateNum) * separateWidth;
        }
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

        if (strokeColor != -1)
            height = height + strokeWidth + strokePadding;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != dataList && dataList.size() >= 0) {
            int size = dataList.size();
            // 改变颜色
            if (size > 0 &&
                    (fillMode == FILL_MODE_ALWAYS ||
                            (size < textMax || size == textMax && startX + curLen < contentWidth))) {
                // 绘制填充层
                if (fillMode != FILL_MODE_NONE)
                    canvas.drawRect(startX, startY, startX + curLen, startY + singleHeight, paint);

                // 底部线条
                if (strokeColor != -1 && strokeFillColor != -1)
                    strokePaint.setColor(strokeFillColor);

                // 文字
                if (fillMode != FILL_MODE_NONE)
                    textPaint.setColor(textFillColor);
                else
                    textPaint.setColor(textColor);
            } else {
                // 底部线条
                strokePaint.setColor(strokeColor);

                // 文字
                textPaint.setColor(textColor);
            }

            // 绘制底部线条
            canvas.drawLine(startX, startY + singleHeight + strokePadding, startX + contentWidth, startY + singleHeight + strokePadding, strokePaint);

            // 绘制文字
            float baseLine = (getHeight() - strokeWidth - strokePadding) / 2 - (fm.ascent + fm.descent) / 2;
            for (int i = 0; i < size; i++) {
                String str = dataList.get(i);
                if (!TextUtils.isEmpty(textInstead))
                    str = textInstead;

                int textWidth = (int) textPaint.measureText(str);
                int x = startX + singleWidth * i + singleWidth / 2 - textWidth / 2;

                if (textMode == MODE_SEPARATE)
                    x += (i / separateNum) * separateWidth;

                canvas.drawText(str, x, baseLine, textPaint);
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

    /**
     * 获取已输入的字符
     *
     * @return 已输入的字符List
     */
    public List<String> getDataList() {
        return dataList;
    }

    /**
     * 获取当前字符长度
     *
     * @return 当前字符长度
     */
    public int getCurLength() {
        if (null == dataList || dataList.size() <= 0)
            return -1;

        return dataList.size();
    }

    /**
     * 获取字符最大长度
     *
     * @return 字符最大长度
     */
    public int getTextMax() {
        return textMax;
    }

    private void startAnimation() {
        new Thread(new Runnable() {
            private int nowLen;
            private float dir;

            @Override
            public void run() {
                int size = dataList.size();

                nowLen = dataList.size() * singleWidth;
                if (textMode == MODE_SEPARATE)
                    nowLen += ((size - 1) / separateNum) * separateWidth;

                dir = Math.signum(nowLen - curLen);

                if (dir > 0)
                    curLen = nowLen - singleWidth;
                else if (dir < 0)
                    curLen = nowLen + singleWidth;

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
