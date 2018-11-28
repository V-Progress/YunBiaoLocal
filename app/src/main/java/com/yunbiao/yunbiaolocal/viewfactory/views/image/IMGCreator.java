package com.yunbiao.yunbiaolocal.viewfactory.views.image;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layouthandle.LayoutRefresher;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutInfo;
import com.yunbiao.yunbiaolocal.viewfactory.bean.LayoutPosition;
import com.yunbiao.yunbiaolocal.viewfactory.tool.LayoutJsonTool;

import java.io.File;

/**
 * Created by Administrator on 2018/11/28.
 */

public class IMGCreator {


    private static Bitmap readBitmapAutoSize(String filePath, int outWidth, int outHeight) {
        // outWidth和outHeight是目标图片的最大宽度和高度，用作限制
        Bitmap bitmap = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                BitmapFactory.Options options = setBitmapOption(filePath, outWidth, outHeight);
                bitmap = BitmapFactory.decodeFile(file.getPath(), options);
            }
        } catch (OutOfMemoryError | Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static BitmapFactory.Options setBitmapOption(String file, int width, int height) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        // 设置只是解码图片的边距，此操作目的是度量图片的实际宽度和高度
        BitmapFactory.decodeFile(file, opt);
        int outWidth = opt.outWidth; // 获得图片的实际高和宽
        int outHeight = opt.outHeight;
        opt.inDither = false;
        opt.inPreferredConfig = Bitmap.Config.ARGB_4444;
        // 设置加载图片的颜色数为16bitRGB_565，默认是RGB_8888，表示24bit颜色和透明通道，但一般用不上
        opt.inSampleSize = 1;
        // 设置缩放比,1表示原比例，2表示原来的四分之一....
        // 计算缩放比
        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize = (outWidth / width + outHeight / height) / 2;
            opt.inSampleSize = sampleSize;
        }
        opt.inJustDecodeBounds = false;// 最后把标志复原
        return opt;
    }


    public static View createImgOrBackgroundView(Context context, LayoutInfo layoutInfo, WindowManager wm) {
        String[] backgroundArray = layoutInfo.getContent();
        if (backgroundArray.length != 0) {
            LinearLayout linearLayout;
            linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.MATCH_PARENT));
            if (backgroundArray[0].startsWith("#")) {
                linearLayout.setBackgroundColor(Color.parseColor(backgroundArray[0]));
            } else if (backgroundArray[0].endsWith(".jpg") || backgroundArray[0].endsWith(".png")) {
//                String path = ResourceUpdate.RESOURSE_PATH + ResourceUpdate.IMAGE_CACHE_PATH + backgroundArray[0];
                String path = "http://i2.hdslb.com/video/f6/f633820b7898e6b5dba3fe0349e3d945.jpg";//// TODO: 2018/11/28
                if (new File(path + "_ok").exists()) {//如果存在ok文件就添加到播放列表中
                    Drawable drawable = Drawable.createFromPath(path);
                    linearLayout.setBackground(drawable);
                } else {
                    linearLayout = (LinearLayout) createTempView(layoutInfo, wm, R.mipmap.no_resourse, null);
                }
            }
            return linearLayout;
        }
        return null;
    }


    public static View createTempView(LayoutInfo layoutInfo, WindowManager wm, int resourseId, String str) {
        LayoutPosition lp = LayoutJsonTool.getViewPostion(layoutInfo, wm);
        AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(lp.getWidth(), lp.getHeight(), lp.getLeft(),
                lp.getTop());
        LinearLayout linearLayout = new LinearLayout(APP.getContext());
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        ImageView image_view = new ImageView(APP.getContext());
        TextView textView = new TextView(APP.getContext());
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT);

        if (TextUtils.isEmpty(str)) {
            image_view.setImageResource(resourseId);
            image_view.setLayoutParams(layoutParams1);
            linearLayout.addView(image_view);
        } else {
            Drawable drawable = APP.getContext().getResources().getDrawable(R.mipmap.hints);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            textView.setCompoundDrawables(drawable, null, null, null);//分别对应 左上右下
            textView.setCompoundDrawablePadding(10);//设置图片和text之间的间距
            textView.setText(str);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(30);
            textView.setLayoutParams(layoutParams1);
            linearLayout.addView(textView);
        }

        return linearLayout;
    }
}
