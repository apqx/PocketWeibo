package me.apqx.pocketweibo.customView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by apqx on 2017/5/17.
 */

public class SwipeActivityLayout extends LinearLayout {
    private final String TAG=this.getClass().getSimpleName();
    private Scroller scroller;
    private Activity activity;
    private int width;
    private View parent;
    private OnFinishActivity onFinishActivity;
    public SwipeActivityLayout(Context context) {
        super(context);
        init();
    }

    public SwipeActivityLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeActivityLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        scroller=new Scroller(getContext());
        post(new Runnable() {
            @Override
            public void run() {
                width=getMeasuredWidth();
                parent=(View)getParent();
            }
        });
    }

    public void onAttachActivity(Activity activity){
        this.activity=activity;

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);
        decorView.removeView(decorChild);

        addView(decorChild);
        decorView.addView(this);
    }

    int x,y;
    int lastX,lastY;
    int offsetX,offsetY;
    int count=0;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        x=(int)ev.getRawX();
        y=(int)ev.getRawY();
        boolean result=false;
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                count=0;
                lastX=x;
                lastY=y;
                if (!scroller.isFinished()){
                    scroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                offsetX=x-lastX;
                offsetY=y-lastY;
                if (Math.abs(offsetX)>Math.abs(offsetY)*2){
                    count++;
                    if (count>3){
                        result=true;
                        count=0;
                    }
                }
                lastX=x;
                lastY=y;
                break;
        }
        return result;
    }
    private boolean shouldFinishActivity;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        VelocityTracker velocityTracker=VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(1000);
        x=(int)event.getRawX();
        y=(int)event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                float vx=velocityTracker.getXVelocity();
                float vy=velocityTracker.getYVelocity();
                if (Math.abs(vx)>2000){
                    //只要曾经速度超过这个值，就应该finish activity
                    shouldFinishActivity=true;
                }
                offsetX=x-lastX;
                offsetY=y-lastY;
                if (parent.getScrollX()>=0&&offsetX<0){

                }else {

                    parent.scrollBy(-offsetX,0);

                }
                //同时，在这里应该跟变背景的透明度,无法调整DecorView的背景透明度

                lastX=x;
                lastY=y;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(parent.getScrollX())>width/2||shouldFinishActivity){
                    //滑动到右边并结束Activity
                    scroller.startScroll(parent.getScrollX(),0,-width-parent.getScrollX(),0,500);
//                    Log.d(TAG,"onTouchEvent actionUp 2");
                }else {
                    //滑动回原点
                    scroller.startScroll(parent.getScrollX(),0,-parent.getScrollX(),0,500);
//                    Log.d(TAG,"onTouchEvent actionUp 1 "+parent.getScrollX());
                }
                invalidate();
                shouldFinishActivity=false;
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()){
            parent.scrollTo(scroller.getCurrX(),scroller.getCurrY());
            if (-parent.getScrollX()>=width&&onFinishActivity!=null){
                onFinishActivity.finishActivity();

            }
            invalidate();

        }
    }

    public void setOnFinishActivity(OnFinishActivity onFinishActivity){
        this.onFinishActivity=onFinishActivity;
    }

    public interface OnFinishActivity{
        void finishActivity();
    }

    //动态计算颜色的ARGB中的A,传入百分比，0表示全透明，1表示不透明
    private int getBackColor(float percent){
        int color=Color.argb((int)(percent*0x90),0,0,0);
        return color;
    }
}
