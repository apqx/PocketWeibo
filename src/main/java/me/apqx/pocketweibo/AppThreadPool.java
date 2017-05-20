package me.apqx.pocketweibo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by apqx on 2017/5/14.
 */

public class AppThreadPool {
    private static ExecutorService exec;
    private AppThreadPool(){}
    public static ExecutorService getThreadPool(){
        if (exec==null){
            exec=Executors.newCachedThreadPool();
        }
        return exec;
    }

}
