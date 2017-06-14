package me.apqx.pocketweibo.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import me.apqx.pocketweibo.struct.ParseJsonTools;

/**
 * Created by apqx on 2017/5/6.
 */

public class UserData {
    private boolean isNull;
    @SerializedName("screen_name")
    private String userName;
    @SerializedName("avatar_large")
    private String userHeadPicURL;
    @SerializedName("cover_image_phone")
    private String profileBGUrl;
    @SerializedName("description")
    private String profileDescription;
    @SerializedName("location")
    private String location;
    @SerializedName("friends_count")
    private String followingCount;
    @SerializedName("followers_count")
    private String followerCount;
    @SerializedName("domain")
    private String websiteUrl;
    @SerializedName("gender")
    private String gender;
    @SerializedName("id")
    private String userId;
    @SerializedName("following")
    private boolean isFollowed;
    public UserData(Builder builder){
        this.userName=builder.userName;
        this.userHeadPicURL=builder.userHeadPicURL;
        this.profileBGUrl=builder.profileBGUrl;
        this.profileDescription=builder.profileDescription;
        this.location=builder.location;
        this.followingCount=builder.followingCount;
        this.followerCount=builder.followerCount;
        this.websiteUrl=builder.websiteUrl;
        this.gender=builder.gender;
        this.isFollowed=builder.isFollowed;
        this.userId=builder.userId;
    }
    public UserData(boolean isNull){
        this.isNull=isNull;
    }

    public boolean isNull() {
        return isNull;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserHeadPicURL() {
        return userHeadPicURL;
    }

    public String getProfileBGUrl() {
        if (profileBGUrl!=null&&profileBGUrl.contains(";")){
            return profileBGUrl.split(";")[0];
        }
        return profileBGUrl;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public String getLocation() {
        return location;
    }

    public String getFollowerCount() {
        return followerCount;
    }

    public String getFollowingCount() {
        return followingCount;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getGender() {
        return gender;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isFollowed(){
        return isFollowed;
    }

    @Override
    public String toString() {
        Gson gson=new Gson();
        String string=gson.toJson(this);
        return string;
    }

    public static class Builder{
        private final String DEFAULT="Default";
        private String userName;
        private String userHeadPicURL;
        private String profileBGUrl;
        private String profileDescription;
        private String location;
        private String followingCount;
        private String followerCount;
        private String websiteUrl;
        private String gender;
        private String userId;
        private boolean isFollowed;

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setUserHeadPicURL(String userHeadPicURL) {
            this.userHeadPicURL = userHeadPicURL;
            return this;
        }

        public Builder setProfileBGUrl(String profileBGUrl) {
            this.profileBGUrl = profileBGUrl;
            return this;
        }

        public Builder setProfileDescription(String profileDescription) {
            this.profileDescription = profileDescription;
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setFollowingCount(String followingCount) {
            this.followingCount = followingCount;
            return this;
        }

        public Builder setFollowerCount(String followerCount) {
            this.followerCount = followerCount;
            return this;
        }

        public Builder setWebsiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
            return this;
        }

        public Builder setFollowed(boolean followed) {
            isFollowed = followed;
            return this;
        }

        public Builder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserData build(){
            return new UserData(this);
        }
    }
}
