package me.apqx.pocketweibo.model;

import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;

/**
 * Created by apqx on 2017/5/3.
 * 工具类
 */

public class FileTools {
    private static final String TAG="FileTools";
    public static void init(){}
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

    public static boolean saveFileToLocal(String content,String fileName){
        boolean success=true;
        File file=new File(MyApplication.getContext().getExternalFilesDir(null),fileName);
        PrintStream printStream=null;
        try {
            printStream=new PrintStream(new FileOutputStream(file));
            printStream.println(content);
        }catch (IOException e){
            e.printStackTrace();
            Log.d(TAG,"save temp file failed");
            success=false;
        }finally {
            closeStream(printStream);
        }
        return success;
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

    public static boolean saveWeiboListToLocal(List<WeiboItemData> list,String uid){
        Gson gson=new Gson();
        String jsonArray=gson.toJson(list);
        boolean success=saveFileToLocal(jsonArray,"WeiboListCache"+"_uid"+".json");
        Log.d(TAG,"saveWeiboListToLocal num = "+list.size());
        return success;
    }

    public static List<WeiboItemData> readWeiboListFromLocal(String uid){
        String string=readStringFromLocal("WeiboListCache"+"_uid"+".json");
        Gson gson=new Gson();
        if (string==null){
            return new ArrayList<WeiboItemData>();
        }
        List<WeiboItemData> list=gson.fromJson(string,new TypeToken<List<WeiboItemData>>(){}.getType());
        return list;
    }
    //获取本地保存的最新的微博id
    public static String getLastWeiboId(String uid){
        return readWeiboListFromLocal(uid).get(0).getWeiboId();
    }
    public static boolean saveUserDataToLocal(UserData userData){
        Boolean success;
        success=saveFileToLocal(userData.toString(),userData.getUserId()+".json");
        Log.d(TAG,"save userdata to local username is "+userData.getUserName()+" id is "+userData.getUserId());
        return success;
    }
    public static UserData readUserDataFromLocal(String userId){
        String string=readStringFromLocal(userId+".json");
        if (string==null){
            Log.d(TAG,"read userdata from local failed");
            return new UserData(true);
        }
        Gson gson=new Gson();
        UserData userData=gson.fromJson(string,UserData.class);
        Log.d(TAG,"read userdata from local succeed");
        return userData;
    }
    public static boolean saveFileToLocalFromStream(File file,InputStream inputStream){
        boolean isSuccess=true;
        OutputStream outputStream=null;
        try {
            outputStream=new FileOutputStream(file);
            byte[] temp=new byte[1024];
            int length;
            while ((length=inputStream.read(temp))!=-1){
                outputStream.write(temp,0,length);
            }
        }catch (IOException e){
            e.printStackTrace();
            isSuccess=false;
        }finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }
        return isSuccess;
    }



}
