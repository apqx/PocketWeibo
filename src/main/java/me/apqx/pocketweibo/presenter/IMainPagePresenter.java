package me.apqx.pocketweibo.presenter;

import android.support.annotation.Nullable;

import java.util.List;

import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;

/**
 * Created by apqx on 2017/6/14.
 */

public interface IMainPagePresenter {
    void refreshNewWeibo();
    void refreshOldWeibo(String weiboId);
    void saveWeibosToLocal(List<WeiboItemData> list,String uid);
    void readWeiboFromLocal(String uid);
    void refreshUserDataFromWeb(@Nullable String userName, @Nullable String uid);
    void saveUserDataToLocal(UserData userData);
    void readUserDataFromLocal(String uid);
}
