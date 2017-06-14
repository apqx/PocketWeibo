package me.apqx.pocketweibo.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by apqx on 2017/6/14.
 */

public class Weibos {
    @SerializedName("statuses")
    private List<WeiboItemData> list;

    public List<WeiboItemData> getList() {
        return list;
    }

}
