package me.apqx.pocketweibo.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import me.apqx.pocketweibo.MyApplication;
import me.apqx.pocketweibo.R;

/**
 * Created by apqx on 2017/5/4.
 */

public class WebTools {
    private static final String TAG="WebTools";
    /**
     * 从网络上下载字符串，当网络出错时返回null
     * @param stringUrl the specified url
     * @return string or null
     */
    public static String getWebString(String stringUrl){
        URL url=null;
        HttpURLConnection httpURLConnection=null;
        BufferedReader bufferedReader=null;
        StringBuilder stringBuilder=new StringBuilder();
        try {
            url=new URL(stringUrl);
            httpURLConnection=(HttpURLConnection)url.openConnection();
            bufferedReader=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String string;
            while ((string=bufferedReader.readLine())!=null){
                stringBuilder.append(string);
            }
            Log.d(TAG,"GET web String url = "+stringUrl);
        }catch (MalformedURLException e){
            e.printStackTrace();
            Log.d(TAG,"URL Exception");
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"HttpURLConnection failed");
        }finally {
            Tools.closeStream(bufferedReader);
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        if (stringBuilder.length()==0){
            return null;
        }
        return stringBuilder.toString();
    }

    /**
     * 从网络上下载图片，当网络连接出错时有可能为null
     * @param stringUrl the specified url
     * @return bitmap or null
     */
    public static Bitmap getWebBitmap(String stringUrl){
        URL url=null;
        HttpURLConnection httpURLConnection=null;
        InputStream inputStream=null;
        Bitmap bitmap=null;
        try {
            url=new URL(stringUrl);
            httpURLConnection=(HttpURLConnection)url.openConnection();
            inputStream=httpURLConnection.getInputStream();
            bitmap=BitmapFactory.decodeStream(inputStream);
        }catch (MalformedURLException e){
            e.printStackTrace();
            Log.d(TAG,"URL Exception");
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"HttpURLConnection failed");
        }finally {
            Tools.closeStream(inputStream);
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return bitmap;
    }
}
