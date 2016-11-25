package com.hzn.easyinputview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EasyInputView inputView;
    private EasyKeyboard keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        inputView = (EasyInputView) findViewById(R.id.input_view);
        keyboard = (EasyKeyboard) findViewById(R.id.key_board);
        final ArrayList<String> dataList = keyboard.getDataList();
        keyboard.setOnEasyKeyListener(new EasyKeyboard.onEasyKeyListener() {
            @Override
            public void onKeyDown(int index) {

            }

            @Override
            public void onKeyCancel(int index) {

            }

            @Override
            public void onKeyUp(int index) {
                if (index == 0) {
                    // 功能键

                } else if (index == dataList.size() - 1) {
                    // 回退
                    inputView.remove();
                    if (inputView.getCurLength() < inputView.getTextMax())
                        keyboard.setDisableKeys(1, 11, false);
                } else {
                    // 数字
                    inputView.add(dataList.get(index));
                    if (inputView.getCurLength() >= inputView.getTextMax())
                        keyboard.setDisableKeys(1, 11, true);
                }
            }
        });
    }
}
