package me.apqx.pocketweibo.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import me.apqx.pocketweibo.AppThreadPool;
import me.apqx.pocketweibo.Constant;
import me.apqx.pocketweibo.MyApplication;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.WeiboItemRecyclerAdapter;
import me.apqx.pocketweibo.model.WeiboServer;
import me.apqx.pocketweibo.presenter.DownloadPresenter;
import me.apqx.pocketweibo.presenter.IDownloadPresenter;
import me.apqx.pocketweibo.presenter.IUserPagePresenter;
import me.apqx.pocketweibo.presenter.UserPagePresenter;
import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.bean.UserData;
import me.apqx.pocketweibo.bean.WeiboItemData;
import me.apqx.pocketweibo.model.ViewTools;
import me.apqx.pocketweibo.model.WebTools;
import me.apqx.pocketweibo.customView.SwipeActivityHelper;
import me.apqx.pocketweibo.customView.SwipeActivityLayout;

/**
 * Created by apqx on 2017/5/16.
 */

public class UserDataActivity extends AppCompatActivity implements IUserDataView{
    private final String TAG=this.getClass().getSimpleName();

    private SwipeActivityHelper swipeActivityHelper;

    private Toolbar toolbar;
    private MenuItem menuItem_follow;
    private SimpleDraweeView imageView_head;
    private SimpleDraweeView imageView_bg;
    private SimpleDraweeView imageView_gender;
    private TextView textView_name;
    private TextView textView_location;
    private TextView textView_followers;
    private TextView textView_following;
    private TextView textView_description;
    private RecyclerView recyclerView;
    private List<WeiboItemData> list;
    private WeiboItemRecyclerAdapter adapter;
    private UserData userData;
    private ExecutorService exec= AppThreadPool.getThreadPool();
//    private Handler handler;

    private IUserPagePresenter userPagePresenter;
    private IDownloadPresenter downloadPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyApplication.getMyTheme()==MyApplication.THEME_DARK){
            setTheme(R.style.AppTheme_Dark_Transparent);
        }else {
            setTheme(R.style.AppTheme_Light_Transparent);
        }
        setContentView(R.layout.layout_user_page);
        userPagePresenter=new UserPagePresenter(this);
        downloadPresenter=new DownloadPresenter();
        toolbar=(Toolbar)findViewById(R.id.toolbar_userData);
        swipeActivityHelper=new SwipeActivityHelper(this);
        swipeActivityHelper.onActivityCreate();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView_head=(SimpleDraweeView) findViewById(R.id.imageView_user_page_head);
        imageView_bg=(SimpleDraweeView) findViewById(R.id.imageView_user_page_bg);
        imageView_gender=(SimpleDraweeView)findViewById(R.id.imageView_user_page_gender);
        textView_name=(TextView)findViewById(R.id.textView_user_page_name);
        textView_location=(TextView)findViewById(R.id.textView_user_page_location);
        textView_followers=(TextView)findViewById(R.id.textView_user_page_followers);
        textView_following=(TextView)findViewById(R.id.textView_user_page_following);
        textView_description=(TextView)findViewById(R.id.textView_user_page_description);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView_user_page_weibo);
        list=new ArrayList<WeiboItemData>();
        adapter=new WeiboItemRecyclerAdapter(list,R.layout.layout_weibo_recycler_item,WeiboItemRecyclerAdapter.WEIBO_USER_PAGE,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

//        handler=new DataHandler();
        Intent intent=getIntent();
        String userName=intent.getStringExtra("apqx");
        userData= MainPageActivity.getUserData(userName);
        if (userData==null){
            //这时应该联网查询
//            exec.execute(new TaskGetUserDataFromWeb(userName));
            userPagePresenter.refreshUserData(userName,null);
        }else {
            setUserDataToView();
        }

        //联网获取这个用户的微博
//        exec.execute(new TaskGetUserWeiboFromWeb(userName));
        userPagePresenter.refreshUserWeibo(userName);

    }
    private void setUserDataToView(){
        //根据获得UserData填充界面
        textView_name.setText(userData.getUserName());
        textView_location.setText(userData.getLocation());
        textView_followers.setText(getString(R.string.followers)+" "+userData.getFollowerCount());
        textView_following.setText(getString(R.string.following)+" "+userData.getFollowingCount());
        textView_description.setText(userData.getProfileDescription());
        imageView_head.setImageURI(userData.getUserHeadPicURL());
        imageView_bg.setImageURI(userData.getProfileBGUrl());

    }



    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        swipeActivityHelper.onPostCreate();
        swipeActivityHelper.setOnFinishActivity(new SwipeActivityLayout.OnFinishActivity() {
            @Override
            public void finishActivity() {
                UserDataActivity.this.finish();
            }
        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                break;
            default:break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userData!=null){
            getMenuInflater().inflate(R.menu.menu_user_page_follow,menu);
            menuItem_follow=menu.findItem(R.menu.menu_user_page_follow);
        }
        return true;
    }

    @Override
    public void showUserData(UserData userData) {
        this.userData=userData;
        setUserDataToView();
    }

    @Override
    public void notifyWeiboDataChanged(List<WeiboItemData> weiboList,boolean isNew) {
        if (weiboList==null){
            //表示无法获取用户发送的微博
            ViewTools.showToast(R.string.api_limit);
            return;
        }
        if (isNew){
            list.clear();
            list.addAll(weiboList);
        }else {
            list.addAll(weiboList);
        }
        adapter.notifyDataSetChanged();
    }

//    private class DataHandler extends Handler{
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.arg1){
//                case TYPE_GET_USERDATA_FROM_WEB:
//                    setUserDataToView();
//                    break;
//                case TYPE_GET_WEIBO_FROM_WEB:
//                    adapter.notifyDataSetChanged();
//                    break;
//                case TYPE_GET_USERDATA_FROM_WEB_ERROR:
//                    ViewTools.showToast(R.string.refresh_userdata_failed);
//                    break;
//                case TYPE_GET_WEIBO_FROM_WEB_ERROR:
//                    ViewTools.showToast(R.string.refresh_weibo_failed);
//                    break;
//                default:break;
//            }
//        }
//    }

//    private class TaskGetUserWeiboFromWeb implements Runnable{
//        private String userName;
//        public TaskGetUserWeiboFromWeb(String userName){
//            this.userName=userName;
//        }
//        @Override
//        public void run() {
//            String urlString="https://api.weibo.com/2/statuses/user_timeline.json?access_token="+ Constant.accessToken.getToken()+"&screen_name="+userName;
//            String weiboJson= WebTools.getWebString(urlString);
//            if (weiboJson==null){
//                //从网络中读取错误
//                Message message=new Message();
//                message.arg1=TYPE_GET_WEIBO_FROM_WEB_ERROR;
//                handler.sendMessage(message);
//                Log.d(TAG,"Refresh weibo failed");
//            }else {
//                try{
//                    JSONObject jsonObject=new JSONObject(weiboJson);
//                    JSONArray jsonArray=jsonObject.getJSONArray("statuses");
//                    if (jsonArray!=null&&jsonArray.length()>0){
//                        //向上刷新，应该清空列表，重新加载
//                        list.clear();
//                        for (int i=0;i<jsonArray.length();i++){
//                            list.add(ParseJsonTools.getWeiboFromJson(jsonArray.getJSONObject(i)));
//                        }
//                        Log.d(TAG,"get Weibo from web "+jsonArray.length());
//                    }
//                    Message message=new Message();
//                    message.arg1=TYPE_GET_WEIBO_FROM_WEB;
//                    handler.sendMessage(message);
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//    private class TaskGetUserDataFromWeb implements Runnable{
//        private String userName;
//        public TaskGetUserDataFromWeb(String userName){
//            this.userName=userName;
//        }
//        @Override
//        public void run() {
//            String urlString="https://api.weibo.com/2/users/show.json?access_token="+Constant.accessToken.getToken()+"&screen_name="+userName;
//            String userDataJson= WebTools.getWebString(urlString);
//            if (userDataJson==null){
//                //从网络中读取错误
//                Message message=new Message();
//                message.arg1=TYPE_GET_USERDATA_FROM_WEB_ERROR;
//                handler.sendMessage(message);
//                Log.d(TAG,"Refresh userdata failed");
//            }else {
//                try{
//                    JSONObject jsonObject=new JSONObject(userDataJson);
//                    userData=ParseJsonTools.getUserDataFromJson(jsonObject);
//                    Message message=new Message();
//                    message.arg1=TYPE_GET_USERDATA_FROM_WEB;
//                    handler.sendMessage(message);
//                    Log.d(TAG,"get userdata from web "+userName);
//                }catch (JSONException e){
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

}
