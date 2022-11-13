package com.example.disconnect.ui.Auth;

import android.app.Application;

/**
 * Created by Mathias Seguy - Android2EE on 04/01/2017.
 */
public class MyApplication extends Application {
    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }
}
