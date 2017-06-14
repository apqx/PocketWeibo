package me.apqx.pocketweibo.presenter;

import android.os.Environment;

import java.io.File;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BooleanSupplier;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.apqx.pocketweibo.MyApplication;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.model.DownloadServer;
import me.apqx.pocketweibo.model.Tools;
import me.apqx.pocketweibo.model.ViewTools;
import me.apqx.pocketweibo.model.WebTools;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by apqx on 2017/6/14.
 */

public class DownloadPresenter implements IDownloadPresenter{
    private DownloadServer downloadServer;
    public DownloadPresenter(){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("http://apqx.me")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        downloadServer=retrofit.create(DownloadServer.class);
    }
    @Override
    public void downloadPicture(final String urlString) {
        downloadServer.downloadPicture(urlString)
                .map(new Function<ResponseBody, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull ResponseBody responseBody) throws Exception {
                        ViewTools.showToast(R.string.start_download_picture);
                        InputStream inputStream=responseBody.byteStream();
                        File dir= new File(Environment.getExternalStorageDirectory(),"PocketWeibo");
                        if (!dir.exists()){
                            dir.mkdir();
                        }
                        File file=new File(dir, WebTools.hashKeyFromUrl(urlString)+urlString.substring(urlString.length()-4));
                        return Tools.saveFileToLocalFromStream(file,inputStream);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean isSuccess) throws Exception {
                        if (isSuccess){
                            //下载成功
                            ViewTools.showToast(R.string.save_file_success);
                        }else {
                            //下载失败
                            ViewTools.showToast(R.string.save_file_failed);
                        }
                    }
                });
    }
}
