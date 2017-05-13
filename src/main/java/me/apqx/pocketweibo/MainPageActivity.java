package me.apqx.pocketweibo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.struct.WeiboItemData;
import me.apqx.pocketweibo.tools.Tools;

/**
 * Created by apqx on 2017/4/19.
 * 主页面
 */

public class MainPageActivity extends AppCompatActivity {
    private static final String TAG="MainPageActivity";
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    private ExecutorService exec;
    private Handler handler;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
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

        recyclerView=(RecyclerView)findViewById(R.id.recyclerView_mainPage);
        list=new ArrayList<WeiboItemData>();
        adapter=new WeiboItemRecyclerAdapter(list,R.layout.layout_weibo_recycler_item,WeiboItemRecyclerAdapter.WEIBO_MAINPAGE_LIST);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        exec=Executors.newCachedThreadPool();
        handler=new DataHandler();
        exec.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection=null;
                URL url=null;
                BufferedReader bufferedReader=null;
                String string="https://api.weibo.com/2/statuses/home_timeline.json?access_token="+Constant.accessToken.getToken();
                Log.d(TAG,string);
                try{
                    url=new URL(string);
                    urlConnection=(HttpURLConnection)url.openConnection();
                    bufferedReader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String result=null;
                    StringBuilder stringBuilder=new StringBuilder();
                    while ((result=bufferedReader.readLine())!=null){
                        stringBuilder.append(result);
                    }
                    JSONObject jsonObject=new JSONObject(stringBuilder.toString());
                    JSONArray jsonArray=jsonObject.getJSONArray("statuses");
                    if (jsonArray!=null&&jsonArray.length()>0){
                        for (int i=0;i<jsonArray.length();i++){
                            list.add(ParseJsonTools.getWeiboFromJson(jsonArray.getJSONObject(i)));
                        }

                    }
                    Message message=new Message();
                    handler.sendMessage(message);

                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }catch (JSONException e){
                    e.printStackTrace();
                }
                finally {
                    Tools.closeStream(bufferedReader);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(toggle);
    }
    class DataHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
        }
    }
    //从外界获取单个微博
    public static WeiboItemData getWeiboItem(String weiboId){
        for (WeiboItemData weiboItemData:list){
            if (weiboItemData.getWeiboId().equals(weiboId)){
                return weiboItemData;
            }
            if (weiboItemData.hasReTwitter()){
                WeiboItemData reTwitter=weiboItemData.getReTwitterWeibo();
                if (reTwitter.getWeiboId().equals(weiboId)){
                    return reTwitter;
                }
            }
        }
        return null;
    }
}
