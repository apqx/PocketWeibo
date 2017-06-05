package me.apqx.pocketweibo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    static final int SEND_COMMENT_ERROR=3;
    static final int SEND_COMMENT_SUCCESS=4;

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
    private String weiboID;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MyApplication.getMyTheme()==MyApplication.THEME_DARK){
            setTheme(R.style.AppTheme_Dark_Transparent);
        }else {
            setTheme(R.style.AppTheme_Light_Transparent);
        }
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
        //加载微博详细信息
        listWeiboDetail=new ArrayList<WeiboItemData>(1);
        weiboItemRecyclerAdapter=new WeiboItemRecyclerAdapter(listWeiboDetail,R.layout.layout_weibo_recycler_item,WeiboItemRecyclerAdapter.WEIBO_DETAIL,this);
        RecyclerView.LayoutManager weboItemLayoutManager=new LinearLayoutManager(this);
        recyclerViewWeiboDetail.setLayoutManager(weboItemLayoutManager);
        recyclerViewWeiboDetail.setAdapter(weiboItemRecyclerAdapter);

        Intent intent=getIntent();
        String weibo=intent.getStringExtra("weibo");
        weiboID=intent.getStringExtra("apqx");

        WeiboItemData weiboItemData=ParseJsonTools.getWeiboFromJson(ParseJsonTools.getJSONObjectFromString(weibo));
        listWeiboDetail.add(weiboItemData);
        radioLikes.setText(getText(R.string.weibo_item_likes)+" "+weiboItemData.getLikeCount());
        radioComment.setText(getText(R.string.weibo_item_comment)+" "+weiboItemData.getCommentCount());
        radioRepost.setText(getText(R.string.weibo_item_reposts)+" "+weiboItemData.getRePostCount());
        weiboItemRecyclerAdapter.notifyDataSetChanged();

        //当点击了评论按钮，则会跳转到微博详细界面，并弹出评论窗口
        boolean clickComment=intent.getBooleanExtra("clickComment",false);
        if (clickComment){
            popUpToComment(weiboID);
        }


        //加载评论
        recyclerViewComment=(RecyclerView)findViewById(R.id.recyclerView_weibo_detail_comment);
        listComments=new ArrayList<CommentData>();
        commentItemRecyclerAdapter=new CommentItemRecyclerAdapter(R.layout.layout_comment_item,listComments);
        RecyclerView.LayoutManager commentLayoutManager=new LinearLayoutManager(this);
        recyclerViewComment.setLayoutManager(commentLayoutManager);
        recyclerViewComment.addItemDecoration(new RecyclerItemDecor());
        recyclerViewComment.setAdapter(commentItemRecyclerAdapter);
        handler=new DataHandler();
        exec.execute(new TaskReadNewComment(weiboID));

        weiboItemRecyclerAdapter.setOnCommentClickListener(new WeiboItemRecyclerAdapter.OnCommentClickListener() {
            @Override
            public void commentClick() {
                popUpToComment(weiboID);
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
                case SEND_COMMENT_ERROR:
                    Tools.showToast(getString(R.string.operation_failed));
                    break;
                case SEND_COMMENT_SUCCESS:
                    Tools.showToast(getString(R.string.operation_success));
                    long commentCount=Long.parseLong(weiboItemData.getCommentCount())+1;
                    weiboItemData.setCommentCount(String.valueOf(commentCount));
                    radioComment.setText(WeiboDetailActivity.this.getString(R.string.weibo_item_comment)+" "+commentCount);
                    commentItemRecyclerAdapter.notifyDataSetChanged();
                    break;
                default:break;
            }
        }
    }


    private void popUpToComment(final String weiboID){
        //应该弹出评论窗口
        View view= LayoutInflater.from(this).inflate(R.layout.layout_dialog_comment,null);
        final EditText editText=(EditText)view.findViewById(R.id.editText_comment);
        ImageButton btn_send=(ImageButton)view.findViewById(R.id.btn_send_comment);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog=builder.create();
        dialog.show();
        editText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager=(InputMethodManager)editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editText,InputMethodManager.SHOW_FORCED);
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exec.execute(new TaskSendComment(editText.getText().toString(),weiboID));
            }
        });
    }

    private class TaskSendComment implements Runnable{
        String comment;
        String id;
        TaskSendComment(String comment,String id){
            this.comment=comment;
            this.id=id;
        }
        @Override
        public void run() {
            String urlString="https://api.weibo.com/2/comments/create.json";
            try {
                comment= URLEncoder.encode(comment,"UTF-8");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
            String post="access_token="+Constant.accessToken.getToken()+"&comment="+comment+"&id="+id;
            String newComment=WebTools.postWebString(urlString,post);
            if (newComment==null){
                //说明网络错误
                Message message=new Message();
                message.arg1=SEND_COMMENT_ERROR;
                handler.sendMessage(message);
            }else {
                try {
                    JSONObject jsonObject=new JSONObject(newComment);
                    CommentData commentData=ParseJsonTools.getCommentDataFromJaon(jsonObject);
                    listComments.add(0,commentData);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Message message=new Message();
                message.arg1=SEND_COMMENT_SUCCESS;
                handler.sendMessage(message);
            }
        }
    }

    private class TaskReadNewComment implements Runnable{
        String weiboId;
        TaskReadNewComment(String weiboId){
            this.weiboId=weiboId;
        }
        @Override
        public void run() {
            String urlString="https://api.weibo.com/2/comments/show.json?access_token="+Constant.accessToken.getToken()+"&id="+weiboId;
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
    }

    
}
