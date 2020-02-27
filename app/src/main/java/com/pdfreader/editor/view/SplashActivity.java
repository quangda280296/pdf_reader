package com.pdfreader.editor.view;

import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pdfreader.editor.base.BaseActivity;
import com.pdfreader.pdf.reader.editor.R;
import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {

    private static final int REQUEST_CODE_PERMISSTION = 21;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avLoadingIndicatorView;

    @BindView(R.id.img_splash)
    ImageView imageView_pdf;

    @BindView(R.id.layout)
    ConstraintLayout constraintLayout;

    @BindView(R.id.linear_splash)
    LinearLayout linearLayout_text;

    private CountDownTimer countDownTimer;
//    private Animation animation_in_left;
//    private Animation animation_in_right;
//    private LottieDrawable drawable;

    private static long time = 3000;

    @Override
    public int getViewResource() {
        return R.layout.activity_splash;
    }

    @Override
    public void foundView(@Nullable View view) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        setAnimation();

//        animation_in_left = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_left);
//        imageView_pdf.startAnimation(animation_in_left);
//        animation_in_right = AnimationUtils.loadAnimation(this, R.anim.anim_slide_in_right);
//        linearLayout_text.startAnimation(animation_in_right);
        imageView_pdf.setVisibility(View.VISIBLE);
        linearLayout_text.setVisibility(View.VISIBLE);
        avLoadingIndicatorView.show();
        animationShowLayoutMain();

//        drawable.addAnimatorListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
//                    startActivity(new Intent(intent));
//                    finish();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//            }
//        });
    }

    @Override
    public void notFoundView() {

    }

    @Override
    public void setListener() {
    }

//    private void setAnimation() {
//        drawable = new LottieDrawable();
//        LottieComposition.Factory.fromAssetFileName(this, "animation_loading.json", (new OnCompositionLoadedListener() {
//            @Override
//            public void onCompositionLoaded(LottieComposition composition) {
//                drawable.setComposition(composition);
//                drawable.playAnimation();
//                drawable.isLooping();
//                drawable.loop(true);
//                drawable.setRepeatCount(4);
//                lottieAnimationView.setImageDrawable(drawable);
//            }
//        }));
//    }

    private void animationShowLayoutMain() {
        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                avLoadingIndicatorView.hide();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(new Intent(intent));
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
