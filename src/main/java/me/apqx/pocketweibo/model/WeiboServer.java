package me.apqx.pocketweibo.model;

import io.reactivex.Observable;
import me.apqx.pocketweibo.bean.CommentData;
import me.apqx.pocketweibo.bean.Comments;
import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;
import me.apqx.pocketweibo.bean.Weibos;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by apqx on 2017/6/14.
 */

public interface WeiboServer {
    @GET ("https://api.weibo.com/2/statuses/home_timeline.json")
    Observable<Weibos> getWeibos(@Query("access_token") String accessToken,@Query("max_id") String manId);
    @GET ("https://api.weibo.com/2/users/show.json")
    Observable<UserData> getUserData(@Query("access_token") String accessToken,@Query("screen_name") String userName,@Query("uid") String uid);
    @GET ("https://api.weibo.com/2/comments/show.json")
    Observable<Comments> getCommentData(@Query("access_token") String accessToken, @Query("id") String weiboId, @Query("max_id") String maxId);
    @POST ("https://api.weibo.com/2/comments/create.json")
    @FormUrlEncoded
    Observable<ResponseBody> newComment(@Field("access_token") String accessToken,@Field("comment") String comment,@Field("id") String weiboId);
}
