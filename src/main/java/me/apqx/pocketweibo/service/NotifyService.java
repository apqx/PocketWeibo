package me.apqx.pocketweibo.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;

import me.apqx.pocketweibo.AppThreadPool;
import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.MainPageActivity;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.tools.Settings;
import me.apqx.pocketweibo.tools.Tools;
import me.apqx.pocketweibo.tools.WebTools;

/**
 * Created by apqx on 2017/6/8.
 */

public class NotifyService extends Service {
    private static final String TAG="NotifyService";
    public static final String ACTION_FROM_SERVICE="ACTION_FROM_SERVICE";
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Settings settings=new Settings(this);
        boolean serviceOn=settings.getServiceOn();

        Log.d(TAG,"serviceOn = "+serviceOn);
        if (!serviceOn){
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,1,new Intent("ACTION_NOTIFY_WEIBO"),PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+1000*60*30,pendingIntent);
        ExecutorService exec=AppThreadPool.getThreadPool();
        String action=intent.getAction();
        if (action!=null&&action.equals("NOT_NOW")){
            //说明是从MainPage启动的Activity，这时应等待下一次启动再执行任务。
            stopSelf();
            return super.onStartCommand(intent, flags, startId);

        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"service");

                if (Constant.accessToken==null){
                    return;
                }
                String urlString="https://api.weibo.com/2/statuses/home_timeline.json?access_token="+Constant.accessToken.getToken();
                String weibos= WebTools.getWebString(urlString);
                if (weibos==null){
                    //从网络中读取错误

                }else {
                    try{
                        JSONObject jsonObject=new JSONObject(weibos);
                        JSONArray jsonArray=jsonObject.getJSONArray("statuses");
                        if (jsonArray!=null&&jsonArray.length()>0){
                            long newId=Long.parseLong(ParseJsonTools.getWeiboFromJson(jsonArray.getJSONObject(0)).getWeiboId());
                            long oldId=Long.parseLong(Tools.getLastWeiboId());
                            Log.d(TAG,newId+" "+oldId);
                            if(newId>oldId){
                                //说明有新的微博,应该弹出Notification
                                Notification.Builder builder=new Notification.Builder(NotifyService.this);
                                Intent intent=new Intent(NotifyService.this,MainPageActivity.class);
                                intent.setAction(ACTION_FROM_SERVICE);
                                PendingIntent pendingIntent=PendingIntent.getActivity(NotifyService.this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentText("有新的微博")
                                        .setContentTitle("口袋微博")
                                        .setAutoCancel(true)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentIntent(pendingIntent)
                                        .setDefaults(Notification.DEFAULT_ALL);
                                Notification notification=builder.build();
                                NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(1,notification);

                            }
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        });


//        Notification.Builder builder=new Notification.Builder(NotifyService.this);
//        Intent newIntent=new Intent(NotifyService.this,MainPageActivity.class);
//        newIntent.setAction(ACTION_FROM_SERVICE);
//        PendingIntent newPendingIntent=PendingIntent.getActivity(NotifyService.this,1,newIntent,PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentText("有新的微博")
//                .setContentTitle("口袋微博")
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(newPendingIntent)
//                .setDefaults(Notification.DEFAULT_ALL);
//        Notification notification=builder.build();
//        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.notify(1,notification);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"stop");

    }
}
