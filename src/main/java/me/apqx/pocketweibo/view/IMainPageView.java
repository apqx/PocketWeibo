package me.apqx.pocketweibo.view;

import java.util.List;

import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;

/**
 * Created by apqx on 2017/6/14.
 */

public interface IMainPageView {
    void notifyWeiboListChanged(List<WeiboItemData> weiboItemDataList,boolean isNew,boolean isFromLocal);
    void toggleSwipeRefreshIfIsRefreshing();
    void toggleSwipeRefreshIfNoRefreshing();
    void showMainUserData(UserData userData);
}
