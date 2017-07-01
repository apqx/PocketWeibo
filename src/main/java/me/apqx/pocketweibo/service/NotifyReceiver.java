package me.apqx.pocketweibo.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by apqx on 2017/6/8.
 * 用于启动后台服务的广播接收器
 */

public class NotifyReceiver extends BroadcastReceiver {
    public static final String ACTION_NOTIFY_WEIBO="ACTION_NOTIFY_WEIBO";
    public static final String ACTION_SHOUND_NOT_NOTIFY_WEIBO="ACTION_SHOUND_NOT_NOTIFY_WEIBO";
    public static final String ACTION_SHOUND_NOTIFY_WEIBO="ACTION_SHOUND_NOTIFY_WEIBO";

    private static boolean shouldNotifyWeibo=true;
    @Override
    public void onReceive(Context context, Intent intent) {
        String flag=intent.getAction();
        switch (flag){
            case ACTION_NOTIFY_WEIBO:
                if (shouldNotifyWeibo){
                    Intent newIntent=new Intent(context,NotifyService.class);
                    context.startService(newIntent);
                }
                break;
            case ACTION_SHOUND_NOT_NOTIFY_WEIBO:
                shouldNotifyWeibo=false;
                break;
            case ACTION_SHOUND_NOTIFY_WEIBO:
                shouldNotifyWeibo=true;
                Intent newIntent=new Intent(context,NotifyService.class);
                newIntent.setAction("NOT_NOW");
                context.startService(newIntent);
                break;
        }

    }
}
