package me.apqx.pocketweibo;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by apqx on 2017/5/4.
 * 自定义的Application
 */

public class MyApplication extends Application {
    public static final int THEME_LIGHT=0;
    public static final int THEME_DARK=1;
    private static Context context;
    private static int myTheme;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        Fresco.initialize(this);
    }
    public static Context getContext(){
        return context;
    }
    public static void setMyTheme(int theme){
        myTheme=theme;
    }
    public static int getMyTheme(){
        return myTheme;
    }
}
