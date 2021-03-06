package me.apqx.pocketweibo.presenter;

import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.util.ListAddBiConsumer;
import io.reactivex.schedulers.Schedulers;
import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.bean.CommentData;
import me.apqx.pocketweibo.bean.Comments;
import me.apqx.pocketweibo.model.ViewTools;
import me.apqx.pocketweibo.model.WeiboServer;
import me.apqx.pocketweibo.view.IWeiboDetailView;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by apqx on 2017/6/14.
 */

public class WeiboDetailPresenter implements IWeiboDetailPresenter {
    private static final String TAG="WeiboDetailPresenter";
    private IWeiboDetailView weiboDetailView;
    private WeiboServer weiboServer;
    public WeiboDetailPresenter(IWeiboDetailView weiboDetailView){
        this.weiboDetailView=weiboDetailView;
        Gson gson=new Gson();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://api.weibo.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        weiboServer=retrofit.create(WeiboServer.class);
    }
    @Override
    public void refreshNewComment(String weiboId) {
        weiboServer.getCommentData(Constant.accessToken.getToken(),weiboId,null)
                .map(new Function<Comments, List<CommentData>>() {
                    @Override
                    public List<CommentData> apply(@NonNull Comments comments) throws Exception {
                        if (comments!=null){
                            return comments.getComments();
                        }
                        return new ArrayList<CommentData>();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CommentData>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<CommentData> list) {
                        if (list.size()>0){
                            //加载评论成功
                            Log.d(TAG,"refresh newComment onNext count = "+list.size());
                            weiboDetailView.notifyCommentListChanged(list,true);
                        }else {
                            //加载评论失败
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //网络错误，加载评论失败
                        Log.d(TAG,"refresh newComment onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void refreshOldComment(String weiboId,String maxId) {
        weiboServer.getCommentData(Constant.accessToken.getToken(),weiboId,maxId)
                .map(new Function<Comments, List<CommentData>>() {
                    @Override
                    public List<CommentData> apply(@NonNull Comments comments) throws Exception {
                        if (comments!=null){
                            return comments.getComments();
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CommentData>>() {
                    @Override
                    public void accept(@NonNull List<CommentData> list) throws Exception {
                        if (list!=null){
                            //加载评论成功
                            weiboDetailView.notifyCommentListChanged(list,false);
                        }else {
                            //加载评论失败
                        }
                    }
                });
    }

    @Override
    public void newComment(String weiboId, String comment) {
        weiboServer.newComment(Constant.accessToken.getToken(),comment,weiboId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        ViewTools.showToast(R.string.commentSuccess);
                        //应该把立即把评论加载到视图中，并更新本地保存的数据
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ViewTools.showToast(R.string.comment_failed);
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
