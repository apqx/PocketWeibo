package me.apqx.pocketweibo;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by apqx on 2017/5/4.
 * 自定义的Application
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        Fresco.initialize(this);
    }
    public static Context getContext(){
        return context;
    }
}
