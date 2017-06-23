package me.apqx.pocketweibo.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import me.apqx.pocketweibo.model.FileTools;

/**
 * Created by apqx on 2017/5/6.
 * 表示一条微博数据结构的类
 */

public class WeiboItemData {
    private boolean isNull;
    @SerializedName("created_at")
    private String createTime;
    @SerializedName("source")
    private String device;
    @SerializedName("text")
    private String content;
    @SerializedName("reposts_count")
    private String rePostCount;
    @SerializedName("comments_count")
    private String commentCount;
    @SerializedName("attitudes_count")
    private String likeCount;
    @SerializedName("id")
    private String weiboId;
    @SerializedName("favorited")
    private boolean favorited;
    @SerializedName("retweeted_status")
    private WeiboItemData reTwitterWeibo;
    @SerializedName("user")
    private UserData weiboUserData;
    @SerializedName("pic_urls")
    private List<PicUrl> picUrls;
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
    public void setNull(){
        isNull=true;
    }

    public boolean isNull(){
        return isNull;
    }

    public boolean hasReTwitter(){
        return reTwitterWeibo!=null;
    }

    public boolean hasPics(){
        return picUrls!=null&&picUrls.size()>0;
    }

    public String getDevice() {
        return FileTools.parseDevice(device);
    }

    public String getContent() {
        return content;
    }

    public String getCreateTime() {
        return FileTools.parseTime(createTime);
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

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
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
        return new PicUrls(picUrls);
    }

    @Override
    public String toString() {
        Gson gson=new Gson();
        String json=gson.toJson(this);
        return json;
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
        private List<PicUrl> picUrls;
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

        public Builder setPicUrls(List<PicUrl> picUrls) {
            this.picUrls = picUrls;
            return this;
        }

        public WeiboItemData build(){
            return new WeiboItemData(this);
        }
    }

}
