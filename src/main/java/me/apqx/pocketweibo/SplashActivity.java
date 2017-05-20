package me.apqx.pocketweibo;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.AccessTokenKeeper;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;

import me.apqx.pocketweibo.tools.Tools;

/**
 * Created by apqx on 2017/5/2.
 * A Splash activity to show a welcome page.
 */

public class SplashActivity extends Activity {
    private static final String TAG="SplashActivity";
    private LinearLayout linearLayoutLogo;
    private SsoHandler ssoHandler;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.init();
        setContentView(R.layout.layout_splash);
        linearLayoutLogo=(LinearLayout) findViewById(R.id.linearLayout_logo);

        Constant.authInfo=new AuthInfo(this,Constant.APP_KEY,Constant.REDIRECT_URL,Constant.SCOPE);
        ssoHandler=new SsoHandler(this,Constant.authInfo);

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimatorSet animatorSet= (AnimatorSet)AnimatorInflater.loadAnimator(this,R.animator.splash_logo_anim);
        animatorSet.setTarget(linearLayoutLogo);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //开始页动画执行完毕后，读取本地文件，判断是否应该引导用户授权
                Constant.accessToken=AccessTokenKeeper.readAccessToken(SplashActivity.this);
                if (Constant.accessToken!=null&&Constant.accessToken.isSessionValid()){

                    //不需要授权，直接启动主页面
                    Intent intent=new Intent(new Intent(SplashActivity.this,MainPageActivity.class));
                    intent.putExtra("uid",Constant.accessToken.getUid());
                    startActivity(intent);
                }else {
                    //引导用户授权
                    ssoHandler.authorizeWeb(new AuthListener());
                }
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ssoHandler!=null){
            ssoHandler.authorizeCallBack(requestCode,resultCode,data);
        }
    }

    /**
     * 授权结束后的回调
     */
    class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle bundle) {
            Log.d(TAG,"onComplete");
            Constant.accessToken=Oauth2AccessToken.parseAccessToken(bundle);
            if (Constant.accessToken.isSessionValid()){
                //授权成功，将获得的token保存到本地
                AccessTokenKeeper.writeAccessToken(SplashActivity.this,Constant.accessToken);
                Tools.showToast(R.string.authority_success);
                startActivity(new Intent(SplashActivity.this,MainPageActivity.class));
            }else {
                //授权失败，获取失败码
                String code=bundle.getString("code");
                Log.d(TAG,"authority error code is "+code);
                if (!TextUtils.isEmpty(code)){
                    Tools.showToast(getString(R.string.authority_failed)+" code is "+code);
                }
                //重新授权
                ssoHandler.authorizeWeb(new AuthListener());
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Log.d(TAG,"onException");
            //授权失败，重新授权
            ssoHandler.authorizeWeb(new AuthListener());
        }

        @Override
        public void onCancel() {
            Log.d(TAG,"onCancel");
            //用户拒绝授权，跳转到主页面，token为null
            startActivity(new Intent(SplashActivity.this,MainPageActivity.class));
        }
    }
}
