package me.apqx.pocketweibo.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import me.apqx.pocketweibo.AppThreadPool;
import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.MyApplication;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.SettingActivity;
import me.apqx.pocketweibo.WeiboItemRecyclerAdapter;
import me.apqx.pocketweibo.presenter.DownloadPresenter;
import me.apqx.pocketweibo.presenter.IDownloadPresenter;
import me.apqx.pocketweibo.presenter.IMainPagePresenter;
import me.apqx.pocketweibo.presenter.MainPagePresenter;
import me.apqx.pocketweibo.service.NotifyReceiver;
import me.apqx.pocketweibo.service.NotifyService;
import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;
import me.apqx.pocketweibo.model.Settings;
import me.apqx.pocketweibo.model.ViewTools;

/**
 * Created by apqx on 2017/4/19.
 * 主页面
 */

public class MainPageActivity extends AppCompatActivity implements View.OnClickListener ,IMainPageView{
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

    //refactor
    private IMainPagePresenter mainPagePresenter;
    private IDownloadPresenter downloadPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApplication.getMyTheme()==MyApplication.THEME_DARK){
            setTheme(R.style.AppTheme_Dark_Main);
        }else {
            setTheme(R.style.AppTheme_Light_Main);
        }
        setContentView(R.layout.layout_main);
        mainPagePresenter=new MainPagePresenter(this);
        downloadPresenter=new DownloadPresenter();
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawerLayout);
        toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.nav_open,R.string.nav_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
        navigationView=(NavigationView)findViewById(R.id.navigationView);

        fab=(FloatingActionButton)findViewById(R.id.fab);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipeRefreshLayout_main_page);
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

        //只有在用户已登录的情况下才加载微博信息
        if (Constant.accessToken!=null){
            Intent intent=getIntent();
            //从登陆界面获得uid
            String uid=Constant.accessToken.getUid();
            String fromService=intent.getAction();
            if (fromService==NotifyService.ACTION_FROM_SERVICE){
                //如果是用户点击Notification而启动的微博页面，应该立即联网刷新
                if (!swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(true);
                    mainPagePresenter.refreshNewWeibo();
                }
            }else {
                //启动时，首先从本地读取保存好的微博，如果本地不存在缓存文件，就从网络中读取
                mainPagePresenter.readWeiboFromLocal(Constant.accessToken.getUid());
            }
            //启动时，先从本地读取用户信息
            mainPagePresenter.readUserDataFromLocal(uid);
        }


        adapter.setOnRefreshDownListener(new WeiboItemRecyclerAdapter.OnRefreshDownListener() {
            @Override
            public void refreshDown() {
                if (list.size()>0){
                    String maxId=Long.parseLong(list.get(list.size()-1).getWeiboId())-1+"";
                    mainPagePresenter.refreshOldWeibo(maxId);
                }
            }
        });
        Settings settings=new Settings(this);

        mainPagePresenter=new MainPagePresenter(this);
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
        Intent intent=new Intent(NotifyReceiver.ACTION_SHOUND_NOTIFY_WEIBO);
        sendBroadcast(intent);
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
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        sendBroadcast(new Intent(NotifyReceiver.ACTION_SHOUND_NOT_NOTIFY_WEIBO));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==0){
            if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                //说明申请权限成功
                if (Constant.urlList.size()>0){
                    for (String urlString:Constant.urlList){
                        downloadPresenter.downloadPicture(urlString);
                    }
                }
            }else {
                ViewTools.showToast(getString(R.string.permission_denied));
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
                    mainPagePresenter.refreshNewWeibo();
                }
                break;
        }
    }

    @Override
    public void toggleSwipeRefreshIfNoRefreshing() {
        if (!swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void toggleSwipeRefreshIfIsRefreshing() {
        if (swipeRefreshLayout.isRefreshing()){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void notifyWeiboListChanged(List<WeiboItemData> weiboItemDataList,boolean isNew,boolean isFromLocal) {
        if (isFromLocal){
            list.addAll(weiboItemDataList);
            adapter.notifyDataSetChanged();
            return;
        }
        if (isNew){
            list.clear();
            list.addAll(weiboItemDataList);
        }else {
            list.addAll(weiboItemDataList);
        }
        mainPagePresenter.saveWeibosToLocal(list,Constant.accessToken.getUid());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showMainUserData(UserData userData) {
        this.userData=userData;
        simpleDraweeView_head.setImageURI(userData.getUserHeadPicURL());
        textView_userName.setText(userData.getUserName());
        Log.d(TAG,userData.getUserName()+" pic head url "+userData.getUserHeadPicURL());
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
}
