package me.apqx.pocketweibo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;

import me.apqx.pocketweibo.model.Settings;
import me.apqx.pocketweibo.customView.SwipeActivityHelper;
import me.apqx.pocketweibo.customView.SwipeActivityLayout;

/**
 * Created by apqx on 2017/6/4.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener , CompoundButton.OnCheckedChangeListener{

    private static final String TAG="SettingActivity";
    private SwipeActivityHelper swipeActivityHelper;
    private TextView textViewLogout;
    private Switch switchService;
    private Switch switchImage;
    private TextView textViewAbout;
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
        textViewAbout=(TextView)findViewById(R.id.textView_about);
        switchService=(Switch)findViewById(R.id.switch_service);
        switchImage=(Switch)findViewById(R.id.switch_image);
        Settings settings=new Settings(this);
        switchService.setChecked(settings.getServiceOn());
        switchImage.setChecked(settings.getNoLoadImageOnLte());
        setListener();
    }
    private void setListener(){
        textViewLogout.setOnClickListener(this);
        textViewAbout.setOnClickListener(this);
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
        switchService.setOnCheckedChangeListener(this);
        switchImage.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.textView_logout:
                AccessTokenKeeper.clear(this);
                finish();
                startActivity(new Intent(this,SplashActivity.class));
                break;
            case R.id.textView_about:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("口袋微博")
                        .setMessage("轻量级的第三方微博客户端，召之即来，挥之即去。\nVersion = 1.0");
                AlertDialog dialog=builder.create();
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);
                break;
            

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Settings settings=new Settings(SettingActivity.this);
        switch (buttonView.getId()){
            case R.id.switch_service:
                settings.setServiceOn(isChecked);
                break;
            case R.id.switch_image:
                settings.setNoLoadImageOnLte(isChecked);
                break;
        }
    }
}
