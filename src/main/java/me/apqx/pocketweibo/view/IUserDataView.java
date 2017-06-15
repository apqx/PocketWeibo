package me.apqx.pocketweibo.view;

import java.util.List;

import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;

/**
 * Created by apqx on 2017/6/14.
 */

public interface IUserDataView {
    void showUserData(UserData userData);
    void notifyWeiboDataChanged(List<WeiboItemData> list,boolean isNew);
}
