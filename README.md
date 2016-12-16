# EasyInputView & EasyKeyboard
A light input view with animation and light keyboard view for Android.

## ScreenShots
![card_input](https://github.com/huzenan/EasyInputView/blob/master/screenshots/card_input.gif)

## Usage
>layout

```xml
    <com.hzn.easyinputview.EasyInputView
        android:id="@+id/eiv_card"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        custom:eivFillColor="#08588d"
        custom:eivFillMode="normal"
        custom:eivSeparateNum="4"
        custom:eivSeparateWidth="12dp"
        custom:eivSingleHeight="28dp"
        custom:eivSingleWidth="18dp"
        custom:eivStrokeColor="#cacaca"
        custom:eivStrokeFillColor="#08588d"
        custom:eivStrokePadding="0dp"
        custom:eivStrokeWidth="1dp"
        custom:eivTextColor="#999999"
        custom:eivTextFillColor="#ffffff"
        custom:eivTextInstead="*"
        custom:eivTextMax="16"
        custom:eivTextMode="separate"
        custom:eivTextSize="20dp"/>
        
    <com.hzn.easyinputview.EasyKeyboard
        android:id="@+id/key_board"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        custom:ekbBackKeyDrawable="@mipmap/back"
        custom:ekbBgColor="#ffffff"
        custom:ekbBgColorDisable="#ffffff"
        custom:ekbBgColorPressed="#08588d"
        custom:ekbHeightRatio="0.33"
        custom:ekbInitState="hide"
        custom:ekbKeyHeight="45dp"
        custom:ekbKeyTextColor="#000000"
        custom:ekbKeyTextColorDisable="#cacaca"
        custom:ekbKeyTextColorPressed="#ffffff"
        custom:ekbKeyTextSize="20dp"
        custom:ekbTextColor="#000000"
        custom:ekbTextColorDisable="#cacaca"
        custom:ekbTextColorPressed="#ffffff"
        custom:ekbTextSize="25dp"/>
```
>Activity

```java
    // show key board
    eiv_card.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            keyboard.show(eiv_card);
        }
    });

    // set listener
    keyboard.setOnEasyKeyListener(new EasyKeyboard.onEasyKeyListener() {
        @Override
        public void onKeyDown(View view, int index) {
            // your codes.
        }

        @Override
        public void onKeyCancel(View view, int index) {
            // your codes.
        }

        @Override
        public void onKeyUp(View view, int index) {
            // your codes.
        }
    });
```
