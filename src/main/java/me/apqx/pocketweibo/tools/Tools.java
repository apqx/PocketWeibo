package me.apqx.pocketweibo.tools;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import me.apqx.pocketweibo.MyApplication;
import me.apqx.pocketweibo.R;
import me.apqx.pocketweibo.struct.WeiboItemData;

/**
 * Created by apqx on 2017/5/3.
 */

public class Tools {
    private static final String TAG="Tools";
    public static void closeStream(Object object){
        InputStream inputStream=null;
        OutputStream outputStream=null;
        Reader reader=null;
        Writer writer=null;
        if (object==null){
            return;
        }
        if (object instanceof InputStream){
            inputStream=(InputStream)object;
        }else if (object instanceof OutputStream){
            outputStream=(OutputStream)object;
        }else if (object instanceof Reader){
            reader=(Reader)object;
        }else if (object instanceof Writer){
            writer=(Writer)object;
        }
        try {
            if (inputStream!=null){
                inputStream.close();
//                Log.d(TAG,"InputStream close");
            }
            if (outputStream!=null){
                outputStream.close();
//                Log.d(TAG,"OutputStream close");
            }
            if (reader!=null){
                reader.close();
//                Log.d(TAG,"Reader close");
            }
            if (writer!=null){
                writer.close();
//                Log.d(TAG,"Writer close");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


}
