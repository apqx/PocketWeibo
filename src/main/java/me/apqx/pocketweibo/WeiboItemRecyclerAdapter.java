package me.apqx.pocketweibo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.util.ArrayList;
import java.util.List;

import me.apqx.pocketweibo.struct.PicUrls;
import me.apqx.pocketweibo.struct.UserData;
import me.apqx.pocketweibo.struct.WeiboItemData;
import me.apqx.pocketweibo.tools.Tools;
import me.apqx.pocketweibo.tools.ViewTools;
import me.apqx.pocketweibo.tools.WebTools;
import me.apqx.pocketweibo.view.LinkTextView;
import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by apqx on 2017/5/4.
 * 主页面的RecyclerAdapter
 */

public class WeiboItemRecyclerAdapter extends RecyclerView.Adapter<WeiboItemRecyclerAdapter.MyViewHolder> {
    private static final String TAG="WeiboItemAdapter";
    static final int WEIBO_MAINPAGE_LIST=0;
    static final int WEIBO_DETAIL=1;
    static final int WEIBO_USER_PAGE=2;
    static final int ITEM_NORMAL=3;
    static final int ITEM_FOOTER=4;

    private List<WeiboItemData> list;
    private int resource;
    private int weiboType;
    private PopupWindow popupWindow;

    private TextView textView_SavePost;
    private TextView textView_UnFollow;
    private ImageButton btnSavePost;
    private ImageButton btnUnFollow;
    private LinearLayout linearLayoutSavePost;
    private LinearLayout linearLayoutUnFollow;
    private Handler handler;
    private Activity activity;

    private MyOnClickListener onClickListener;
    private OnRefreshDownListener refreshDownListener;

    public WeiboItemRecyclerAdapter(List<WeiboItemData> list, int resource,int weiboType,Activity activity) {
        super();
        this.list=list;
        this.resource=resource;
        this.weiboType=weiboType;
        handler=new Handler();
        this.activity=activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder myViewHolder;
        if (viewType==ITEM_NORMAL){
            View view= LayoutInflater.from(parent.getContext()).inflate(resource,parent,false);
            myViewHolder=new MyViewHolder(view,ITEM_NORMAL);
            return myViewHolder;
        }else {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_weibo_footer,null);
            myViewHolder=new MyViewHolder(view,ITEM_FOOTER);
            return myViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (position==getItemCount()-1){
            //说明是Footer
            if (refreshDownListener!=null){
                refreshDownListener.refreshDown();
                Log.d(TAG,"Refresh Down");
            }
            if (weiboType==WEIBO_DETAIL||weiboType==WEIBO_USER_PAGE){
                holder.progressBar.setVisibility(View.GONE);
            }
            return;
        }

        WeiboItemData weiboItemData=list.get(position);
        UserData userData=weiboItemData.getWeiboUserData();
        holder.textView_time.setText(weiboItemData.getCreateTime());
        holder.textView_device.setText(weiboItemData.getDevice());
        holder.textView_name.setText(userData.getUserName());
        holder.textView_content.setText(weiboItemData.getContent());
        holder.draweeView_head.setImageURI(userData.getUserHeadPicURL());
        if (weiboType==WEIBO_MAINPAGE_LIST){
            //如果跳转到微博详细页面，应该隐藏这些信息，因为有取代的显示方式
            holder.textView_likeCount.setText(weiboItemData.getLikeCount());
            holder.textView_rePostCount.setText(weiboItemData.getRePostCount());
            holder.textView_commentCount.setText(weiboItemData.getCommentCount());
        }

        Log.d(TAG,userData.getUserName()+" pic head url "+userData.getUserHeadPicURL());
        Log.d(TAG,userData.getUserName()+" pic bg url "+userData.getProfileBGUrl());

        //把微博ID保存在View里,这样当点击列表Item的时候可以明确的知道点击的是哪个微博
        holder.textView_content.setTag(weiboItemData.getWeiboId());
        holder.btnExpand.setTag(weiboItemData.getWeiboId());
        //把用户名保存在View里，这样当用户点击名字或头像时可以知道点击的具体用户
        holder.draweeView_head.setTag(userData.getUserName());
        holder.textView_name.setTag(userData.getUserName());
        //对有图片的微博显示图片
        if (weiboItemData.hasPics()){
            holder.gridLayout_pic.setVisibility(View.VISIBLE);
            holder.gridLayout_pic.setTag(weiboItemData.getWeiboId());
            Log.d(TAG,userData.getUserName()+" has pic nem = "+weiboItemData.getPicUrls().getImageCount());
            //问题是这里获得的width老是0,说明到这里还没有加载完成
            setGridImage(weiboItemData.getPicUrls(),holder.gridLayout_pic,weiboItemData.getWeiboId());
        }else {
            holder.gridLayout_pic.setVisibility(View.GONE);
        }

        if (weiboItemData.hasReTwitter()){
            holder.relativeLayout_reTwitterMain.setVisibility(View.VISIBLE);
            WeiboItemData reTwitterWeibo=weiboItemData.getReTwitterWeibo();
            UserData reTwitterUserData=reTwitterWeibo.getWeiboUserData();
            holder.textView_reTwitter_state.setText(getReTwitterState(reTwitterWeibo.getRePostCount(),reTwitterWeibo.getCommentCount(),reTwitterWeibo.getLikeCount()));
            holder.textView_reTwitter_content.setText("@"+reTwitterUserData.getUserName()+": "+reTwitterWeibo.getContent());
            //把被转发的微博ID保存在View里
            holder.textView_reTwitter_content.setTag(weiboItemData.getWeiboId());
            if (reTwitterWeibo.hasPics()){
                holder.gridLayout_reTwitter_pic.setVisibility(View.VISIBLE);
                Log.d(TAG,"ReTwitter "+reTwitterUserData.getUserName()+" has pic nem = "+reTwitterWeibo.getPicUrls().getImageCount());
                holder.gridLayout_reTwitter_pic.setTag(reTwitterWeibo.getWeiboId());
                setGridImage(reTwitterWeibo.getPicUrls(),holder.gridLayout_reTwitter_pic,reTwitterWeibo.getWeiboId());
            }else {
                holder.gridLayout_reTwitter_pic.setVisibility(View.GONE);
            }
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
        return list.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position==getItemCount()-1){
            return ITEM_FOOTER;
        }
        return ITEM_NORMAL;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private SimpleDraweeView draweeView_head;
        private GridLayout gridLayout_pic;
        private GridLayout gridLayout_reTwitter_pic;
        private TextView textView_name;
        private TextView textView_time;
        private TextView textView_device;
        private TextView textView_content;
        private TextView textView_reTwitter_content;
        private TextView textView_reTwitter_state;
        private TextView textView_likeCount;
        private TextView textView_commentCount;
        private TextView textView_rePostCount;
        private RelativeLayout relativeLayout_reTwitterMain;
        private ImageButton btnExpand;
        private int itemType;

        private ProgressBar progressBar;
        public MyViewHolder(View itemView,int type) {
            super(itemView);
            this.itemType=type;
            if (type==ITEM_NORMAL){
                draweeView_head =(SimpleDraweeView)itemView.findViewById(R.id.simpleDraweeView_main_item_head);
                gridLayout_pic =(GridLayout) itemView.findViewById(R.id.gridLayout_main_item_image);
                gridLayout_reTwitter_pic =(GridLayout) itemView.findViewById(R.id.gridLayout_main_item_reTwitter_image);
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
                textView_name.setOnClickListener(onClickListener);
                draweeView_head.setOnClickListener(onClickListener);
            }else {
                //说明是Footer，这里加载另一个布局
                progressBar=(ProgressBar)itemView.findViewById(R.id.progressbar);
            }
        }
    }
    //弹出窗口显示对微博的操作选项，收藏，关注
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
            Tools.showToast("Error");
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
        OnExpandClickListener listener=new OnExpandClickListener(weiboItemData);
        linearLayoutSavePost.setOnClickListener(listener);
        linearLayoutUnFollow.setOnClickListener(listener);
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
    //微博中有图片，根据图片的多少自动调整GridLayout的布局并动态加载图片
    private void setGridImage(PicUrls picUrls, GridLayout gridLayout,String weiboId){
        gridLayout.removeAllViews();
        int count=picUrls.getImageCount();
        int width;
        int screenWidthDp=activity.getResources().getConfiguration().screenWidthDp;

        if (isPad()||activity.getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE){
            width=ViewTools.dpToPx(activity,screenWidthDp/2-20);
        }else {
            width=ViewTools.dpToPx(activity,screenWidthDp-20);
        }
        int imageWidth;
        int margin=5;
//        Log.d(TAG,"GridLayout width = "+width);
        //最多支持9张图片
        if (count<=3){
            imageWidth=width/count-2*margin;
        }else {
            imageWidth=width/3-2*margin;
        }
        for (int i=0;i<count;i++){
            GridLayout.Spec row;
            GridLayout.Spec col;
            //获取每一个子View所在的行和列
            if (count<=3){
                row=GridLayout.spec(1);
                col=GridLayout.spec(i);
            }else {
                row=GridLayout.spec(i/3);
                col=GridLayout.spec(i%3);
            }
            SimpleDraweeView simpleDraweeView=(SimpleDraweeView)LayoutInflater.from(activity).inflate(R.layout.layout_item_fresco_grid,null);
            GridLayout.LayoutParams layoutParams=new GridLayout.LayoutParams(row,col);
            //设置每个子View的尺寸
            layoutParams.width=imageWidth;
            layoutParams.height=imageWidth;
            //设置每个子View的margin
            layoutParams.setMargins(margin,margin,margin,margin);

            //这里进入了子线程下载图片
            //遗留图片加载错位问题需要解决

            simpleDraweeView.setOnClickListener(new GridItemClickListener(picUrls,i));
            DraweeController draweeController;
            String urlString=picUrls.getSmallImageUrlAt(i);
            if (count<3){
                draweeController=Fresco.newDraweeControllerBuilder()
                        .setUri(picUrls.getMiddleImageUrlAt(i))
                        .setAutoPlayAnimations(true)
                        .build();
            }else {
                if (urlString.substring(urlString.length()-3).equals("gif")){
                    draweeController=Fresco.newDraweeControllerBuilder()
                            .setUri(picUrls.getSmallImageUrlAt(i))
                            .setAutoPlayAnimations(true)
                            .build();
                }else{
                    draweeController=Fresco.newDraweeControllerBuilder()
                            .setUri(picUrls.getMiddleImageUrlAt(i))
                            .setAutoPlayAnimations(true)
                            .build();

                }

            }
            simpleDraweeView.setController(draweeController);
            gridLayout.addView(simpleDraweeView,layoutParams);
        }
    }
    //判断是不是平板电脑
    private boolean isPad() {
        return (activity.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
    private class MyOnClickListener implements View.OnClickListener{
        String weiboId;
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_main_item_expand:
                    weiboId = v.getTag().toString();
                    //此时应该弹出窗口
                    v.animate().setDuration(500).rotation(180).start();
                    showWeiboItemExpandWindow(v, weiboId);
                    break;
                case R.id.textView_main_item_content:
                    weiboId = v.getTag().toString();
                    LinkTextView linkTextView = (LinkTextView) v;
                    //当点击TextView内部的链接时，不应该再执行这里的点击事件
                    if (!linkTextView.shouldInterruptClick()) {
                        //这不是被转发的微博，所以在该微博的详细界面，点击不再跳转
                        if (weiboType == WEIBO_MAINPAGE_LIST) {
                            //跳转到微博详细界面，传输微博ID
                            Intent intent = new Intent(v.getContext(), WeiboDetailActivity.class);
                            intent.putExtra("apqx", weiboId);
                            v.getContext().startActivity(intent);
                        }
                    }
                    linkTextView.donotInterruptClick();
                    break;
                case R.id.textView_main_item_content_reTwitter:
                    weiboId = v.getTag().toString();
                    LinkTextView textView = (LinkTextView) v;
                    if (!textView.shouldInterruptClick()) {
                        //跳转到微博详细界面
                        Intent reIntent = new Intent(v.getContext(), WeiboDetailActivity.class);
                        reIntent.putExtra("reTwitter", true);
                        reIntent.putExtra("apqx", weiboId);
                        v.getContext().startActivity(reIntent);
                    }
                    textView.donotInterruptClick();
                    break;
                case R.id.textView_main_item_name:
                case R.id.simpleDraweeView_main_item_head:
                    String userName=v.getTag().toString();
                    Intent intent=new Intent(v.getContext(),UserDataActivity.class);
                    intent.putExtra("apqx",userName);
                    v.getContext().startActivity(intent);
                    break;
            }
            Log.d(TAG,"weibo id = "+weiboId+" clicked");
        }
    }
    //按钮点击展开
    private class OnExpandClickListener implements View.OnClickListener{
        private WeiboItemData weiboItemData;
        OnExpandClickListener(WeiboItemData weiboItemData){
            this.weiboItemData=weiboItemData;
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.linearLayout_savePost:
                    //因为点击此之前一定点击btn_main_item_expand，所以可以获得微博ID
                    //根据本地内存数据判断当前状态，联网请求改变状态，如果成功，则同时改变本地保存的状态
                    if (weiboItemData==null){
                        Tools.showToast("Error");
                        break;
                    }
                    if (weiboItemData.isFavorited()){
                        //如果已经被收藏了
                        Tools.showToast(R.string.remove_from_favorite);
                    }else {
                        //如果没有被收藏
                        Tools.showToast(R.string.add_to_favorite);
                    }
                    break;
                case R.id.linearLayout_unFollow:
                    if (weiboItemData==null){
                        Tools.showToast("Error");
                        break;
                    }
                    if (weiboItemData.getWeiboUserData().isFollowed()){
                        //如果已经关注了此人
                        Tools.showToast(R.string.unfollow_success);
                    }else {
                        //如果没有关注此人
                        Tools.showToast(R.string.follow_success);
                    }
                    break;

            }
        }
    }
    //图片网格点击监听器
    private class GridItemClickListener implements View.OnClickListener{
        private PicUrls picUrls;
        private int index;
        GridItemClickListener(PicUrls picUrls,int index){
            this.picUrls=picUrls;
            this.index=index;
        }
        @Override
        public void onClick(View v) {
            Log.d(TAG,"Grid click "+index);
            View view=LayoutInflater.from(activity).inflate(R.layout.layout_grid_pics_expand,null);
            final ViewPager viewPager=(ViewPager)view.findViewById(R.id.viewPage_grid_pic_expand);
            List<View> list=new ArrayList<View>();
            for (int i=0;i<picUrls.getImageCount();i++){
                final PhotoDraweeView mPhotoDraweeView=(PhotoDraweeView) LayoutInflater.from(activity).inflate(R.layout.layout_item_fresco_viewpage,null);
                PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
                controller.setUri(picUrls.getOriginalImageUrlAt(i));
                controller.setAutoPlayAnimations(true);
                controller.setOldController(mPhotoDraweeView.getController());
                controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imageInfo == null || mPhotoDraweeView == null) {
                            return;
                        }
                        mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                    }
                });
                mPhotoDraweeView.setController(controller.build());
                list.add(mPhotoDraweeView);
                mPhotoDraweeView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        String urlString=picUrls.getOriginalImageUrlAt(viewPager.getCurrentItem());
                        //在这里判断是否有存储权限，有的话直接下载当前图片，否则申请权限，回调方法确认授权后，开始下载
                        if (ContextCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                            Log.d(TAG,"has permission");
                            Constant.urlList.add(urlString);
                            WebTools.startDownLoadPics(handler);
                        }else {
                            Log.d(TAG,"no permission");
                            Constant.urlList.add(urlString);
                            ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
                        }
                        return false;
                    }
                });

            }
            PagerAdapter pagerAdapter=new GridPicPageAdapter(list);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setCurrentItem(index);
            AlertDialog.Builder builder=new AlertDialog.Builder(activity);
            builder.setView(view);
            final AlertDialog alertDialog=builder.create();
            alertDialog.show();
            alertDialog.setCanceledOnTouchOutside(true);
            viewPager.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                }
            });


        }
    }
    private class GridPicPageAdapter extends PagerAdapter{
        private List<View> list;
        public GridPicPageAdapter(List<View> list) {
            super();
            this.list=list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view=list.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view=list.get(position);
            container.removeView(view);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }
    }
    private boolean checktPermission(String permission){
        return ContextCompat.checkSelfPermission(activity,permission)== PackageManager.PERMISSION_GRANTED;
    }

    //对外暴露方法，当列表快要接近底部时刷新数据
    public void setOnRefreshDownListener(OnRefreshDownListener refreshDownListener){
        this.refreshDownListener=refreshDownListener;
    }

    interface OnRefreshDownListener{
        void refreshDown();

    }


}
