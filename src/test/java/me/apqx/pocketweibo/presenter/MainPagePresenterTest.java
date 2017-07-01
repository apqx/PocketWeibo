package me.apqx.pocketweibo.presenter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.apqx.pocketweibo.bean.Weibos;
import me.apqx.pocketweibo.model.WeiboServer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by apqx on 2017/6/14.
 */
public class MainPagePresenterTest {
    private static final String ACCESS_TOKEN="2.00LtsXaD0xIktVaca3c5f5f8nsu3CD";
    private WeiboServer weiboServer;
    private Gson gson;
    @Before
    public void setUp() throws Exception {
        gson=new GsonBuilder().create();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://api.weibo.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        weiboServer=retrofit.create(WeiboServer.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void refreshNewWeibo() throws Exception {
        weiboServer.getWeibos(ACCESS_TOKEN,null)
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<Weibos>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                        System.out.println("onSubscribe");
                    }

                    @Override
                    public void onNext(@NonNull Weibos weibos) {
                        System.out.println("onNext "+weibos.getList().size());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println("onError");

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("onComplete");

                    }
                });

    }

    @Test
    public void refreshOldWeibo() throws Exception {

    }

}