package me.apqx.pocketweibo.presenter;

import com.google.gson.Gson;

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
    private IWeiboDetailView weiboDetailView;
    private WeiboServer weiboServer;
    public WeiboDetailPresenter(IWeiboDetailView weiboDetailView){
        this.weiboDetailView=weiboDetailView;
        Gson gson=new Gson();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://api.weibo.com")
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
                            weiboDetailView.notifyCommentListChanged(list,true);
                        }else {
                            //加载评论失败
                        }
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
        weiboServer.newComment(Constant.accessToken.getToken(),weiboId,comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResponseBody responseBody) {
                        ViewTools.showToast(R.string.commentSuccess);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        ViewTools.showToast(R.string.comment_failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
