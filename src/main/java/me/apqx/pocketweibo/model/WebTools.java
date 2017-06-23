package me.apqx.pocketweibo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.AppThreadPool;
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
            Log.d(TAG,"GET web String url = "+stringUrl);
            url=new URL(stringUrl);
            httpURLConnection=(HttpURLConnection)url.openConnection();
            bufferedReader=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String string;
            while ((string=bufferedReader.readLine())!=null){
                stringBuilder.append(string);
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
            Log.d(TAG,"URL Exception");
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"HttpURLConnection failed");
        }finally {
            FileTools.closeStream(bufferedReader);
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        if (stringBuilder.length()==0){
            return null;
        }
        //将读取到的数据临时保存到本地，方便测试
        FileTools.saveFileToLocal(stringBuilder.toString(),"temp.json");
        return stringBuilder.toString();
    }

    public static String postWebString(String urlString,String post){
        URL url;
        HttpURLConnection httpURLConnection=null;
        PrintStream printPost=null;
        BufferedReader bufferedReader=null;
        StringBuilder stringBuilder=new StringBuilder();
        try {
            Log.d(TAG,"POST web String url = "+urlString);
            Log.d(TAG,"POST  = "+post);
            url=new URL(urlString);
            httpURLConnection=(HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");


            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            printPost=new PrintStream(httpURLConnection.getOutputStream());
            printPost.print(post);
            bufferedReader=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String temp;
            while ((temp=bufferedReader.readLine())!=null){
                stringBuilder.append(temp);
            }
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"HttpURLConnection failed");
        }finally {
            FileTools.closeStream(bufferedReader);
            FileTools.closeStream(printPost);
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
            FileTools.closeStream(inputStream);
            if (httpURLConnection!=null){
                httpURLConnection.disconnect();
            }
        }
        return bitmap;
    }

    public static void downLoadPicToDisk(Handler handler,String urlString){
        URL url=null;
        BufferedInputStream inputStream=null;
        FileOutputStream outputStream=null;
        handler.post(new Runnable() {
            @Override
            public void run() {
                ViewTools.showToast(R.string.start_download_picture);
            }
        });
        try {
            url=new URL(urlString);
            inputStream=new BufferedInputStream(url.openStream());
            File dir= new File(Environment.getExternalStorageDirectory(),"PocketWeibo");
            if (!dir.exists()){
                dir.mkdir();
            }
            File file=new File(dir,hashKeyFromUrl(urlString)+urlString.substring(urlString.length()-4));

            outputStream=new FileOutputStream(file);
            byte[] temp=new byte[1024];
            int length;
            while ((length=inputStream.read(temp))!=-1){
                outputStream.write(temp,0,length);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ViewTools.showToast(R.string.save_file_success);
                }
            });
        }catch (IOException e){
            e.printStackTrace();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ViewTools.showToast(R.string.save_file_failed);
                }
            });
        }finally {
            FileTools.closeStream(inputStream);
            FileTools.closeStream(outputStream);
        }
    }

    public static String hashKeyFromUrl(String url){
        String key="";
        try{
            //MessageDigest可以根据传入字节数组的MD5值，将其转换为对应的哈希字节数组
            MessageDigest digest=MessageDigest.getInstance("MD5");
            //传入字节数组
            digest.update(url.getBytes());
            //生成哈希字节数组，并转换为十六进制数字
            key=bytesToHexString(digest.digest());
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return key;
    }
    private static String bytesToHexString(byte[] bytes){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<bytes.length;i++){
            String hex=Integer.toHexString(0xff&bytes[i]);
            if(hex.length()==1){
                //保证每个字节都转换为2位十六进制数字
                sb.append(hex);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public static void startDownLoadPics(Handler handler){
        //查看列表中是否有要下载的图片连接
        for (String urlString: Constant.urlList){
            AppThreadPool.getThreadPool().execute(new TaskDownloadPic(urlString,handler));
            Constant.urlList.remove(urlString);
        }
    }
    static class TaskDownloadPic implements Runnable{
        private String urlString;
        private Handler handler;
        TaskDownloadPic(String urlString,Handler handler){
            this.urlString=urlString;
            this.handler=handler;
        }

        @Override
        public void run() {
            WebTools.downLoadPicToDisk(handler,urlString);
        }
    }

    //判断是否使用移动网络上网
    public static boolean isUsingLTE(){
        ConnectivityManager manager=(ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (networkInfo!=null){
            return networkInfo.isConnected();
        }
        return false;
    }
}
