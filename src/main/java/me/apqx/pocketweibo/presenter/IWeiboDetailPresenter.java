package me.apqx.pocketweibo.presenter;

/**
 * Created by apqx on 2017/6/14.
 */

public interface IWeiboDetailPresenter {
    void refreshNewComment(String weiboId);
    void refreshOldComment(String weiboId,String maxId);
    void newComment(String weiboId,String comment);
}
