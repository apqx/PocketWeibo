package me.apqx.pocketweibo;

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
import android.view.MenuItem;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import me.apqx.pocketweibo.struct.CommentData;
import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.struct.WeiboItemData;
import me.apqx.pocketweibo.tools.Tools;
import me.apqx.pocketweibo.tools.WebTools;
import me.apqx.pocketweibo.view.SwipeActivityHelper;
import me.apqx.pocketweibo.view.SwipeActivityLayout;

/**
 * Created by apqx on 2017/5/9.
 */

public class WeiboDetailActivity extends AppCompatActivity {
    private static final String TAG="WeiboDetailActivity";
    private static final int NOTIFY_LIKE=0;
    private static final int NOTIFY_COMMENT=1;
    private static final int NOTIFY_REPOST=2;

    private Toolbar toolbar;
    private ExecutorService exec;
    private Handler handler;
    private RadioButton radioLikes;
    private RadioButton radioComment;
    private RadioButton radioRepost;
    private RecyclerView recyclerViewWeiboDetail;
    private RecyclerView recyclerViewLike;
    private RecyclerView recyclerViewComment;
    private RecyclerView recyclerViewRepost;
    private WeiboItemRecyclerAdapter weiboItemRecyclerAdapter;
    private List<WeiboItemData> listWeiboDetail;
    private List<CommentData> listComments;
    private CommentItemRecyclerAdapter commentItemRecyclerAdapter;
    private WeiboItemData weiboItemData;
    private SwipeActivityHelper swipeActivityHelper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_weibo_detail);
        toolbar=(Toolbar)findViewById(R.id.toolbar_WeiboDetail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerViewWeiboDetail=(RecyclerView)findViewById(R.id.recyclerView_weibo_Detail);
        recyclerViewLike=(RecyclerView)findViewById(R.id.recyclerView_weibo_detail_likes);
        recyclerViewComment=(RecyclerView)findViewById(R.id.recyclerView_weibo_detail_comment);
        recyclerViewRepost=(RecyclerView)findViewById(R.id.recyclerView_weibo_detail_repost);
        radioLikes=(RadioButton)findViewById(R.id.radio_weibo_detail_like);
        radioComment=(RadioButton)findViewById(R.id.radio_weibo_detail_comment);
        radioRepost=(RadioButton)findViewById(R.id.radio_weibo_detail_repost);
        exec= AppThreadPool.getThreadPool();

        swipeActivityHelper=new SwipeActivityHelper(this);
        swipeActivityHelper.onActivityCreate();

        Intent intent=getIntent();
        final String weiboID=intent.getStringExtra("apqx");
        if (intent.hasExtra("reTwitter")){
            weiboItemData=MainPageActivity.getWeiboItem(weiboID).getReTwitterWeibo();
        }else {
            weiboItemData=MainPageActivity.getWeiboItem(weiboID);
        }

        radioLikes.setText(getText(R.string.weibo_item_likes)+" "+weiboItemData.getLikeCount());
        radioComment.setText(getText(R.string.weibo_item_comment)+" "+weiboItemData.getCommentCount());
        radioRepost.setText(getText(R.string.weibo_item_reposts)+" "+weiboItemData.getRePostCount());


        //加载微博详细信息
        listWeiboDetail=new ArrayList<WeiboItemData>(1);
        weiboItemRecyclerAdapter=new WeiboItemRecyclerAdapter(listWeiboDetail,R.layout.layout_weibo_recycler_item,WeiboItemRecyclerAdapter.WEIBO_DETAIL,this);
        RecyclerView.LayoutManager weboItemLayoutManager=new LinearLayoutManager(this);
        recyclerViewWeiboDetail.setLayoutManager(weboItemLayoutManager);
        listWeiboDetail.add(weiboItemData);
        recyclerViewWeiboDetail.setAdapter(weiboItemRecyclerAdapter);

        //加载评论
        recyclerViewComment=(RecyclerView)findViewById(R.id.recyclerView_weibo_detail_comment);
        listComments=new ArrayList<CommentData>();
        commentItemRecyclerAdapter=new CommentItemRecyclerAdapter(R.layout.layout_comment_item,listComments);
        RecyclerView.LayoutManager commentLayoutManager=new LinearLayoutManager(this);
        recyclerViewComment.setLayoutManager(commentLayoutManager);
        recyclerViewComment.addItemDecoration(new RecyclerItemDecor());
        recyclerViewComment.setAdapter(commentItemRecyclerAdapter);
        handler=new DataHandler();
        exec.execute(new Runnable() {
            @Override
            public void run() {
                String urlString="https://api.weibo.com/2/comments/show.json?access_token="+Constant.accessToken.getToken()+"&id="+weiboID;
                String comments=WebTools.getWebString(urlString);
                if (comments==null){
                    //从网络中读取错误
                }else {
                    try{
                        JSONObject jsonObject=new JSONObject(comments);
                        JSONArray jsonArray=jsonObject.getJSONArray("comments");
                        Log.d(TAG,"get comment num = "+jsonArray.length());
                        for (int i=0;i<jsonArray.length();i++){
                            listComments.add(ParseJsonTools.getCommentDataFromJaon(jsonArray.getJSONObject(i)));
                        }
                        Message message=new Message();
                        message.arg1=NOTIFY_COMMENT;
                        handler.sendMessage(message);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }
            }
        });
    }
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        swipeActivityHelper.onPostCreate();
        swipeActivityHelper.setOnFinishActivity(new SwipeActivityLayout.OnFinishActivity() {
            @Override
            public void finishActivity() {
                WeiboDetailActivity.this.finish();
            }
        });
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

    private class DataHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.arg1){
                case NOTIFY_COMMENT:
                    commentItemRecyclerAdapter.notifyDataSetChanged();
                    break;
                case NOTIFY_LIKE:
                    break;
                case NOTIFY_REPOST:
                    break;
                default:break;
            }
        }
    }
    
}
