package me.apqx.pocketweibo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;

import me.apqx.pocketweibo.view.SwipeActivityHelper;
import me.apqx.pocketweibo.view.SwipeActivityLayout;

/**
 * Created by apqx on 2017/6/4.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private SwipeActivityHelper swipeActivityHelper;
    private TextView textViewLogout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApplication.getMyTheme()==MyApplication.THEME_DARK){
            setTheme(R.style.AppTheme_Dark_Transparent);
        }else {
            setTheme(R.style.AppTheme_Light_Transparent);
        }
        setContentView(R.layout.layout_settings);

        swipeActivityHelper=new SwipeActivityHelper(this);
        swipeActivityHelper.onActivityCreate();

        textViewLogout=(TextView)findViewById(R.id.textView_logout);
        setListener();
    }
    private void setListener(){
        textViewLogout.setOnClickListener(this);
    }
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        swipeActivityHelper.onPostCreate();
        swipeActivityHelper.setOnFinishActivity(new SwipeActivityLayout.OnFinishActivity() {
            @Override
            public void finishActivity() {
                SettingActivity.this.finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.textView_logout:
                AccessTokenKeeper.clear(this);
                finish();
                startActivity(new Intent(this,SplashActivity.class));
                break;

        }
    }
}
