package com.hzn.easyinputview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EasyInputView eiv_card;
    private EasyKeyboard keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initKeyboard();
    }

    private void initViews() {
        eiv_card = (EasyInputView) findViewById(R.id.eiv_card);
        keyboard = (EasyKeyboard) findViewById(R.id.key_board);

        eiv_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard.show(eiv_card);
            }
        });
    }

    private void initKeyboard() {
        final ArrayList<String> dataList = keyboard.getDataList();
        keyboard.setDisableKey(EasyKeyboard.KEY_FUNC, true);
        keyboard.setOnEasyKeyListener(new EasyKeyboard.onEasyKeyListener() {
            @Override
            public void onKeyDown(View view, int index) {

            }

            @Override
            public void onKeyCancel(View view, int index) {

            }

            @Override
            public void onKeyUp(View view, int index) {
                if (view instanceof EasyInputView) {
                    EasyInputView iv = (EasyInputView) view;
                    if (index == EasyKeyboard.KEY_FUNC) {
                        // 功能键
                        if (iv.getCurLength() == iv.getTextMax())
                            keyboard.hide();
                    } else if (index == EasyKeyboard.KEY_BACK) {
                        // 回退
                        iv.remove();
                        if (iv.getCurLength() < iv.getTextMax()) {
                            keyboard.setDisableKeys(1, 11, false);
                            keyboard.setDisableKey(EasyKeyboard.KEY_FUNC, true);
                        }
                    } else {
                        // 数字
                        iv.add(dataList.get(index));
                        if (iv.getCurLength() >= iv.getTextMax()) {
                            keyboard.setDisableKeys(1, 11, true);
                            keyboard.setDisableKey(EasyKeyboard.KEY_FUNC, false);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (keyboard.getState() == EasyKeyboard.STATE_SHOW)
            keyboard.hide();
        else
            super.onBackPressed();
    }
}
