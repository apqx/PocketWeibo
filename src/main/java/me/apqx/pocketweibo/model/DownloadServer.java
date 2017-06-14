package me.apqx.pocketweibo.model;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by apqx on 2017/6/14.
 */

public interface DownloadServer {
    @GET
    @Streaming
    Observable<ResponseBody> downloadPicture(@Url String urlString);
}
