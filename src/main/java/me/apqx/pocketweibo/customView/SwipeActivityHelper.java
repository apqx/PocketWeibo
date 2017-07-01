package me.apqx.pocketweibo.customView;

import android.app.Activity;

/**
 * Created by apqx on 2017/5/17.
 * 用于实现Activity滑动退出的帮助类
 */

public class SwipeActivityHelper {
    private Activity activity;
    private SwipeActivityLayout swipeActivityLayout;
    public SwipeActivityHelper(Activity activity){
        this.activity=activity;
    }
    public void onActivityCreate(){
        swipeActivityLayout=new SwipeActivityLayout(activity);

    }
    public void onPostCreate(){
        swipeActivityLayout.onAttachActivity(activity);
    }

    public void setOnFinishActivity(SwipeActivityLayout.OnFinishActivity onFinishActivity){
        swipeActivityLayout.setOnFinishActivity(onFinishActivity);
    }

}
