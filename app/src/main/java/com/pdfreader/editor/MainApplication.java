package com.pdfreader.editor;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.pdfreader.editor.utils.Utils;

import io.fabric.sdk.android.Fabric;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Utils.initial(this);
    }
}
