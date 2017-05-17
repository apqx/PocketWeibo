package me.apqx.pocketweibo.struct;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import me.apqx.pocketweibo.tools.Tools;

/**
 * Created by apqx on 2017/5/6.
 * 表示一条微博数据结构的类
 */

public class WeiboItemData {
    private String createTime;
    private String device;
    private String content;
    private String rePostCount;
    private String commentCount;
    private String likeCount;
    private String weiboId;
    private boolean favorited;
    private WeiboItemData reTwitterWeibo;
    private UserData weiboUserData;
    private PicUrls picUrls;
    private WeiboItemData(Builder builder){
        this.createTime=builder.createTime;
        this.device=builder.device;
        this.content =builder.content;
        this.rePostCount=builder.rePostCount;
        this.commentCount=builder.commentCount;
        this.likeCount=builder.likeCount;
        this.weiboId=builder.weiboId;
        this.favorited=builder.favorited;
        this.reTwitterWeibo=builder.reTwitterWeibo;
        this.weiboUserData=builder.weiboUserData;
        this.picUrls=builder.picUrls;
    }
    public boolean hasReTwitter(){
        return reTwitterWeibo!=null;
    }

    public boolean hasPics(){
        return picUrls!=null;
    }

    public String getDevice() {
        return Tools.parseDevice(device);
    }

    public String getContent() {
        return content;
    }

    public String getCreateTime() {
        return Tools.parseTime(createTime);
    }

    public String getLikeCount() {
        return likeCount;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public String getRePostCount() {
        return rePostCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getWeiboId() {
        return weiboId;
    }

    public WeiboItemData getReTwitterWeibo() {
        return reTwitterWeibo;
    }

    public UserData getWeiboUserData() {
        return weiboUserData;
    }

    public PicUrls getPicUrls() {
        return picUrls;
    }

    @Override
    public String toString() {
        JSONObject jsonObject=null;
        try {
            jsonObject=new JSONObject();
            jsonObject.put(ParseJsonTools.CREATE_TIME,createTime);
            jsonObject.put(ParseJsonTools.DEVICE,device);
            jsonObject.put(ParseJsonTools.CONTENT,content);
            jsonObject.put(ParseJsonTools.REPOST_COUNT,rePostCount);
            jsonObject.put(ParseJsonTools.COMMENT_COUNT,commentCount);
            jsonObject.put(ParseJsonTools.LIKE_COUNT,likeCount);
            jsonObject.put(ParseJsonTools.WEIBO_ID,weiboId);
            jsonObject.put(ParseJsonTools.ISFAVORITED,favorited);
            jsonObject.put(ParseJsonTools.USER,ParseJsonTools.getJSONObjectFromString(weiboUserData.toString()));
            if (hasReTwitter()){
                jsonObject.put(ParseJsonTools.RETWITTER,ParseJsonTools.getJSONObjectFromString(reTwitterWeibo.toString()));
            }
            if (hasPics()){
                jsonObject.put(ParseJsonTools.IMAGES_SMALL,ParseJsonTools.getImagesFromString(picUrls.toString()));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static class Builder{
        private final String DEFAULT="Default";
        private String createTime;
        private String device;
        private String content;
        private String rePostCount;
        private String commentCount;
        private String likeCount;
        private String weiboId;
        private boolean favorited;
        private WeiboItemData reTwitterWeibo;
        private UserData weiboUserData;
        private PicUrls picUrls;
        public Builder setCreateTime(String createTime){
            this.createTime=createTime;
            return this;
        }

        public Builder setDevice(String device) {
            this.device = device;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setRePostCount(String rePostCount) {
            this.rePostCount = rePostCount;
            return this;
        }

        public Builder setCommentCount(String commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public Builder setLikeCount(String likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder setWeiboId(String weiboId) {
            this.weiboId = weiboId;
            return this;
        }

        public Builder setFavorited(boolean favorited) {
            this.favorited = favorited;
            return this;
        }

        public Builder setReTwitterWeibo(WeiboItemData reTwitterWeibo) {
            this.reTwitterWeibo = reTwitterWeibo;
            return this;
        }

        public Builder setWeiboUserData(UserData weiboUserData) {
            this.weiboUserData = weiboUserData;
            return this;
        }

        public Builder setPicUrls(PicUrls picUrls) {
            this.picUrls = picUrls;
            return this;
        }

        public WeiboItemData build(){
            return new WeiboItemData(this);
        }
    }

}
