package me.apqx.pocketweibo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import me.apqx.pocketweibo.view.SwipeActivityHelper;
import me.apqx.pocketweibo.view.SwipeActivityLayout;

/**
 * Created by apqx on 2017/5/17.
 */

public class TempActivity extends Activity{
    private SwipeActivityHelper swipeActivityHelper;
    private float percent=0.1f;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_temp);
        swipeActivityHelper=new SwipeActivityHelper(this);
        swipeActivityHelper.onActivityCreate();

        ImageView imageView=(ImageView)findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setBackgroundColor(getBackColor(percent));
                percent=percent+0.1f;
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        swipeActivityHelper.onPostCreate();
        swipeActivityHelper.setOnFinishActivity(new SwipeActivityLayout.OnFinishActivity() {
            @Override
            public void finishActivity() {
                TempActivity.this.finish();
                Log.d("apqx","finish");
            }
        });
    }
    //动态计算颜色的ARGB中的A,传入百分比，0表示全透明，1表示不透明
    private int getBackColor(float percent){
        int color= Color.argb(0xff,(int)(percent*0xff),0,0);
        return color;
    }
}
