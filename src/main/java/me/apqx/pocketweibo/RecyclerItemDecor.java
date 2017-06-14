package me.apqx.pocketweibo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by apqx on 2017/5/14.
 * 用来给RecyclerView的item之间绘制分割线
 */

public class RecyclerItemDecor extends RecyclerView.ItemDecoration{
    private int strokeWidth;
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(MyApplication.getContext().getResources().getColor(R.color.cardview_shadow_start_color));
        strokeWidth= 1;
        paint.setStrokeWidth(strokeWidth);
        int left=parent.getPaddingLeft();
        int right=parent.getWidth()-parent.getPaddingRight();
        for(int i=0;i<parent.getChildCount();i++){
            View itemView=parent.getChildAt(i);
            int top=itemView.getBottom();
            int bottom=top+strokeWidth;
            c.drawLine(left,top,right,bottom,paint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0,0,0,strokeWidth);
    }
}
