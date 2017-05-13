package me.apqx.pocketweibo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.apqx.pocketweibo.struct.ParseJsonTools;
import me.apqx.pocketweibo.struct.WeiboItemData;
import me.apqx.pocketweibo.tools.WebTools;

/**
 * Created by apqx on 2017/5/9.
 */

public class WeiboDetailActivity extends AppCompatActivity {
    private static final String TAG="WeiboDetailActivity";
    private Toolbar toolbar;
    private RadioButton radioLikes,radioComment,radioRepost;
    private RecyclerView recyclerViewWeiboDetail,recyclerViewLike,recyclerViewComment,recyclerViewRepost;
    private WeiboItemRecyclerAdapter weiboItemRecyclerAdapter;
    private List<WeiboItemData> listWeiboDetail;
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

        Intent intent=getIntent();
        listWeiboDetail=new ArrayList<WeiboItemData>(1);
        weiboItemRecyclerAdapter=new WeiboItemRecyclerAdapter(listWeiboDetail,R.layout.layout_weibo_recycler_item,WeiboItemRecyclerAdapter.WEIBO_DETAIL);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerViewWeiboDetail.setLayoutManager(layoutManager);
        recyclerViewWeiboDetail.setAdapter(weiboItemRecyclerAdapter);

        String weiboID=intent.getStringExtra("apqx");
        if (intent.hasExtra("reTwitter")){
            listWeiboDetail.add(MainPageActivity.getWeiboItem(weiboID).getReTwitterWeibo());
        }else {
            listWeiboDetail.add(MainPageActivity.getWeiboItem(weiboID));
        }
        weiboItemRecyclerAdapter.notifyDataSetChanged();
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
    
}
