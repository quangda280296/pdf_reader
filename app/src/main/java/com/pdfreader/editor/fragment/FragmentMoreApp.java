package com.pdfreader.editor.fragment;


import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageView;

import com.pdfreader.editor.base.BaseFragment;
import com.pdfreader.editor.utils.Constants;
import com.pdfreader.pdf.reader.editor.R;

import butterknife.BindView;
import butterknife.OnClick;


public class FragmentMoreApp extends BaseFragment {

    private static final String POLICY = "http://smarttoolstudio.online/pdf-reader/privacy-policy.html";

    @BindView(R.id.img_back)
    ImageView imageView_back;
    @BindView(R.id.webView)
    WebView webView;

    private int versioncode = 0;
    private String typeLayout = "";

    public static boolean isDie = false;

    @Override
    public int getViewResource() {
        return R.layout.fragment_about;
    }

    @Override
    public void setListener() {
        isDie = true;
        getData();

        webView.loadUrl(POLICY);

    }

    private void getData() {
        Bundle bundle = getArguments();
        versioncode = bundle.getInt(Constants.VERSION_CODE, 1);
        typeLayout = bundle.getString(Constants.TYPE_LAYOUT);
    }


    @Override
    public void retrieveData() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDie = false;
    }

    @OnClick(R.id.img_back)
    public void eventBack(){
        getActivity().onBackPressed();
    }
}
