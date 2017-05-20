package me.apqx.pocketweibo;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import me.apqx.pocketweibo.view.SwipeActivityHelper;
import me.apqx.pocketweibo.view.SwipeActivityLayout;

/**
 * Created by apqx on 2017/5/17.
 */

public class TempActivity extends Activity{
    private SwipeActivityHelper swipeActivityHelper;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_temp);
        swipeActivityHelper=new SwipeActivityHelper(this);
        swipeActivityHelper.onActivityCreate();

        SimpleDraweeView simpleDraweeView=(SimpleDraweeView)findViewById(R.id.fresco);

        DraweeController draweeController= Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse("http://wx2.sinaimg.cn/mw690/92e8647aly1ffpefmj9i7g20b406yx6q.gif"))
                .setAutoPlayAnimations(true)
                .build();
        simpleDraweeView.setController(draweeController);
    }


}
