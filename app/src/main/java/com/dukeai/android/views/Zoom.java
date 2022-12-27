package com.dukeai.android.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

public class Zoom extends View implements View.OnTouchListener {

    ImageButton img, img1;
    private ImageView image;
    private int zoomControler = 20;

    public Zoom(Context context, ImageView image) {
        super(context);
        this.image = image;
        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect rect = new Rect((getWidth() / 2) - zoomControler, (getHeight() / 2) - zoomControler, (getWidth() / 2) + zoomControler, (getHeight() / 2) + zoomControler);
        image.setClipBounds(rect);

        //here u can control the width and height of the images........ this line is very important
        image.draw(canvas);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            // zoom in
            zoomControler += 10;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            // zoom out
            zoomControler -= 10;
        }
        if (zoomControler < 10) {
            zoomControler = 10;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        onKeyDown(KeyEvent.KEYCODE_DPAD_UP, null);


        Rect rect = new Rect((v.getWidth() / 2) - zoomControler, (v.getHeight() / 2) - zoomControler, (v.getWidth() / 2) + zoomControler, (v.getHeight() / 2) + zoomControler);
        image.setClipBounds(rect);

        return false;
    }
}