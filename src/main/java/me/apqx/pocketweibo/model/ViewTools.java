package me.apqx.pocketweibo.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.util.Log;
import android.widget.Toast;

import me.apqx.pocketweibo.MyApplication;

/**
 * Created by apqx on 2017/4/19.
 * 工具类
 */

public class ViewTools {
    private static Toast toast=Toast.makeText(MyApplication.getContext(),"",Toast.LENGTH_SHORT);
    public static void showToast(String string){
        toast.setText(string);
        toast.show();
    }
    public static void showToast(int resId){
        toast.setText(resId);
        toast.show();
    }
    /**
     * 将给定的Bitmap转化为一个圆形Bitmap
     * @param bitmap 指定的bitmap
     * @return 转化后的圆形Bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap){
        if (bitmap==null){
            Log.d("apqx","getCircleBitmap_bitmap=null");
            return bitmap;
        }
        Bitmap mBitmap=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(mBitmap);
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        int centerX=canvas.getWidth()/2;
        int centerY=canvas.getHeight()/2;
        canvas.drawCircle(centerX,centerY,Math.min(centerX,centerY),paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap,0,0,paint);
        return mBitmap;
    }

    /**
     * 以给定的尺寸加载缩放后的图片
     * @param resource 资源
     * @param id id
     * @param reqWidth 要求的宽度
     * @param reqHeight 要求的高度
     * @return 缩放后的图片
     */
    public static Bitmap decodeBitmapFromResource(Resources resource, int id, int reqWidth, int reqHeight){
        //创建Options对象
        BitmapFactory.Options options=new BitmapFactory.Options();
        //表示只是获得图片的属性信息，不会真正加载图片，操作是轻量级的
        options.inJustDecodeBounds=true;
        //不会真正加载图片，只是获得图片属性
        BitmapFactory.decodeResource(resource,id,options);
        //计算采样率
        options.inSampleSize=calculateInSampleSize(options,reqWidth,reqHeight);
        //真正加载图片
        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeResource(resource,id,options);
    }
    //计算采样率
    private static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        //获取图片的长宽
        int height=options.outHeight;
        int width=options.outWidth;
        int inSampleSize=1;
        if(height>reqHeight||width>reqWidth){
            int halfHeight=height/2;
            int halfWidth=width/2;
            while((halfWidth/inSampleSize>=reqWidth) && (halfHeight/inSampleSize>=reqHeight)){
                inSampleSize *=2;
            }
        }
        return inSampleSize;
    }

    /**
     * 将px转换为dp或dip
     */
    public static int pxToDp(Context context, float pxValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }
    /**
     * 将dp或dip转换为px
     */
    public static int dpToPx(Context context,float dpValue){
        final float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
    /**
     * 将px转换为sp
     */
    public static int pxToSp(Context context,float pxValue){
        final float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(pxValue/fontScale+0.5f);
    }
    /**
     * 将sp转换为px
     */
    public static int spToPx(Context context,float dpValue){
        final float fontScale=context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(dpValue*fontScale+0.5f);
    }

}
