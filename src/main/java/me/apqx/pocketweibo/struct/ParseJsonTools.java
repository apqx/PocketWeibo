package me.apqx.pocketweibo.struct;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import me.apqx.pocketweibo.tools.Tools;

/**
 * Created by apqx on 2017/5/10.
 */

public class ParseJsonTools {
    static final String TAG="ParseJsonTools";
    static final String USER="user";
    static final String RETWITTER="retweeted_status";
    static final String DEVICE="source";
    static final String CONTENT="text";
    static final String CREATE_TIME="created_at";
    static final String LIKE_COUNT="favourites_count";
    static final String ISFAVORITED="favorited";
    static final String REPOST_COUNT="reposts_count";
    static final String COMMENT_COUNT="comments_count";
    static final String WEIBO_ID="id";

    static final String MALE="m";
    static final String FEMALE="f";
    static final String USERNAME="screen_name";
    static final String USER_HEAD_PIC_URL="avatar_large";
    static final String PROFILE_BG_URL="profile_image_url";
    static final String PROFILE_DESCRIPTION="description";
    static final String LOCATION="location";
    static final String FOLLOWING_COUNT="friends_count";
    static final String FOLLOWERS_COUNT="followers_count";
    static final String WEBSITE_URL="domain";
    static final String GENDER="gender";
    static final String ISFOLLOWING="following";

    public static WeiboItemData getWeiboFromJson(JSONObject jsonObject){
        WeiboItemData.Builder builder=new WeiboItemData.Builder();
        try {
            builder.setCreateTime(jsonObject.getString(CREATE_TIME))
                    .setDevice(jsonObject.getString(DEVICE))
                    .setContent(jsonObject.getString(CONTENT))
                    .setRePostCount(jsonObject.getString(REPOST_COUNT))
                    .setCommentCount(jsonObject.getString(COMMENT_COUNT))
                    .setLikeCount(jsonObject.getJSONObject(USER).getString(LIKE_COUNT))
                    .setWeiboId(jsonObject.getString(WEIBO_ID))
                    .setFavorited(jsonObject.getBoolean(ISFAVORITED))
                    .setWeiboUserData(getUserDataFromJson(jsonObject.getJSONObject(USER)));
            if (jsonObject.has(RETWITTER)){
                builder.setReTwitterWeibo(getWeiboFromJson(jsonObject.getJSONObject(RETWITTER)));
                Log.d(TAG,"reTwitter");
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return builder.build();
    }

    public static UserData getUserDataFromJson(JSONObject jsonObject){
        UserData.Builder builder=new UserData.Builder();
        try {
            builder.setUserName(jsonObject.getString(USERNAME))
                    .setUserHeadPicURL(jsonObject.getString(USER_HEAD_PIC_URL))
                    .setProfileBGUrl(jsonObject.getString(PROFILE_BG_URL))
                    .setProfileDescription(jsonObject.getString(PROFILE_DESCRIPTION))
                    .setLocation(jsonObject.getString(LOCATION))
                    .setFollowingCount(jsonObject.getString(FOLLOWING_COUNT))
                    .setFollowerCount(jsonObject.getString(FOLLOWERS_COUNT))
                    .setWebsiteUrl(jsonObject.getString(WEBSITE_URL))
                    .setGender(jsonObject.getString(GENDER))
                    .setFollowed(jsonObject.getBoolean(ISFOLLOWING));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return builder.build();
    }

    public static JSONObject getReTwitterJsonFromJson(JSONObject jsonObject){
        JSONObject object=null;
        try {
            object=jsonObject.getJSONObject(RETWITTER);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return object;
    }

    public static JSONObject getJSONObjectFromString(String jsonString){
        JSONObject object=null;
        try {
            object=new JSONObject(jsonString);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return object;
    }

}
