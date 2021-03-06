package me.apqx.pocketweibo.bean;

import com.google.gson.annotations.SerializedName;

import me.apqx.pocketweibo.model.FileTools;

/**
 * Created by apqx on 2017/5/14.
 * 评论的封装
 */

public class CommentData {
    @SerializedName("user")
    private UserData userData;
    @SerializedName("text")
    private String comment;
    @SerializedName("created_at")
    private String commentTime;
    @SerializedName("id")
    private String commentId;
    @SerializedName("source")
    private String device;
    //我暂时无法获取评论的点赞数
    private String likeCount;
    //暂时无法获取各条评论之间的关系

    private CommentData(Builder builder){
        this.userData=builder.userData;
        this.comment=builder.comment;
        this.commentTime=builder.commentTime;
        this.commentId=builder.commentId;
        this.device=builder.device;
        this.likeCount=builder.likeCount;
    }

    public UserData getUserData() {
        return userData;
    }

    public String getComment() {
        return comment;
    }

    public String getCommentTime() {
        return FileTools.parseTime(commentTime);
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getCommentId() {
        return commentId;
    }

    public String getDevice() {
        return FileTools.parseDevice(device);
    }

    public static class Builder{
        private String DEFAULT="default";

        private UserData userData;
        private String comment;
        private String commentTime;
        private String commentId;
        private String device;
        private String likeCount=DEFAULT;

        public Builder setUserData(UserData userData){
            this.userData=userData;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setCommentTime(String commentTime) {
            this.commentTime = commentTime;
            return this;
        }

        public Builder setLikeCount(String likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder setCommentId(String commentId) {
            this.commentId = commentId;
            return this;
        }

        public Builder setDevice(String device) {
            this.device = device;
            return this;
        }

        public CommentData build(){
            return new CommentData(this);
        }
    }

}
