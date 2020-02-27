package com.pdfreader.editor.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.os.StrictMode;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pdfreader.editor.utils.Constants;
import com.pdfreader.pdf.reader.editor.R;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements View.OnTouchListener {

    private boolean foundView = false;
    private ProgressDialog mProgressDialog;
    private long mLastClickTime = 0;

    //region Abstract method
    public abstract int getViewResource();

    public abstract void foundView(@Nullable View view);

    public abstract void notFoundView();

    public abstract void setListener();
    View view;

    public String handleIntent(Intent intent) {
        //Kinda not recommended by google but watever
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Uri appLinkData = intent.getData();
        String appLinkAction = intent.getAction();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            return Constants.DETAIL;
        }
        return null;
    }
    //endregion

    //region Override method
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        LocaleUtils.updateConfig(this);

        int resource = getViewResource();
        try {
            view = View.inflate(this, resource, null);
        } catch (Exception e) {
            foundView = false;
            notFoundView();
            return;
        }
        foundView = true;
        setContentView(view);
        ButterKnife.bind(this);
        view.setClickable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(this);
        handleIntent(getIntent());
        setListener();
        foundView(view);
    }



    @Override
    protected void onResume() {
        if (foundView)
            super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        EventBus.getDefault().unregister(this);
    }

//    @Subscribe(threadMode = ThreadMode.BACKGROUND)
//    public void onMessageEvent(String event) {
//        switch (event.getAction()) {
//            case RadioAction.URL: {
//                setUrl(event.getData());
//                return;
//            }
//            case RadioAction.PAUSE: {
//                pause();
//                return;
//            }
//            case RadioAction.PLAY: {
//                play();
//                return;
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideKeyboard();
        return false;
    }

    public void onFragmentAttached() {

    }

    public void showKeyboard(EditText edt){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideKeyboardFrom( View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
    public void showProgressDialog() {
        hideProgressDialog();
        if (mProgressDialog == null) {
            mProgressDialog = showLoadingDialog(this, true);
        }
        mProgressDialog.show();
    }
    public boolean isShowLoading() {
        try {
            return mProgressDialog.isShowing();
        } catch (Exception ignored) {
        }
        return false;
    }
    private ProgressDialog showLoadingDialog(Context context, boolean cancelable) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.TransparentDialogTheme);
        progressDialog.show();
        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        progressDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

//    public void showSnackbar(String message){
//        com.google.android.material.snackbar.Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction("Action", null).show();
//    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
////        super.attachBaseContext(LanguageLocalManager.setLocal(newBase));
//    }

    public boolean setDoubleClick(){
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

}
