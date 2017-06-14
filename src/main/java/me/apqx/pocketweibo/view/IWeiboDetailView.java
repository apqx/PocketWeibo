package me.apqx.pocketweibo.view;

import java.util.List;

import me.apqx.pocketweibo.bean.CommentData;

/**
 * Created by apqx on 2017/6/14.
 */

public interface IWeiboDetailView {
    void notifyCommentListChanged(List<CommentData> list,boolean isNew);
}
