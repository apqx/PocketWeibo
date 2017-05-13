package me.apqx.pocketweibo;

/**
 * Created by apqx on 2017/5/9.
 * 用于从微博列表中取出指定的微博，要考虑到转发的微博。
 * 转发的微博的所有信息已经包含在该微博中。
 */

public class KeyForWeiboDetail {
    private boolean reTwitter;
    private int index;
    public KeyForWeiboDetail(boolean reTwitter,int index){
        this.reTwitter=reTwitter;
        this.index=index;
    }
    public int getIndex() {
        return index;
    }

    public boolean isReTwitter() {
        return reTwitter;
    }
}
