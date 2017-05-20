package me.apqx.pocketweibo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import me.apqx.pocketweibo.struct.CommentData;
import me.apqx.pocketweibo.struct.UserData;

/**
 * Created by apqx on 2017/5/14.
 * 为评论设置的适配器
 */

public class CommentItemRecyclerAdapter extends RecyclerView.Adapter<CommentItemRecyclerAdapter.ViewHolder>{
    private int resource;
    private List<CommentData> list;

    public CommentItemRecyclerAdapter(int resource,List<CommentData> list) {
        super();
        this.resource=resource;
        this.list=list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(resource,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CommentData commentData=list.get(position);
        UserData userData=commentData.getUserData();
        holder.imageView_head.setImageURI(userData.getUserHeadPicURL());
        holder.textView_username.setText(userData.getUserName());
        holder.textView_commentTime.setText(commentData.getCommentTime());
        holder.textView_device.setText(commentData.getDevice());
        holder.textView_commnet.setText(commentData.getComment());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private SimpleDraweeView imageView_head;
        private TextView textView_username;
        private TextView textView_commentTime;
        private TextView textView_device;
        private TextView textView_commnet;
        private ImageButton btn_expand;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView_head=(SimpleDraweeView)itemView.findViewById(R.id.imageView_comment_item_head);
            textView_username=(TextView)itemView.findViewById(R.id.textView_comment_item_name);
            textView_commentTime=(TextView)itemView.findViewById(R.id.textView_comment_item_time);
            textView_device=(TextView)itemView.findViewById(R.id.textView_comment_item_device);
            textView_commnet=(TextView)itemView.findViewById(R.id.textView_comment_item_content);
            btn_expand=(ImageButton)itemView.findViewById(R.id.btn_comment_item_expand);
        }
    }

    private class ExpandOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_comment_item_expand:
                    //弹出窗口
                    break;
            }
        }
    }
}
