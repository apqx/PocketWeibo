package me.apqx.pocketweibo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
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

public class MainPageActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG="MainPageActivity";
    private static final int FILL_WEIBO_FROM_WEB_UP =0;
    private static final int FILL_WEIBO_FROM_WEB_DOWN =1;
    private static final int FILL_WEIBO_FROM_LOCAL =2;
    private static final int READ_WEB_ERROR=3;
    private static final int READ_USERDATA_FROM_WEB=4;
    private static final int READ_USERDATA_FROM_LOCAL=5;
    private static final int READ_USERDATA_FROM_LOCAL_ERROR=6;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FloatingActionButton fab;
    private ExecutorService exec;
    private Handler handler;
    private RecyclerView recyclerView;
    private WeiboItemRecyclerAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavigationView navigationView;
    private SimpleDraweeView simpleDraweeView_head;
    private TextView textView_userName;
    private UserData userData;
    private TextView navTextViewProfile;
    private TextView navTextViewSettings;
    private TextView navTextViewNight;
    private Switch switchNight;
    private RecyclerView.LayoutManager layoutManager;
    private int resumeIndex;
    //本地的微博列表是源JSONObject转换的WeiboItemData对象
    private static List<WeiboItemData> list;
    private List<WeiboItemData> tempList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApplication.getMyTheme()==MyApplication.THEME_DARK){
            setTheme(R.style.AppTheme_Dark_Main);
        }else {
            setTheme(R.style.AppTheme_Light_Main);
        }
        setContentView(R.layout.layout_main);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.nav_open,R.string.nav_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
        navigationView=(NavigationView)findViewById(R.id.navigationView);

        fab=(FloatingActionButton)findViewById(R.id.fab);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout_main_page);
        swipeRefreshLayout.setOnRefreshListener(new MyOnRefreshListener());
        simpleDraweeView_head=(SimpleDraweeView)findViewById(R.id.simpleDraweeView_nav_head);
//        Log.d(TAG,simpleDraweeView_head.toString());
        textView_userName=(TextView)findViewById(R.id.textView_nav_username);
        navTextViewNight=(TextView)findViewById(R.id.nav_textView_night);
        navTextViewProfile=(TextView)findViewById(R.id.nav_textView_profile);
        navTextViewSettings=(TextView)findViewById(R.id.nav_textView_settings);
        switchNight=(Switch)findViewById(R.id.switch_night);
        if (MyApplication.getMyTheme()==MyApplication.THEME_DARK){
            switchNight.setChecked(true);
        }

        int resource;
        if (isPad()){
            resource=R.layout.layout_weibo_recycler_item_pad;
        }else {
            resource=R.layout.layout_weibo_recycler_item;
        }
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView_mainPage);
        list=new ArrayList<WeiboItemData>();

        adapter=new WeiboItemRecyclerAdapter(list,resource,WeiboItemRecyclerAdapter.WEIBO_MAINPAGE_LIST,this);
        if (isPad()||getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            layoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        }else {
            layoutManager=new LinearLayoutManager(this);
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        exec= AppThreadPool.getThreadPool();
        handler=new DataHandler();

        //只有在用户已登录的情况下才加载微博信息
        if (Constant.accessToken!=null){
            Intent intent=getIntent();
            //从登陆界面获得uid
            String uid=intent.getStringExtra("uid");
            //启动时，首先从本地读取保存好的微博，如果本地不存在缓存文件，就从网络中读取
            exec.execute(new TaskReadWeiboListFromLocal());
            //启动时，先从本地读取用户信息
            exec.execute(new TaskReadUserDataFromLocal(uid));
        }


        adapter.setOnRefreshDownListener(new WeiboItemRecyclerAdapter.OnRefreshDownListener() {
            @Override
            public void refreshDown() {
                if (list.size()>0){
                    exec.execute(new TaskLoadOldWeibo(list.get(list.size()-1).getWeiboId()));
                }
            }
        });



        setListener();
    }

    private void setListener(){
        simpleDraweeView_head.setOnClickListener(this);
        textView_userName.setOnClickListener(this);
        toolbar.setOnClickListener(this);
        navTextViewSettings.setOnClickListener(this);
        navTextViewProfile.setOnClickListener(this);
        navTextViewNight.setOnClickListener(this);
        switchNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    MyApplication.setMyTheme(MyApplication.THEME_DARK);
                }else {
                    MyApplication.setMyTheme(MyApplication.THEME_LIGHT);
                }
                recreate();
            }
        });
        fab.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawerLayout.removeDrawerListener(toggle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
            Log.d("apqx","onRestoreInstanceState");
        if (savedInstanceState!=null){
            resumeIndex=savedInstanceState.getInt("index");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        RecyclerView.LayoutManager layoutManager=recyclerView.getLayoutManager();
//            Log.d("apqx","onSaveInstanceState");
        if (layoutManager instanceof LinearLayoutManager){
            int index=((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();
            outState.putInt("index",index);
//            Log.d("apqx","LinearLayoutManager index = "+index);
        }else if (layoutManager instanceof StaggeredGridLayoutManager){
            int[] index=((StaggeredGridLayoutManager)layoutManager).findFirstVisibleItemPositions(null);
            outState.putInt("index",index[0]);
//            Log.d("apqx","StaggeredGridLayoutManager index = "+index[0]);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==0){
            if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                //说明申请权限成功
                WebTools.startDownLoadPics(handler);
            }else {
                Tools.showToast(getString(R.string.permission_denied));
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //判断是不是平板电脑
    private boolean isPad() {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbar:
                recyclerView.scrollToPosition(0);
                break;
            case R.id.textView_nav_username:
            case R.id.simpleDraweeView_nav_head:
            case R.id.nav_textView_profile:
                if (userData!=null){
                    Intent intent=new Intent(MainPageActivity.this,UserDataActivity.class);
                    intent.putExtra("apqx",userData.getUserName());
                    startActivity(intent);
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                break;
            case R.id.nav_textView_settings:
                startActivity(new Intent(this,SettingActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_textView_night:

                break;
            case R.id.fab:
                if (!swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(true);
                    exec.execute(new TaskLoadNewWeibo());
                }
                break;
        }
    }

    private class DataHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1){
                case READ_WEB_ERROR:
                    Tools.showToast(R.string.web_error);
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    break;
                case FILL_WEIBO_FROM_LOCAL:
                    if (tempList!=null){
                        list.clear();
                        list.addAll(tempList);
                        adapter.notifyDataSetChanged();
                        if (resumeIndex!=0){
                            layoutManager.scrollToPosition(resumeIndex);
                        }
                    }else {
                        swipeRefreshLayout.setRefreshing(true);
                        exec.execute(new TaskLoadNewWeibo());
                    }

                    break;
                case FILL_WEIBO_FROM_WEB_UP:
                    //向上刷新
                    if (swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    Tools.showToast(R.string.refresh_weibo);
                    adapter.notifyDataSetChanged();
                    //每一次刷新微博，应该立即将微博列表保存到本地
                    exec.execute(new TaskSaveWeiboToLocal());
                    break;
                case FILL_WEIBO_FROM_WEB_DOWN:
                    //向下刷新，读取更早前的微博
                    Tools.showToast(R.string.refresh_weibo);
                    adapter.notifyDataSetChanged();
                    exec.execute(new TaskSaveWeiboToLocal());
                    break;
                case READ_USERDATA_FROM_WEB:
                    simpleDraweeView_head.setImageURI(userData.getUserHeadPicURL());
                    textView_userName.setText(userData.getUserName());
                    exec.execute(new TaskSaveUserToLocal());
                    Log.d(TAG,userData.getUserName()+" pic head url "+userData.getUserHeadPicURL());
                    break;
                case READ_USERDATA_FROM_LOCAL:
                    if (userData!=null){
                        //说明本地读取成功
                        simpleDraweeView_head.setImageURI(userData.getUserHeadPicURL());
                        textView_userName.setText(userData.getUserName());
                        Log.d(TAG,userData.getUserName()+" pic head url "+userData.getUserHeadPicURL());
                    }else {
                        //否则从网络读取
                        exec.execute(new TaskLoadUserData(Constant.accessToken.getUid()));
                    }
                    break;
            }
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
            if (Constant.accessToken==null){
                return;
            }
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
                    message.arg1= FILL_WEIBO_FROM_WEB_UP;
                    message.arg2=jsonArray.length();
                    handler.sendMessage(message);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class TaskLoadOldWeibo implements Runnable{
        private String max_id;
        public TaskLoadOldWeibo(String max_id){
            this.max_id=Long.parseLong(max_id)-1+"";
        }
        @Override
        public void run() {
            //获取比给定微博ID早的微博
            String urlString="https://api.weibo.com/2/statuses/home_timeline.json?access_token="+Constant.accessToken.getToken()+"&max_id="+max_id;
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
                    message.arg1= FILL_WEIBO_FROM_WEB_DOWN;
                    message.arg2=jsonArray.length();
                    handler.sendMessage(message);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class TaskLoadUserData implements Runnable{
        private String userId;
        TaskLoadUserData(String userId){
            this.userId=userId;
        }
        @Override
        public void run() {
            //这里使用UID获取用户信息，链接可能不对
            String urlString="https://api.weibo.com/2/users/show.json?access_token="+Constant.accessToken.getToken()+"&uid="+userId;
            String userDataJson= WebTools.getWebString(urlString);
            if (userDataJson==null){
                //从网络中读取错误
                Message message=new Message();
                message.arg1=READ_WEB_ERROR;
                handler.sendMessage(message);
                Log.d(TAG,"Refresh userdata failed");
            }else {
                try{
                    JSONObject jsonObject=new JSONObject(userDataJson);
                    userData=ParseJsonTools.getUserDataFromJson(jsonObject);
                    Message message=new Message();
                    message.arg1=READ_USERDATA_FROM_WEB;
                    handler.sendMessage(message);
                    Log.d(TAG,"get userdata from web "+userId);
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
    private class TaskSaveUserToLocal implements Runnable{
        @Override
        public void run() {
            Tools.saveUserDataToLocal(userData);
        }
    }
    private class TaskReadUserDataFromLocal implements Runnable{
        private String userId;
        TaskReadUserDataFromLocal(String userId){
            this.userId=userId;
        }
        @Override
        public void run() {
            userData=Tools.readUserDataFromLocal(userId);
            Message message=new Message();
            message.arg1=READ_USERDATA_FROM_LOCAL;
            handler.sendMessage(message);
        }
    }
    private class TaskReadWeiboListFromLocal implements Runnable{
        @Override
        public void run() {
            tempList=Tools.readWeiboListFromLocal();
            Message message=new Message();
            message.arg1=FILL_WEIBO_FROM_LOCAL;
            handler.sendMessage(message);
        }
    }

    private class UserClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(MainPageActivity.this,UserDataActivity.class);
            intent.putExtra("apqx",userData.getUserName());
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }





}
