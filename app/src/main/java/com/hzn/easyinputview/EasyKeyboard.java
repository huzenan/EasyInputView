package com.hzn.easyinputview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * 九宫格虚拟键盘，宽度默认为match_parent，高度可设置
 * Created by huzn on 2016/11/11.
 */
public class EasyKeyboard extends View {

    /**
     * 高度，相对于屏幕高的百分比值，float类型，
     * 最小为0.25，最大为0.5，默认为0.33
     */
    private float heightRatio;
    // 文字大小，默认25sp
    private int textSize;
    // 文字颜色，默认Color.BLACK
    private int textColor;
    // 文字按下颜色，默认Color.BLACK
    private int textColorPressed;
    // 背景色，默认Color.WHITE
    private int bgColor;
    // 键盘按下颜色，默认Color.LTGRAY
    private int bgColorPressed;
    // 功能键高度，默认50dp
    private int keyHeight;

    private Paint paint;
    private TextPaint textPaint;
    private Paint.FontMetrics fm;
    private int textWidth;
    private int textHeight;

    private int width;
    private int height;
    private int w;
    private int h;
    private Rect[] rects;
    private int lines;
    private int columns;

    private static final int STATE_SHOW = 0;
    private static final int STATE_HIDE = 1;
    private int curState = STATE_SHOW;

    /**
     * 键盘显示数据，默认为如下：(其中右下角为固定的删除符号)
     * | 1  2  3
     * | 4  5  6
     * | 7  8  9
     * |    0  ←
     */
    private ArrayList<String> dataList;

    private ArrayList<Integer> pressedIndexes;

    public EasyKeyboard(Context context) {
        this(context, null);
    }

    public EasyKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasyKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasyKeyboard, defStyleAttr, 0);
        heightRatio = a.getFloat(R.styleable.EasyKeyboard_ekbHeightRatio, 0.33f);
        textSize = a.getDimensionPixelSize(R.styleable.EasyKeyboard_ekbTextSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 25, getResources().getDisplayMetrics()));
        textColor = a.getColor(R.styleable.EasyKeyboard_ekbTextColor, Color.BLACK);
        textColorPressed = a.getColor(R.styleable.EasyKeyboard_ekbTextColorPressed, Color.BLACK);
        bgColor = a.getColor(R.styleable.EasyKeyboard_ekbBgColor, Color.WHITE);
        bgColorPressed = a.getColor(R.styleable.EasyKeyboard_ekbBgColorPressed, Color.LTGRAY);
        keyHeight = a.getDimensionPixelSize(R.styleable.EasyKeyboard_ekbKeyHeight, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        a.recycle();

        if (heightRatio < 0.25f)
            heightRatio = 0.25f;
        else if (heightRatio > 0.5f)
            heightRatio = 0.5f;

        paint = new Paint();
        paint.setAntiAlias(true);
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
        fm = textPaint.getFontMetrics();

        lines = 4;
        columns = 3;

        int len = lines * columns;
        rects = new Rect[len];
        for (int i = 0; i < len; i++)
            rects[i] = new Rect();

        dataList = new ArrayList<>();
        for (int i = 0; i < len - 3; i++)
            dataList.add("" + (i + 1));
        dataList.add("");
        dataList.add("0");
        dataList.add("←");

        pressedIndexes = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = (int) (dm.heightPixels * heightRatio) + keyHeight;

        w = width / columns;
        h = (height - keyHeight) / lines;
        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                int index = l * columns + c;
                rects[index].left = w * c;
                rects[index].top = keyHeight + h * l;
                rects[index].right = w * (c + 1);
                rects[index].bottom = keyHeight + h * (l + 1);
            }
        }

        textWidth = (int) textPaint.measureText("9");
        textHeight = (int) (fm.bottom - fm.top);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 背景
        paint.setColor(bgColor);
        canvas.drawRect(0, 0, width, height, paint);

        // TODO 功能键
        paint.setColor(Color.GRAY);
        canvas.drawRect(0, 0, width, keyHeight, paint);
        paint.setColor(bgColor);

        // 按下部分
        if (null != pressedIndexes && pressedIndexes.size() > 0) {
            paint.setColor(bgColorPressed);
            for (int i = 0, len = pressedIndexes.size(); i < len; i++) {
                Rect r = rects[pressedIndexes.get(i)];
                canvas.drawRect(r, paint);
            }
        }

        // 文字
        int baseLine = 0;
        for (int l = 0; l < lines; l++) {
            baseLine = (int) (keyHeight + l * h + h / 2 - (fm.ascent + fm.descent) / 2);
            for (int c = 0; c < columns; c++) {
                int index = l * columns + c;
                canvas.drawText(dataList.get(index), c * w + w / 2 - textWidth / 2, baseLine, textPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
            }
            break;

            case MotionEvent.ACTION_MOVE: {

            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {

            }
            break;
        }
        return true;
    }
}
