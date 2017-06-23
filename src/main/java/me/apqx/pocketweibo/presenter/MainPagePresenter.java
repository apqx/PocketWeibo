package me.apqx.pocketweibo.presenter;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;
import me.apqx.pocketweibo.bean.Weibos;
import me.apqx.pocketweibo.model.FileTools;
import me.apqx.pocketweibo.model.ViewTools;
import me.apqx.pocketweibo.model.WeiboServer;
import me.apqx.pocketweibo.view.IMainPageView;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by apqx on 2017/6/14.
 */

public class MainPagePresenter implements IMainPagePresenter {
    private static final String TAG="MainPagePresenter";
    private IMainPageView mainPageView;
    private WeiboServer weiboServer;
    private Gson gson;
    public MainPagePresenter(IMainPageView mainPageView){
        this.mainPageView=mainPageView;
        gson=new Gson();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://api.weibo.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        weiboServer=retrofit.create(WeiboServer.class);
    }
    @Override
    public void refreshNewWeibo() {
        weiboServer.getWeibos(Constant.accessToken.getToken(),null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Weibos>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Weibos weibos) {
                        List<WeiboItemData> list=weibos.getList();
                        if (list.size()>0){
                            mainPageView.notifyWeiboListChanged(weibos.getList(),true,false);
                            Log.d(TAG,"get new weibo count = "+list.size());
                            ViewTools.showToast(R.string.refresh_weibo);
                        }else {
                            Log.d(TAG,"get new weibo count = "+list.size());
                            ViewTools.showToast(R.string.no_new_weibo);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG,"refresh new weibo error");
                        e.printStackTrace();
                        ViewTools.showToast(R.string.refresh_failed);
                        mainPageView.toggleSwipeRefreshIfIsRefreshing();
                    }

                    @Override
                    public void onComplete() {
                        mainPageView.toggleSwipeRefreshIfIsRefreshing();
                    }
                });
    }

    @Override
    public void refreshOldWeibo(String weiboId) {
        weiboServer.getWeibos(Constant.accessToken.getToken(),weiboId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Weibos>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Weibos weibos) {
                        List<WeiboItemData> list=weibos.getList();
                        if (list.size()>0){
                            mainPageView.notifyWeiboListChanged(weibos.getList(),false,false);
                            Log.d(TAG,"get new weibo count = "+list.size());
                            ViewTools.showToast(R.string.refresh_weibo);
                        }else {
                            Log.d(TAG,"get new weibo count = "+list.size());
                            ViewTools.showToast(R.string.refresh_weibo_failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG,"refresh new weibo error");
                        e.printStackTrace();
                        mainPageView.toggleSwipeRefreshIfIsRefreshing();
                    }

                    @Override
                    public void onComplete() {
                        mainPageView.toggleSwipeRefreshIfIsRefreshing();
                    }
                });
    }

    @Override
    public void readWeiboFromLocal(final String uid) {
        Observable.just(uid)
                .map(new Function<String, List<WeiboItemData>>() {
                    @Override
                    public List<WeiboItemData> apply(@NonNull String s) throws Exception {
                        List<WeiboItemData> list= FileTools.readWeiboListFromLocal(s);
                        return list;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<WeiboItemData>>() {
                    @Override
                    public void accept(@NonNull List<WeiboItemData> list) throws Exception {
                        if (list.size()==0){
                            //从本地读取了微博失败
                            Log.d(TAG,"load weibo from local failed");
                            //开始联网刷新微博
                            refreshNewWeibo();
                        }else {
                            //读取成功
                            Log.d(TAG,"load weibo from local count = "+list.size());
                            mainPageView.notifyWeiboListChanged(list,true,true);
                        }
                    }
                });
    }

    /**
     * 把现有的微博保存到本地
     * @param list 当前微博列表
     * @param uid 用户ID
     */
    @Override
    public void saveWeibosToLocal(final List<WeiboItemData> list,final String uid){
        Observable.just(list)
                .map(new Function<List<WeiboItemData>, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull List<WeiboItemData> weiboItemDataList) throws Exception {
                        return FileTools.saveWeiboListToLocal(weiboItemDataList,uid);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (!aBoolean){
                            ViewTools.showToast(R.string.saveWeiboDataFailed);
                        }
                    }
                });

    }

    /**
     * 把用户信息保存到本地
     * @param userData 用户信息
     */
    @Override
    public void saveUserDataToLocal(UserData userData) {
        Observable.just(userData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<UserData, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull UserData userData) throws Exception {
                        return FileTools.saveUserDataToLocal(userData);
                    }
                })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                        if (aBoolean){
                            //保存成功
                        }else {
                            //保存失败
                            ViewTools.showToast(R.string.save_userdata_failed);
                        }
                    }
                });
    }

    /**
     * 从本地读取用户信息
     * @param uid
     */
    @Override
    public void readUserDataFromLocal(final String uid) {
        Observable.just(uid)
                .map(new Function<String, UserData>() {
                    @Override
                    public UserData apply(@NonNull String s) throws Exception {

                        return FileTools.readUserDataFromLocal(s);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UserData>() {
                    @Override
                    public void accept(@NonNull UserData userData) throws Exception {
                        if (!userData.isNull()){
                            //读取成功
                            mainPageView.showMainUserData(userData);
                        }else {
                            //本地读取失败，应该联网查询
                            refreshUserDataFromWeb(null,uid);
                        }
                    }
                });
    }

    /**
     * 从网络获取用户信息
     * @param userName 用户名
     * @param uid 用户ID
     */
    @Override
    public void refreshUserDataFromWeb(@Nullable String userName, @Nullable String uid) {
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
                            mainPageView.showMainUserData(userData);
                            //立刻保存到本地
                            saveUserDataToLocal(userData);
                        }else {
                            //读取失败
                            ViewTools.showToast(R.string.refresh_userdata_failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.printStackTrace();
                        ViewTools.showToast(R.string.web_error);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
