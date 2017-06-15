package me.apqx.pocketweibo.presenter;

import android.support.annotation.Nullable;

/**
 * Created by apqx on 2017/6/14.
 */

public interface IUserPagePresenter {
    void refreshUserData(@Nullable String userName, @Nullable String uid);
    void refreshUserWeibo(String userName);
}
