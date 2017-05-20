package me.apqx.pocketweibo.struct;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    static final String IMAGES_SMALL="pic_urls";
    static final String IMAGE_SMALL="thumbnail_pic";


    static final String MALE="m";
    static final String FEMALE="f";
    static final String USERNAME="screen_name";
    static final String USER_HEAD_PIC_URL="avatar_large";
    static final String PROFILE_BG_URL="cover_image_phone";
    static final String PROFILE_DESCRIPTION="description";
    static final String LOCATION="location";
    static final String FOLLOWING_COUNT="friends_count";
    static final String FOLLOWERS_COUNT="followers_count";
    static final String WEBSITE_URL="domain";
    static final String GENDER="gender";
    static final String ISFOLLOWING="following";
    static final String USER_ID="id";


    static final String COMMENTS="text";
    static final String COMMENT_ID="id";
    static final String COMMENT_TIME="created_at";
    static final String COMMENT_DEVICE="source";

    public static WeiboItemData getWeiboFromJson(JSONObject jsonObject){
        WeiboItemData.Builder builder=new WeiboItemData.Builder();
        try {
            builder.setCreateTime(jsonObject.getString(CREATE_TIME));
            builder.setDevice(jsonObject.getString(DEVICE));
            builder.setContent(jsonObject.getString(CONTENT));
            builder.setRePostCount(jsonObject.getString(REPOST_COUNT));
            builder.setCommentCount(jsonObject.getString(COMMENT_COUNT));
            builder.setLikeCount(jsonObject.getJSONObject(USER).getString(LIKE_COUNT));
            builder.setWeiboId(jsonObject.getString(WEIBO_ID));
            builder.setFavorited(jsonObject.getBoolean(ISFAVORITED));
            builder.setWeiboUserData(getUserDataFromJson(jsonObject.getJSONObject(USER)));
            if (jsonObject.has(RETWITTER)){
                builder.setReTwitterWeibo(getWeiboFromJson(jsonObject.getJSONObject(RETWITTER)));
            }
            if (jsonObject.has(IMAGES_SMALL)){
                builder.setPicUrls(getPicUrlsFromJsonArray(jsonObject.getJSONArray(IMAGES_SMALL)));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return builder.build();
    }

    public static CommentData getCommentDataFromJaon(JSONObject jsonObject){
        CommentData.Builder builder=new CommentData.Builder();
        try {
            builder.setComment(jsonObject.getString(COMMENTS));
            builder.setCommentId(jsonObject.getString(COMMENT_ID));
            builder.setCommentTime(jsonObject.getString(COMMENT_TIME));
            builder.setDevice(jsonObject.getString(COMMENT_DEVICE));
            builder.setUserData(getUserDataFromJson(jsonObject.getJSONObject(USER)));
        }catch (JSONException e){
            e.printStackTrace();
        }
        return builder.build();
    }

    public static UserData getUserDataFromJson(JSONObject jsonObject){
        UserData.Builder builder=new UserData.Builder();
        try {
            builder.setUserName(jsonObject.getString(USERNAME));
            builder.setUserHeadPicURL(jsonObject.getString(USER_HEAD_PIC_URL));

            if (jsonObject.isNull(PROFILE_BG_URL)){
                builder.setProfileBGUrl("http://ww2.sinaimg.cn//crop.0.0.640.640.640//a1d3feabjw1ecat8op0e1j20hs0hswgu.jpg");
            }else {
                builder.setProfileBGUrl(jsonObject.getString(PROFILE_BG_URL));

            }
            builder.setProfileDescription(jsonObject.getString(PROFILE_DESCRIPTION));
            builder.setLocation(jsonObject.getString(LOCATION));
            builder.setFollowingCount(jsonObject.getString(FOLLOWING_COUNT));
            builder.setFollowerCount(jsonObject.getString(FOLLOWERS_COUNT));
            builder.setWebsiteUrl(jsonObject.getString(WEBSITE_URL));
            builder.setGender(jsonObject.getString(GENDER));
            builder.setLikeCount(jsonObject.getString(LIKE_COUNT));
            builder.setFollowed(jsonObject.getBoolean(ISFOLLOWING));
            builder.setUserId(jsonObject.getString(USER_ID));
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

    public static JSONArray getImagesFromString(String jsonString){
        JSONArray jsonArray=null;
        try {
            jsonArray=new JSONArray(jsonString);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static PicUrls getPicUrlsFromJsonArray(JSONArray jsonArray){
        List<String> list=new ArrayList<String>();
        if (jsonArray.length()==0){
            return null;
        }
        try {
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                list.add(jsonObject.getString(IMAGE_SMALL));
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
        return new PicUrls(list);
    }

}
