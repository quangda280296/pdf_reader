package com.pdfreader.editor.font;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import com.pdfreader.editor.utils.AnimationUtils;

public class ImageViewCustom extends AppCompatImageView {

    private boolean isLongClick = false;
    private long delaymilis = 510;
    public ImageViewCustom(Context context) {
        super(context);
    }

    public ImageViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                AnimationUtils.zoomInButton(this);
                handler.postDelayed(mLongPressed, 800);
                return true;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(mLongPressed);
                AnimationUtils.zoomOutButton(this).addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (onImageClickListener != null && !isLongClick){
                            onImageClickListener.onClick(ImageViewCustom.this);
                        }
                        isLongClick = false;
                    }
                });

                return true;
        }
        return super.onTouchEvent(event);
    }

    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        public void run() {
            isLongClick = true;
            onHoldClickListener.onHoldClick(ImageViewCustom.this);
            if (delaymilis > 10) delaymilis = delaymilis - 50;
            handler.postDelayed(mLongPressed, delaymilis);
        }
    };

    private OnImageClickListener onImageClickListener;
    private OnHoldClickListener onHoldClickListener;

    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public void setOnHoldClickListener(OnHoldClickListener onHoldClickListener) {
        this.onHoldClickListener = onHoldClickListener;
    }


    public interface OnImageClickListener {
        void onClick(View view);
    }

    public interface OnHoldClickListener{
        void onHoldClick(View view);
    }
}
