package me.apqx.pocketweibo.struct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by apqx on 2017/5/16.
 * 表示微博中的图片
 */

public class PicUrls {
    private List<String> urlList;
    public PicUrls(List<String> urlList){
        this.urlList = urlList;
    }
    public int getImageCount(){
        return urlList.size();
    }

    public String getSmallImageUrlAt(int index){
        if (index<0||index>= urlList.size()){
            throw new IndexOutOfBoundsException();
        }
        return urlList.get(index);
    }

    public String getMiddleImageUrlAt(int index){
        if (index<0||index>= urlList.size()){
            throw new IndexOutOfBoundsException();
        }
        return urlList.get(index).replace("thumbnail","bmiddle");
    }
    public String getOriginalImageUrlAt(int index){
        if (index<0||index>= urlList.size()){
            throw new IndexOutOfBoundsException();
        }
        return urlList.get(index).replace("thumbnail","large");
    }

    @Override
    public String toString() {
        JSONArray jsonArray=new JSONArray();
        try {
            for (int i = 0; i< urlList.size(); i++){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put(ParseJsonTools.IMAGE_SMALL,getSmallImageUrlAt(i));
                jsonArray.put(jsonObject);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonArray.toString();
    }
}
