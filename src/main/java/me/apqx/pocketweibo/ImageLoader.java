package me.apqx.pocketweibo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.apqx.pocketweibo.tools.Tools;
import me.apqx.pocketweibo.tools.WebTools;

/**
 * Created by apqx on 2017/5/4.
 * 对于图片缓存，采用内存和磁盘两种缓存方式
 * 对于字符串，应该解析出所有的微博，维护一个按顺序包含100条微博的List，微博主体可以用JSONObject表示，在加载和退出时更新这个List，保存到本地。
 */

public class ImageLoader {
    private DiskLruCache diskLruCache;
    private LruCache<String,Bitmap> bitmapLruCache;
    private ExecutorService exec;
    private static ImageLoader imageLoader;
    private ImageLoader(Context context){
        //kb
        int maxMemory=(int)Runtime.getRuntime().maxMemory()/1024;
        int cacheSize=maxMemory/8;
        bitmapLruCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getHeight()*value.getRowBytes()/1024;
            }
        };
        File cacheDir=context.getExternalCacheDir();
        //byte
        int diskCacheSize=50*1024*1024;
        if (cacheDir.getFreeSpace()>=diskCacheSize){
            try {
                diskLruCache=DiskLruCache.open(cacheDir,1,1,diskCacheSize);
            }catch (IOException e){
                e.printStackTrace();
                Log.d("apqx","DiskLruCache open failed");
            }
        }
        exec= Executors.newCachedThreadPool();
    }
    public static ImageLoader initiate(Context context){
        if (imageLoader!=null){
            return imageLoader;
        }
        return new ImageLoader(context);
    }
    private void addBitmapToLruCache(String key,Bitmap bitmap){
        bitmapLruCache.put(key,bitmap);
    }
    private Bitmap getBitmapFromLruCache(String key){
        return bitmapLruCache.get(key);
    }


    /**
     * 从网络中下载图片，然后立即存储到磁盘缓存中
     * @param stringUrl 图片所在的url
     * @return bitmap or null
     */
    private Bitmap loadBitmapFromHttp(String stringUrl){
        if (Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("Don't visit web from main thread!");
        }
        Bitmap bitmap=WebTools.getWebBitmap(stringUrl);
        if (diskLruCache==null){
            return bitmap;
        }
        String key=getKeyFromURL(stringUrl);
        if (bitmap!=null){
            DiskLruCache.Editor editor=null;
            OutputStream outputStream=null;
            try {
                editor=diskLruCache.edit(key);
                outputStream=editor.newOutputStream(1);
                if (storeHttpToStream(stringUrl,outputStream)){
                    editor.commit();
                }else {
                    editor.abort();
                }
                diskLruCache.flush();
            }catch (IOException e){
                e.printStackTrace();
                Log.d("apqx","Save string to disk cache error");
            }finally {
                Tools.closeStream(outputStream);
            }
        }
        return bitmap;
    }


    /**
     * 从磁盘缓存中读取图片，当不存在是返回null
     * @param stringUrl the specified url
     * @return bitmap or null
     */
    private Bitmap loadBitmapFromDiskCache(String stringUrl){
        if (Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("Don't visit disk from main thread!");
        }
        if (diskLruCache==null){
            return null;
        }
        String key=getKeyFromURL(stringUrl);
        DiskLruCache.Editor editor=null;
        Bitmap bitmap=null;
        try {
            editor=diskLruCache.edit(key);
            InputStream inputStream=editor.newInputStream(1);
            if (inputStream!=null){
                bitmap=BitmapFactory.decodeStream(inputStream);
            }

        }catch (IOException e){
            e.printStackTrace();
            Log.d("apqx","load string from disk cache error");
        }
        return bitmap;
    }
    public void bindImageViewWithBitmap(View view, String stringUrl){

    }



    private String getKeyFromURL(String url){
        String key="";
        try {
            MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            byte[] bytes=messageDigest.digest();
            StringBuilder stringBuilder=new StringBuilder();
            for (byte b:bytes){
                String hex=Integer.toHexString(0xff&b);
                if (hex.length()==1){
                    stringBuilder.append(hex);
                }
                stringBuilder.append(hex);
            }
            key=stringBuilder.toString();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return key;
    }
    private boolean storeHttpToStream(String stringUrl,OutputStream outputStream){
        URL url=null;
        HttpURLConnection httpURLConnection=null;
        InputStream inputStream=null;
        try {
            url=new URL(stringUrl);
            httpURLConnection=(HttpURLConnection)url.openConnection();
            inputStream=httpURLConnection.getInputStream();
            byte[] temp=new byte[1024];
            while (inputStream.read(temp)!=-1){
                outputStream.write(temp);
            }
            return true;
        }catch (MalformedURLException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
            Tools.closeStream(inputStream);
        }
        return false;
    }
}
