package me.apqx.pocketweibo.customView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.facebook.drawee.generic.GenericDraweeHierarchy;

import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by apqx on 2017/6/15.
 * 在PhotoDraweeView的基础上修改了点击事件处理方式
 */

public class MyPhotoDraweeView extends PhotoDraweeView {
    private GestureDetector gestureDetector;
    private OnClickListener onClickListener;
    public MyPhotoDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
        inite();
    }

    public MyPhotoDraweeView(Context context) {
        super(context);
        inite();
    }

    public MyPhotoDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inite();
    }
    private void inite(){
        gestureDetector=new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
//                Log.d("apqx","onDown");
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
//                Log.d("apqx","onDown");

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
//                Log.d("apqx","onShowPress");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                Log.d("apqx","onShowPress");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
//                Log.d("apqx","onShowPress");

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        gestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (onClickListener!=null){
                    onClickListener.onClick(MyPhotoDraweeView.this);
                }
//                Log.d("apqx","onSingleTapConfirmed");
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
//                Log.d("apqx","onDoubleTap");
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
//                Log.d("apqx","onDoubleTapEvent");
                return false;
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.onClickListener=l;
    }
}
