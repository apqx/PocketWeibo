package me.apqx.pocketweibo.customView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.view.UserDataActivity;

/**
 * Created by apqx on 2017/5/5.
 * 一个自定义的TextView，可以自动识别指定格式的字符并拦截点击事件
 */

public class LinkTextView extends AppCompatTextView {
    private static final String TAG="LinkTextView";
    private boolean shouldInterruptClick;

    public LinkTextView(Context context) {
        super(context);
    }

    public LinkTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LinkTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
//        text="[赞]//@天上-的蝎:胡适之，李叔同，张爱玲，http://m.weibo.cn/5044281310/4103430798429973 齐白石，张大千，梁漱溟 //@张晨初艺术空间,:是的，其他呢？[good]//@温温122365:第二张 弘一大师#中国历史#...全文： http://m.weibo.cn/5044281310/4103430798429973 ​";
        SpannableString spannableString=new SpannableString(text);
        //识别省略的链接 全文： http:\/\/m.weibo.cn\/5044281310\/4103430798429973 ​
        Matcher matcherEndLink=Pattern.compile("(全文\\W+)(http[^,，。?\\n@]+)[\\s\\n]*").matcher(text);
        if (matcherEndLink.find()){
            String string=getContext().getString(R.string.linkTextView_more);
            Log.d(TAG,"end link "+matcherEndLink.group());
            String url=matcherEndLink.group(2);
            int start=matcherEndLink.start(1);
            text=matcherEndLink.replaceAll(string);
            Log.d(TAG,text.toString());
            spannableString=new SpannableString(text);
            spannableString.setSpan(new MyClickableSpan(url, MyClickableSpan.TYPE_END_LINK),start,start+string.length(),Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        }
        //识别内容中的链接 http:\/\/m.weibo.cn\/5044281310\/4103430798429973
        Matcher matcherInnerLink=Pattern.compile("(http://([\\w\\d]*\\.*[\\w\\d]*/*)+)[\\s（）()，。,？\\n]*").matcher(spannableString);
        while (matcherInnerLink.find()){
            Log.d(TAG,"inner link "+matcherInnerLink.group(1));
            spannableString.setSpan(new MyClickableSpan(matcherInnerLink.group(1), MyClickableSpan.TYPE_INNER_LINK),matcherInnerLink.start(),matcherInnerLink.start()+matcherInnerLink.group(1).length(),Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        //识别用户名 @username:你好    @username 你好    @username,你好
        Matcher matcherUsername=Pattern.compile("@([^@:\\s,.，。?\\n]+)[:\\s,.，。?\\n]*").matcher(spannableString);
        while (matcherUsername.find()){
            spannableString.setSpan(new MyClickableSpan(matcherUsername.group(1), MyClickableSpan.TYPE_USERNAME),matcherUsername.start(1)-1,matcherUsername.start(1)+matcherUsername.group(1).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            Log.d(TAG,"username "+matcherUsername.group(1));
        }
        //识别话题 #topic#
        Matcher matcherTopic=Pattern.compile("#(\\w+)#").matcher(spannableString);
        while (matcherTopic.find()){
            spannableString.setSpan(new MyClickableSpan(matcherTopic.group(1), MyClickableSpan.TYPE_TOPIC),matcherTopic.start(1)-1,matcherTopic.start(1)+matcherTopic.group(1).length()+1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            Log.d(TAG,"topic "+matcherTopic.group(1));
        }
        setMovementMethod(LinkMovementMethod.getInstance());
        super.setText(spannableString,type);
    }
    class MyClickableSpan extends ClickableSpan{
        private static final int TYPE_USERNAME=0;
        private static final int TYPE_TOPIC=1;
        private static final int TYPE_END_LINK=2;
        private static final int TYPE_INNER_LINK=3;
        private String string;
        private int type;
        private MyClickableSpan(String string, int type) {
            super();
            this.string=string;
            this.type=type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            //设置链接样式
            ds.setColor(LinkTextView.this.getResources().getColor(R.color.light_colorPrimary));
            ds.setUnderlineText(false);
        }

        @Override
        public void onClick(View widget) {
            Intent intent;
            switch (type){
                case TYPE_USERNAME:
                    intent=new Intent(getContext(), UserDataActivity.class);
                    intent.putExtra("apqx",string);
                    getContext().startActivity(intent);
                    break;
                case TYPE_INNER_LINK:
                    //打开浏览器
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(Color.parseColor("#1ABC9C"));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(getContext(), Uri.parse(string));
                    break;
                case TYPE_END_LINK:
                    //当用户点击了更多时，应该解析页面，提取出微博的完整信息
                    break;
                case TYPE_TOPIC:
                    break;
            }
            //点击链接后的行为
//            Tools.showToast(string+" "+type);
            shouldInterruptClick=true;
        }
    }
    public boolean shouldInterruptClick(){
        return shouldInterruptClick;
    }
    public void donotInterruptClick(){
        shouldInterruptClick=false;
    }


}
