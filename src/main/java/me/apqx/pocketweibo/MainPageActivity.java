package me.apqx.pocketweibo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.struct.UserData;
import me.apqx.pocketweibo.struct.WeiboItemData;
import me.apqx.pocketweibo.tools.Tools;
import me.apqx.pocketweibo.tools.WebTools;

/**
 * Created by apqx on 2017/4/19.
 * 主页面
 */

public class MainPageActivity extends AppCompatActivity {
    private static final String TAG="MainPageActivity";
    private static final int FILL_FROM_WEB_UP =0;
    private static final int FILL_FROM_WEB_DOWN =1;
    private static final int FILL_FROM_LOCAL=2;
    private static final int READ_WEB_ERROR=3;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    private ExecutorService exec;
    private Handler handler;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    //本地的微博列表是源JSONObject转换的WeiboItemData对象
    private static List<WeiboItemData> list;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.nav_open,R.string.nav_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        fab=(FloatingActionButton)findViewById(R.id.fab);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout_main_page);
        swipeRefreshLayout.setOnRefreshListener(new MyOnRefreshListener());

        recyclerView=(RecyclerView)findViewById(R.id.recyclerView_mainPage);
        list=new ArrayList<WeiboItemData>();
        adapter=new WeiboItemRecyclerAdapter(list,R.layout.layout_weibo_recycler_item,WeiboItemRecyclerAdapter.WEIBO_MAINPAGE_LIST);
        RecyclerView.LayoutManager layoutManager=null;
        if (isPad()||getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            layoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        }else {
            layoutManager=new LinearLayoutManager(this);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        exec=MyThreadPool.getThreadPool();
        handler=new DataHandler();
        //启动时，首先从本地读取保存好的微博，如果本地不存在缓存文件，就从网络中读取
        List<WeiboItemData> tempList=Tools.readWeiboListFromLocal();
        if (tempList!=null){
            list.clear();
            list.addAll(tempList);
            adapter.notifyDataSetChanged();
        }else {
            swipeRefreshLayout.setRefreshing(true);
            exec.execute(new TaskLoadNewWeibo());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(toggle);
    }
    //判断是不是平板电脑
    private boolean isPad() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    private class DataHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case READ_WEB_ERROR:
                    Toast.makeText(MainPageActivity.this, R.string.web_error,Toast.LENGTH_SHORT).show();
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case FILL_FROM_WEB_UP:
                    //向上刷新
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainPageActivity.this, getString(R.string.refresh_weibo)+" "+msg.arg2,Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                    //每一次刷新微博，应该立即将微博列表保存到本地
                    exec.execute(new TaskSaveWeiboToLocal());
                    break;
                case FILL_FROM_WEB_DOWN:
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    }
    //对外提供方法，可以当前列表中查询指定ID的微博
    public static WeiboItemData getWeiboItem(String weiboId){
        Log.d(TAG,"Search weiboId from list : "+weiboId);
        for (WeiboItemData weiboItemData:list){
            if (weiboItemData.getWeiboId().equals(weiboId)){
                Log.d(TAG,"Search weiboId from list : "+weiboId+" found");
                return weiboItemData;
            }
            if (weiboItemData.hasReTwitter()){
                WeiboItemData reTwitter=weiboItemData.getReTwitterWeibo();
                if (reTwitter.getWeiboId().equals(weiboId)){
                    Log.d(TAG,"Search weiboId from list : "+weiboId+" found reTwitter");
                    return reTwitter;
                }
            }
        }
        Log.d(TAG,"Search weiboId from list : "+weiboId+" not found");
        return null;
    }
    //对外提供方法，可以可以当前列表中查询指定用户名的用户
    public static UserData getUserData(String userName){
        Log.d(TAG,"Search user from list : "+userName);
        for (WeiboItemData weiboItemData:list){
            UserData userData=weiboItemData.getWeiboUserData();
            if (userData.getUserName().equals(userName)){
                return userData;
            }
            if (weiboItemData.hasReTwitter()){
                userData=weiboItemData.getReTwitterWeibo().getWeiboUserData();
                if (userData.getUserName().equals(userName)){
                    return userData;
                }
            }
        }
        Log.d(TAG,"Search user from list : "+userName+" not found");
        return null;
    }



    private class TaskLoadNewWeibo implements Runnable{

        @Override
        public void run() {
            String urlString="https://api.weibo.com/2/statuses/home_timeline.json?access_token="+Constant.accessToken.getToken();
            String weibos= WebTools.getWebString(urlString);
            if (weibos==null){
                //从网络中读取错误
                Message message=new Message();
                message.arg1=READ_WEB_ERROR;
                handler.sendMessage(message);
            }else {
                try{
                    JSONObject jsonObject=new JSONObject(weibos);
                    JSONArray jsonArray=jsonObject.getJSONArray("statuses");
                    if (jsonArray!=null&&jsonArray.length()>0){
                        //向上刷新，应该清空列表，重新加载
                        list.clear();
                        for (int i=0;i<jsonArray.length();i++){
                            list.add(ParseJsonTools.getWeiboFromJson(jsonArray.getJSONObject(i)));
                        }
                    }
                    Message message=new Message();
                    message.arg1=FILL_FROM_WEB_UP;
                    message.arg2=jsonArray.length();
                    handler.sendMessage(message);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class TaskLoadOldWeibo implements Runnable{
        private String sinceId;
        public TaskLoadOldWeibo(String sinceId){
            this.sinceId=sinceId;
        }
        @Override
        public void run() {
            //获取比给定微博ID早的微博
            String urlString="https://api.weibo.com/2/statuses/home_timeline.json?access_token="+Constant.accessToken.getToken();
            String weibos= WebTools.getWebString(urlString);
            if (weibos==null){
                //从网络中读取错误
                Message message=new Message();
                message.arg1=READ_WEB_ERROR;
                handler.sendMessage(message);
            }else {
                try{
                    JSONObject jsonObject=new JSONObject(weibos);
                    JSONArray jsonArray=jsonObject.getJSONArray("statuses");
                    if (jsonArray!=null&&jsonArray.length()>0){
                        //向下刷新，应该应该将获得的新的微博添加列表的后方
                        for (int i=0;i<jsonArray.length();i++){
                            list.add(ParseJsonTools.getWeiboFromJson(jsonArray.getJSONObject(i)));
                        }
                    }
                    Message message=new Message();
                    message.arg1=FILL_FROM_WEB_DOWN;
                    handler.sendMessage(message);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private class MyOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh() {
            exec.execute(new TaskLoadNewWeibo());
        }
    }
    private class TaskSaveWeiboToLocal implements Runnable{
        @Override
        public void run() {
            Tools.saveWeiboListToLocal(list);
        }
    }

}
