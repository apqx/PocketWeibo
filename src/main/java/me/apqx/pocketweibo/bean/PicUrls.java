package me.apqx.pocketweibo.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import me.apqx.pocketweibo.struct.ParseJsonTools;

/**
 * Created by apqx on 2017/5/16.
 * 表示微博中的所有图片
 */

public class PicUrls {
    @SerializedName("pic_urls")
    private List<PicUrl> urlList;
    public PicUrls(List<PicUrl> urlList){
        this.urlList = urlList;
    }
    public int getImageCount(){
        return urlList.size();
    }

    public String getSmallImageUrlAt(int index){
        if (index<0||index>= urlList.size()){
            throw new IndexOutOfBoundsException();
        }
        return urlList.get(index).getPicUrl();
    }

    public String getMiddleImageUrlAt(int index){
        if (index<0||index>= urlList.size()){
            throw new IndexOutOfBoundsException();
        }
        return urlList.get(index).getPicUrl().replace("thumbnail","bmiddle");
    }
    public String getOriginalImageUrlAt(int index){
        if (index<0||index>= urlList.size()){
            throw new IndexOutOfBoundsException();
        }
        return urlList.get(index).getPicUrl().replace("thumbnail","large");
    }

    @Override
    public String toString() {
        Gson gson=new Gson();
        String jsonArray=gson.toJson(this);
        return jsonArray;
    }
}
