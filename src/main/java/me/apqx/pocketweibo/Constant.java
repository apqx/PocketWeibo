package me.apqx.pocketweibo;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

/**
 * Created by apqx on 2017/5/3.
 * 定义一些重要的常量
 */

public class Constant {
    public static final String APP_KEY="471351835";
    public static final String APP_SECRET="6c86cc63fd2153b86ac5f2367cbd7258";
    public static final String REDIRECT_URL="https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE="";
    public static Oauth2AccessToken accessToken;
    public static AuthInfo authInfo;
}
