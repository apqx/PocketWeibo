package me.apqx.pocketweibo;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import me.apqx.pocketweibo.struct.UserData;
import me.apqx.pocketweibo.struct.WeiboItemData;
import me.apqx.pocketweibo.tools.ViewTools;
import me.apqx.pocketweibo.view.LinkTextView;

/**
 * Created by apqx on 2017/5/4.
 * 主页面的RecyclerAdapter
 */

public class WeiboItemRecyclerAdapter extends RecyclerView.Adapter<WeiboItemRecyclerAdapter.MyViewHolder> {
    private static final String TAG="WeiboItemAdapter";
    public static final int WEIBO_MAINPAGE_LIST=0;
    public static final int WEIBO_DETAIL=1;
    private List<WeiboItemData> list;
    private int resource;
    private int weiboType;
    private PopupWindow popupWindow;
    private MyViewHolder myViewHolder;

    private TextView textView_SavePost,textView_UnFollow;
    private ImageButton btnSavePost,btnUnFollow;
    private LinearLayout linearLayoutSavePost,linearLayoutUnFollow;

    private MyOnClickListener onClickListener;

    public WeiboItemRecyclerAdapter(List<WeiboItemData> list, int resource,int weiboType) {
        super();
        this.list=list;
        this.resource=resource;
        this.weiboType=weiboType;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(resource,parent,false);
        myViewHolder=new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WeiboItemData weiboItemData=list.get(position);
        UserData userData=weiboItemData.getWeiboUserData();
        holder.imageView_head.setImageResource(R.mipmap.pic);
        holder.imageView_pic.setImageResource(R.mipmap.pic);
        holder.textView_time.setText(weiboItemData.getCreateTime());
        holder.textView_device.setText(weiboItemData.getDevice());
        holder.textView_likeCount.setText(weiboItemData.getLikeCount());
        holder.textView_rePostCount.setText(weiboItemData.getRePostCount());
        holder.textView_commentCount.setText(weiboItemData.getCommentCount());
        holder.textView_name.setText(userData.getUserName());
        holder.textView_content.setText(weiboItemData.getContent());

        //把微博ID保存在View里,这样当点击列表Item的时候可以明确的知道点击的是哪个微博
        holder.textView_content.setTag(weiboItemData.getWeiboId());
        holder.btnExpand.setTag(weiboItemData.getWeiboId());

        if (weiboItemData.hasReTwitter()){
            WeiboItemData reTwitterWeibo=weiboItemData.getReTwitterWeibo();
            holder.imageView_reTwitter_pic.setImageResource(R.mipmap.pic);
            holder.textView_reTwitter_state.setText(getReTwitterState(reTwitterWeibo.getRePostCount(),reTwitterWeibo.getCommentCount(),reTwitterWeibo.getLikeCount()));
            holder.textView_reTwitter_content.setText("@"+reTwitterWeibo.getWeiboUserData().getUserName()+": "+reTwitterWeibo.getContent());
            //把被转发的微博ID保存在View里
            holder.textView_reTwitter_content.setTag(weiboItemData.getWeiboId());
        }else {
            holder.relativeLayout_reTwitterMain.setVisibility(View.GONE);
        }
//        holder.btnExpand.setOnClickListener(new MyOnClickListener());

    }
    //获取转发微博的状态
    private String getReTwitterState(String rePostCount,String commentCount,String likeCount){
        String rePost=MyApplication.getContext().getString(R.string.weibo_item_reposts);
        String comments=MyApplication.getContext().getString(R.string.weibo_item_comment);
        String likes=MyApplication.getContext().getString(R.string.weibo_item_likes);
        return String.format(rePost+"  %-10s"+comments+"  %-10s"+likes+"  %-10s",rePostCount,commentCount,likeCount);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView_head,imageView_pic,imageView_reTwitter_pic;
        private TextView textView_name,textView_time,textView_device,textView_content,textView_reTwitter_content,textView_reTwitter_state,
                textView_likeCount,textView_commentCount,textView_rePostCount;
        private RelativeLayout relativeLayout_reTwitterMain;
        private ImageButton btnExpand;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView_head=(ImageView)itemView.findViewById(R.id.imageView_main_item_head);
            imageView_pic=(ImageView)itemView.findViewById(R.id.imageView_main_item_image);
            imageView_pic.setVisibility(View.GONE);
            imageView_reTwitter_pic=(ImageView)itemView.findViewById(R.id.imageView_main_item_reTwitter_image);
            imageView_reTwitter_pic.setVisibility(View.GONE);
            textView_name=(TextView)itemView.findViewById(R.id.textView_main_item_name);
            textView_time=(TextView)itemView.findViewById(R.id.textView_main_item_time);
            textView_device=(TextView)itemView.findViewById(R.id.textView_main_item_device);
            textView_content=(TextView)itemView.findViewById(R.id.textView_main_item_content);
            textView_reTwitter_content=(TextView)itemView.findViewById(R.id.textView_main_item_content_reTwitter);
            textView_reTwitter_state=(TextView)itemView.findViewById(R.id.textView_main_item_reTwitter_state);
            textView_likeCount=(TextView)itemView.findViewById(R.id.textView_main_item_likeCount);
            textView_commentCount=(TextView)itemView.findViewById(R.id.textView_main_item_commentCount);
            textView_rePostCount=(TextView)itemView.findViewById(R.id.textView_main_item_rePostCount);
            relativeLayout_reTwitterMain=(RelativeLayout)itemView.findViewById(R.id.relativeLayout_main_item_reTwitter);
            btnExpand=(ImageButton)itemView.findViewById(R.id.btn_main_item_expand);
            btnSavePost=(ImageButton)itemView.findViewById(R.id.imageButton_expand_addToFavorite);
            btnUnFollow=(ImageButton)itemView.findViewById(R.id.imageButton_expand_follow);
            linearLayoutSavePost=(LinearLayout)itemView.findViewById(R.id.linearLayout_savePost);
            linearLayoutUnFollow=(LinearLayout)itemView.findViewById(R.id.linearLayout_unFollow);
            textView_SavePost=(TextView)itemView.findViewById(R.id.textView_expand_addToFavorite);
            textView_UnFollow=(TextView)itemView.findViewById(R.id.textView_expand_follow);

            onClickListener=new MyOnClickListener();
            btnExpand.setOnClickListener(onClickListener);
            textView_content.setOnClickListener(onClickListener);
            textView_reTwitter_content.setOnClickListener(onClickListener);
        }
    }
    //唐初窗口显示对微博的操作选项，收藏，关注
    private void showWeiboItemExpandWindow(final View view,String weiboId){
        View expandView=LayoutInflater.from(view.getContext()).inflate(R.layout.layout_weibo_expand,null);
        btnSavePost=(ImageButton)expandView.findViewById(R.id.imageButton_expand_addToFavorite);
        btnUnFollow=(ImageButton)expandView.findViewById(R.id.imageButton_expand_follow);
        linearLayoutSavePost=(LinearLayout)expandView.findViewById(R.id.linearLayout_savePost);
        linearLayoutUnFollow=(LinearLayout)expandView.findViewById(R.id.linearLayout_unFollow);
        textView_SavePost=(TextView)expandView.findViewById(R.id.textView_expand_addToFavorite);
        textView_UnFollow=(TextView)expandView.findViewById(R.id.textView_expand_follow);
        WeiboItemData weiboItemData=MainPageActivity.getWeiboItem(weiboId);
        if (weiboItemData==null){
            Toast.makeText(view.getContext(),"Error",Toast.LENGTH_SHORT).show();
            return;
        }
        UserData userData=weiboItemData.getWeiboUserData();
        if (weiboItemData.isFavorited()){
            //如果当前微博已经被收藏
            btnSavePost.setImageResource(R.drawable.icon_star);
            textView_SavePost.setText(R.string.unsave_post);
        }else {
            btnSavePost.setImageResource(R.drawable.icon_unstar);
            textView_SavePost.setText(R.string.save_post);
        }
        if (userData.isFollowed()){
            //如果已经关注了当前用户
            btnUnFollow.setImageResource(R.drawable.icon_follow);
            textView_UnFollow.setText(R.string.unfollow);
        }else {
            btnUnFollow.setImageResource(R.drawable.icon_unfollow);
            textView_UnFollow.setText(R.string.follow);
        }

        linearLayoutSavePost.setOnClickListener(onClickListener);
        linearLayoutUnFollow.setOnClickListener(onClickListener);
        if (popupWindow==null){
            popupWindow=new PopupWindow(expandView, ViewTools.dpToPx(view.getContext(),150),ViewTools.dpToPx(view.getContext(),130));
        }
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        int xPos=(int)view.getX();
        int yPos=(int)view.getY();
        popupWindow.showAsDropDown(view,xPos,yPos);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                view.animate().setDuration(500).rotation(0).start();
            }
        });

    }
    class MyOnClickListener implements View.OnClickListener{
        String weiboId;
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_main_item_expand:
                    weiboId=v.getTag().toString();
                    //此时应该弹出窗口
                    v.animate().setDuration(500).rotation(180).start();
                    showWeiboItemExpandWindow(v,weiboId);
                    break;
                case R.id.textView_main_item_content:
                    weiboId=v.getTag().toString();
                    LinkTextView linkTextView=(LinkTextView)v;
                    //当点击TextView内部的链接时，不应该再执行这里的点击事件
                    if (!linkTextView.shouldInterruptClick()){
                        //这不是被转发的微博，所以在该微博的详细界面，点击不再跳转
                        if (weiboType==WEIBO_MAINPAGE_LIST){
                            //跳转到微博详细界面，传输微博ID
                            Intent intent=new Intent(v.getContext(),WeiboDetailActivity.class);
                            intent.putExtra("apqx",weiboId);
                            v.getContext().startActivity(intent);
                        }
                    }
                    linkTextView.donotInterruptClick();
                    break;
                case R.id.textView_main_item_content_reTwitter:
                    weiboId=v.getTag().toString();
                    LinkTextView textView=(LinkTextView)v;
                    if (!textView.shouldInterruptClick()) {
                        //跳转到微博详细界面
                        Intent reIntent = new Intent(v.getContext(), WeiboDetailActivity.class);
                        reIntent.putExtra("reTwitter",true);
                        reIntent.putExtra("apqx", weiboId);
                        v.getContext().startActivity(reIntent);
                    }
                    textView.donotInterruptClick();
                    break;
                case R.id.linearLayout_savePost:
                    //因为点击此之前一定点击btn_main_item_expand，所以可以获得微博ID
                    //根据本地内存数据判断当前状态，联网请求改变状态，如果成功，则同时改变本地保存的状态
                    WeiboItemData SaveWeiboItemData=MainPageActivity.getWeiboItem(weiboId);
                    if (SaveWeiboItemData==null){
                        Toast.makeText(v.getContext(),"Error",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (SaveWeiboItemData.isFavorited()){
                        //如果已经被收藏了
                        Toast.makeText(v.getContext(), R.string.remove_from_favorite,Toast.LENGTH_SHORT).show();
                    }else {
                        //如果没有被收藏
                        Toast.makeText(v.getContext(), R.string.add_to_favorite,Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.linearLayout_unFollow:
                    WeiboItemData followWeiboItemData=MainPageActivity.getWeiboItem(weiboId);
                    if (followWeiboItemData==null){
                        Toast.makeText(v.getContext(),"Error",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (followWeiboItemData.getWeiboUserData().isFollowed()){
                        //如果已经关注了此人
                        Toast.makeText(v.getContext(), R.string.unfollow_success,Toast.LENGTH_SHORT).show();
                    }else {
                        //如果没有关注此人
                        Toast.makeText(v.getContext(), R.string.follow_success,Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            Log.d(TAG,"weibo id = "+weiboId+" clicked");
        }
    }
}
