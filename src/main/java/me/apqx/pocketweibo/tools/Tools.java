package me.apqx.pocketweibo.tools;

import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import me.apqx.pocketweibo.MyApplication;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.struct.WeiboItemData;

/**
 * Created by apqx on 2017/5/3.
 * 工具类
 */

public class Tools {
    private static final String TAG="Tools";
    public static void closeStream(Object object){
        InputStream inputStream=null;
        OutputStream outputStream=null;
        Reader reader=null;
        Writer writer=null;
        if (object==null){
            return;
        }
        if (object instanceof InputStream){
            inputStream=(InputStream)object;
        }else if (object instanceof OutputStream){
            outputStream=(OutputStream)object;
        }else if (object instanceof Reader){
            reader=(Reader)object;
        }else if (object instanceof Writer){
            writer=(Writer)object;
        }
        try {
            if (inputStream!=null){
                inputStream.close();
//                Log.d(TAG,"InputStream close");
            }
            if (outputStream!=null){
                outputStream.close();
//                Log.d(TAG,"OutputStream close");
            }
            if (reader!=null){
                reader.close();
//                Log.d(TAG,"Reader close");
            }
            if (writer!=null){
                writer.close();
//                Log.d(TAG,"Writer close");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void saveFileToLocal(String content,String fileName){
        File file=new File(MyApplication.getContext().getExternalFilesDir(null),fileName);
        PrintStream printStream=null;
        try {
            printStream=new PrintStream(new FileOutputStream(file));
            printStream.println(content);
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"save temp file failed");
        }finally {
            closeStream(printStream);
        }
    }
    public static String readStringFromLocal(String fileName){
        File file=new File(MyApplication.getContext().getExternalFilesDir(null),fileName);
        if (!file.exists()){
            return null;
        }
        BufferedReader bufferedReader=null;
        StringBuilder stringBuilder=new StringBuilder();
        try {
            bufferedReader=new BufferedReader(new FileReader(file));
            String temp;
            while ((temp=bufferedReader.readLine())!=null){
                stringBuilder.append(temp);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            closeStream(bufferedReader);
        }
        return stringBuilder.toString();
    }

    public static String parseTime(String time){
        return time.substring(0,7);
    }
    public static String parseDevice(String device){
        if (TextUtils.isEmpty(device)){
            device="<a href=\"http://weibo.com\" rel=\"nofollow\">微博 weibo.com</a>";
        }
        int startIndex=device.indexOf(">")+1;
        if (startIndex==0){
            return device;
        }
        int endIndex=device.indexOf("<",startIndex);
        device=device.substring(startIndex,endIndex);
        return device;
    }

    public static void saveWeiboListToLocal(List<WeiboItemData> list){
        JSONArray jsonArray=new JSONArray();
        try {
            for (int i=0;i<list.size();i++){
                JSONObject jsonObject=new JSONObject(list.get(i).toString());
                jsonArray.put(jsonObject);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        saveFileToLocal(jsonArray.toString(),"WeiboListCache.json");
        Log.d(TAG,"saveWeiboListToLocal num = "+list.size());
    }

    public static List<WeiboItemData> readWeiboListFromLocal(){
        List<WeiboItemData> list=new ArrayList<WeiboItemData>();
        String string=readStringFromLocal("WeiboListCache.json");
        if (string==null){
            Log.d(TAG,"readWeiboListFromLocal num = null");
            return null;
        }
        try{
            JSONArray jsonArray=new JSONArray(string);
            if (jsonArray!=null&&jsonArray.length()>0){
                for (int i=0;i<jsonArray.length();i++){
                    list.add(ParseJsonTools.getWeiboFromJson(jsonArray.getJSONObject(i)));
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.d(TAG,"readWeiboListFromLocal num = "+list.size());
        return list;
    }


}
