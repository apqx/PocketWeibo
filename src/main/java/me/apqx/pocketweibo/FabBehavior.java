package me.apqx.pocketweibo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by apqx on 2017/5/17.
 */

public class FabBehavior extends FloatingActionButton.Behavior {
    private final String TAG=this.getClass().getSimpleName();

    private boolean isAniming;
    private boolean isOut;
    public FabBehavior() {
        super();
    }

    public FabBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return super.layoutDependsOn(parent, child, dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes== ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//        Log.d(TAG,"y consumed "+dyConsumed);
        if (!isAniming){
            if (dyConsumed>0&&!isOut){
                //RecyclerView向上滚动时，fab应该消失
                ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(child,"translationY",300);
                objectAnimator.setDuration(500);
                objectAnimator.addListener(new AnimListener(AnimListener.TYPE_SLID_OUT));
                objectAnimator.start();
            }else if (dyConsumed<0&&isOut){
                ObjectAnimator objectAnimator=ObjectAnimator.ofFloat(child,"translationY",0);
                objectAnimator.setDuration(500);
                objectAnimator.addListener(new AnimListener(AnimListener.TYPE_SLID_IN));
                objectAnimator.start();
            }
        }
    }

    private class AnimListener implements Animator.AnimatorListener{
        private static final int TYPE_SLID_IN=0;
        private static final int TYPE_SLID_OUT=1;
        private int type;
        private AnimListener(int type){
            this.type=type;
        }
        @Override
        public void onAnimationStart(Animator animation) {
            isAniming=true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            isAniming=false;
            if (type==TYPE_SLID_IN){
                isOut=false;
            }else {
                isOut=true;
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

}
