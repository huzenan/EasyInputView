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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    // 文字禁按状态下颜色，默认Color.LTGRAY
    private int textColorDisable;
    // 功能键字体大小，默认20sp
    private int keyTextSize;
    // 功能键字体颜色，默认Color.BLACK
    private int keyTextColor;
    // 功能键字体按下颜色，默认Color.BLACK
    private int keyTextColorPressed;
    // 功能键文字禁按状态下颜色，默认Color.LTGRAY
    private int keyTextColorDisable;
    // 背景色，默认Color.WHITE
    private int bgColor;
    // 键盘按下颜色，默认Color.LTGRAY
    private int bgColorPressed;
    // 键盘禁按状态下颜色，默认Color.WHITE
    private int bgColorDisable;
    // 功能键高度，默认50dp
    private int keyHeight;

    private Paint paint;
    private TextPaint keyTextPaint;
    private TextPaint textPaint;
    private Paint.FontMetrics fm;
    private Paint.FontMetrics kFm;
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

    private Map<Integer, Integer> pressedMap;

    private ArrayList<Integer> disableList;

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
        textColorDisable = a.getColor(R.styleable.EasyKeyboard_ekbTextColorDisable, Color.LTGRAY);
        keyTextSize = a.getDimensionPixelSize(R.styleable.EasyKeyboard_ekbKeyTextSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics()));
        keyTextColor = a.getColor(R.styleable.EasyKeyboard_ekbKeyTextColor, Color.BLACK);
        keyTextColorPressed = a.getColor(R.styleable.EasyKeyboard_ekbKeyTextColorPressed, Color.BLACK);
        keyTextColorDisable = a.getColor(R.styleable.EasyKeyboard_ekbKeyTextColorDisable, Color.LTGRAY);
        bgColor = a.getColor(R.styleable.EasyKeyboard_ekbBgColor, Color.WHITE);
        bgColorPressed = a.getColor(R.styleable.EasyKeyboard_ekbBgColorPressed, Color.LTGRAY);
        bgColorDisable = a.getColor(R.styleable.EasyKeyboard_ekbBgColorDisable, Color.WHITE);
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
        keyTextPaint = new TextPaint();
        keyTextPaint.setAntiAlias(true);
        keyTextPaint.setTextSize(keyTextSize);
        keyTextPaint.setColor(keyTextColor);
        fm = textPaint.getFontMetrics();
        kFm = keyTextPaint.getFontMetrics();

        lines = 4;
        columns = 3;

        int len = lines * columns + 1;
        rects = new Rect[len];
        for (int i = 0; i < len; i++)
            rects[i] = new Rect();

        // 默认显示的数据项
        dataList = new ArrayList<>();
        initDefaultDataList(len);

        pressedMap = new HashMap<>();
        disableList = new ArrayList<>();
    }

    private void initDefaultDataList(int len) {
        // 功能键
        dataList.add("CONFIRM");

        // 数字
        for (int i = 0; i < len - 4; i++)
            dataList.add("" + (i + 1));

        // 最后一行
        dataList.add("");
        dataList.add("0");
        dataList.add("←");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = (int) (dm.heightPixels * heightRatio) + keyHeight;

        // 第一个位置为功能键
        rects[0].left = 0;
        rects[0].top = 0;
        rects[0].right = width;
        rects[0].bottom = keyHeight;

        w = width / columns;
        h = (height - keyHeight) / lines;
        for (int l = 0; l < lines; l++) {
            for (int c = 0; c < columns; c++) {
                int index = l * columns + c + 1;
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

        // 禁用部分背景
        paint.setColor(bgColorDisable);
        for (Integer index : disableList)
            canvas.drawRect(rects[index], paint);
        paint.setColor(bgColor);

        // 按下部分背景
        Collection<Integer> pressedIndexes = pressedMap.values();
        paint.setColor(bgColorPressed);
        for (Integer index : pressedIndexes) {
            Rect r = rects[index];
            canvas.drawRect(r, paint);
        }

        // 功能键
        if (keyHeight > 0) {
            String funcStr = dataList.get(0);
            float startX = width / 2 - keyTextPaint.measureText(funcStr) / 2;

            if (pressedIndexes.contains(0))
                keyTextPaint.setColor(keyTextColorPressed);
            else if (disableList.contains(0))
                keyTextPaint.setColor(keyTextColorDisable);
            else
                keyTextPaint.setColor(keyTextColor);

            canvas.drawText(funcStr, startX, keyHeight / 2 - (kFm.ascent + kFm.descent) / 2, keyTextPaint);
        }

        // 文字
        int baseLine = 0;
        for (int l = 0; l < lines; l++) {
            baseLine = (int) (keyHeight + l * h + h / 2 - (fm.ascent + fm.descent) / 2);
            for (int c = 0; c < columns; c++) {
                int index = l * columns + c + 1;

                if (pressedIndexes.contains(index))
                    textPaint.setColor(textColorPressed);
                else if (disableList.contains(index))
                    textPaint.setColor(textColorDisable);
                else
                    textPaint.setColor(textColor);

                canvas.drawText(dataList.get(index), c * w + w / 2 - textWidth / 2, baseLine, textPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 多点触摸移动时，系统会抛出异常java.lang.IllegalArgumentException: pointerIndex out of range
        // 是安卓系统的bug
        try {
            int actionIndex = event.getActionIndex();
            int pointerId = event.getPointerId(actionIndex);

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    int downX = (int) event.getX(pointerId);
                    int downY = (int) event.getY(pointerId);
                    int index = getNewPointIndex(downX, downY);

                    if (disableList.contains(index))
                        return false;

                    if (index != -1) {
                        pressedMap.put(pointerId, index);

                        if (null != onEasyKeyListener)
                            onEasyKeyListener.onKeyDown(index);
                    }
                }
                break;

                case MotionEvent.ACTION_MOVE: {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        int movePointerId = event.getPointerId(i);
                        int x = (int) event.getX(movePointerId);
                        int y = (int) event.getY(movePointerId);
                        Integer index = pressedMap.get(movePointerId);

                        // 触摸点已经移出正在按下的键
                        if (null != index && index != getPointIndex(x, y)) {
                            pressedMap.remove(movePointerId);

                            if (null != onEasyKeyListener)
                                onEasyKeyListener.onKeyCancel(index);
                        }
                    }
                }
                break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    Integer index = pressedMap.remove(pointerId);
                    if (null != index && null != onEasyKeyListener)
                        onEasyKeyListener.onKeyUp(index);
                }
                break;
            }
            invalidate();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取点对应的按键下标
     *
     * @param x 触摸点x坐标
     * @param y 触摸点y坐标
     * @return 点对应的按键下标
     */
    private int getPointIndex(int x, int y) {
        int index = -1;
        for (int i = 0; i < rects.length; i++) {
            if (rects[i].contains(x, y)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 获取点对应的按键下标，不包含已按下的点
     *
     * @param x 触摸点x坐标
     * @param y 触摸点y坐标
     * @return 点对应的按键下标
     */
    private int getNewPointIndex(int x, int y) {
        int pointIndex = getPointIndex(x, y);
        return pressedMap.containsValue(pointIndex) ? -1 : pointIndex;
    }

    /**
     * 设置要显示的数据项
     *
     * @param dataList 要显示的数据项，size必须为13
     */
    public void setDataList(ArrayList<String> dataList) {
        if (null == dataList || dataList.size() <= 0 || dataList.size() > 13) {
            throw new IllegalArgumentException("Size of dataList must be 13.");
        } else {
            this.dataList.addAll(dataList);
            invalidate();
        }
    }

    public ArrayList<String> getDataList() {
        return dataList;
    }

    /**
     * 设置单个禁用的按键
     *
     * @param index   要禁用的按键下标
     * @param disable true为禁用，false为不禁用
     */
    public void setDisableKey(Integer index, boolean disable) {
        if (index < 0 || index >= lines * columns + 1)
            return;

        if (!disableList.contains(index)) {
            if (disable)
                disableList.add(index);
            else
                disableList.remove(index);
        }

        invalidate();
    }

    /**
     * 设置禁用的按键集
     *
     * @param startIndex 禁用按键集下标起始
     * @param endIndex   禁用按键集下标结束
     * @param disable    true为禁用，false为不禁用
     */
    public void setDisableKeys(Integer startIndex, Integer endIndex, boolean disable) {
        if (startIndex > endIndex || startIndex < 0 || endIndex >= lines * columns + 1)
            return;

        disableList.clear();
        for (Integer i = startIndex; i <= endIndex; i++) {
            if (disable)
                disableList.add(i);
            else
                disableList.remove(i);
        }

        invalidate();
    }

    /**
     * 监听按键的接口
     */
    public interface onEasyKeyListener {
        /**
         * 当按键按下时回调
         *
         * @param index 被按下的按键的下标
         */
        public void onKeyDown(int index);

        /**
         * 当按键被取消时回调，如触摸点移出按键时
         *
         * @param index 被取消的按键的下标
         */
        public void onKeyCancel(int index);

        /**
         * 当按键抬起时回调
         *
         * @param index 抬起的按键的下标
         */
        public void onKeyUp(int index);
    }

    private onEasyKeyListener onEasyKeyListener;

    /**
     * 设置监听按键的接口
     *
     * @param onEasyKeyListener 监听按键的接口
     */
    public void setOnEasyKeyListener(onEasyKeyListener onEasyKeyListener) {
        this.onEasyKeyListener = onEasyKeyListener;
    }
}
