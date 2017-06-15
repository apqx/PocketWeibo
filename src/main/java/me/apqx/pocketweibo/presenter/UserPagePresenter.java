package me.apqx.pocketweibo.presenter;

import android.support.annotation.Nullable;

import com.google.gson.Gson;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.Weibos;
import me.apqx.pocketweibo.model.ViewTools;
import me.apqx.pocketweibo.model.WeiboServer;
import me.apqx.pocketweibo.view.IUserDataView;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by apqx on 2017/6/14.
 */

public class UserPagePresenter implements IUserPagePresenter{
    private IUserDataView userDataView;
    private WeiboServer weiboServer;
    public UserPagePresenter(IUserDataView userDataView){
        this.userDataView=userDataView;
        Gson gson=new Gson();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://api.weibo.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        weiboServer=retrofit.create(WeiboServer.class);
    }
    @Override
    public void refreshUserData(@Nullable String userName, @Nullable String uid) {
        weiboServer.getUserData(Constant.accessToken.getToken(),userName,uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserData>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UserData userData) {
                        if (userData!=null){
                            //读取成功
                            userDataView.showUserData(userData);
                        }else {
                            //读取失败
                            ViewTools.showToast(R.string.refresh_userdata_failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void refreshUserWeibo(String userName) {
        weiboServer.getUserWeibos(Constant.accessToken.getToken(),userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Weibos>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Weibos weibos) {
                        if (weibos!=null){
                            //读取成功
                            userDataView.notifyWeiboDataChanged(weibos.getList(),true);
                        }else {
                            //读取失败
                            ViewTools.showToast(R.string.api_limit_get_weibo_failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //读取失败
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
