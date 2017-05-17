package me.apqx.pocketweibo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by apqx on 2017/5/14.
 */

public class MyThreadPool {
    private static ExecutorService exec;
    private MyThreadPool(){}
    public static ExecutorService getThreadPool(){
        if (exec==null){
            exec=Executors.newCachedThreadPool();
        }
        return exec;
    }
}
