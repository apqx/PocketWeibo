package me.apqx.pocketweibo.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by apqx on 2017/6/8.
 */

public class Settings {
    public static final String SERVICE_ON="service_on";
    public static final String NO_LOAD_IMAGE_ON_LTE="no_image_on_lte";
    public static boolean NO_IMAGE_ON_LTE;

    private SharedPreferences sharedPreferences;
    public Settings(Context context){
        sharedPreferences=context.getSharedPreferences("Settings",Context.MODE_PRIVATE);
        getNoLoadImageOnLte();
    }
    public boolean setServiceOn(boolean flag){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(SERVICE_ON,flag);
        return editor.commit();
    }
    public boolean getServiceOn(){
        return sharedPreferences.getBoolean(SERVICE_ON,false);
    }

    public boolean setNoLoadImageOnLte(boolean flag){
        NO_IMAGE_ON_LTE=flag;
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(NO_LOAD_IMAGE_ON_LTE,flag);
        return editor.commit();
    }

    public boolean getNoLoadImageOnLte(){
        boolean flag=sharedPreferences.getBoolean(NO_LOAD_IMAGE_ON_LTE,false);
        NO_IMAGE_ON_LTE=flag;
        return flag;
    }


}
