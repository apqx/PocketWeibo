package me.apqx.pocketweibo.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by apqx on 2017/6/14.
 * 表示微博中的单个图片
 */

public class PicUrl {
    @SerializedName("thumbnail_pic")
    private String picUrl;
    public PicUrl(String picUrl){
        this.picUrl=picUrl;
    }
    public String getSmallImageUrl(){

        return picUrl;
    }

    public String getMiddleImageUrl(){

        return picUrl.replace("thumbnail","bmiddle");
    }
    public String getOriginalImageUrl(){

        return picUrl.replace("thumbnail","large");
    }

    public String getPicUrl() {
        return picUrl;
    }
}
