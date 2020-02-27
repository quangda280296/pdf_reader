package com.pdfreader.editor.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment implements View.OnTouchListener {

    private View containerView;
    private BaseActivity mBaseActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) context;
            mBaseActivity = activity;
            activity.onFragmentAttached();
        }
    }

    //region Abstract method
    public abstract int getViewResource();

    public abstract void setListener();

    public abstract void retrieveData();
    //endregion


    protected BaseActivity getBaseActivity() {
        return mBaseActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleArguments(getArguments());
        getBaseActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    public void handleArguments(Bundle arguments) {

    }

    //region Override method
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int resource = getViewResource();
        if (resource == 0)
            return super.onCreateView(inflater, container, savedInstanceState);
        containerView = View.inflate(getActivity(), resource, null);
        ButterKnife.bind(this, containerView);
        containerView.setOnTouchListener(this);

        containerView.setClickable(true);
        containerView.setFocusableInTouchMode(true);

        setListener();
        setUpCreatingView(containerView);
        return containerView;
    }

    public void setUpCreatingView(View containerView) {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        retrieveData();
    }

    public View getContainerView() {
        return containerView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        hideKeyboard();
        return true;
    }

    public void hideKeyboard() {
        View view = this.getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    public void hideKeyboardFrom( View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showProgressDialog() {
        if (mBaseActivity != null) {
            mBaseActivity.showProgressDialog();
        }
    }

    public void hideProgressDialog() {
        if (mBaseActivity != null) {
            mBaseActivity.hideProgressDialog();
        }
    }


    public void showErrorMessage(String message) {
        if (getBaseActivity() != null) {
 //           getBaseActivity().showErrorMessage(message);
        }
    }

    public void showErrorMessage(int messageId) {
        if (getBaseActivity() != null) {
//            getBaseActivity().showErrorMessage(messageId);
        }
    }
}

