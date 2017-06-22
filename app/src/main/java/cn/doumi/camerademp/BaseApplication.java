package cn.doumi.camerademp;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by Administrator on 2017/6/21 0021.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /*一种7.0打开相机出现的错误：
         android.os.FileUriExposedException:
         file:///storage/emulated/0/aaaa/1498030554707.jpg
         exposed beyond app through ClipData.Item.getUri()*/
        //这里打开主要是因为裁剪也需要用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }
}
