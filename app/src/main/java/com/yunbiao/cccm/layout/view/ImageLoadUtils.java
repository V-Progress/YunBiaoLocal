package com.yunbiao.cccm.layout.view;

import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.common.ResourceConst;

import java.io.File;

/**
 * Created by LiuShao on 2016/7/7.
 * 图片加载的统一初始化类，方便框架切换代码的处理问题
 */
public class ImageLoadUtils {

    public static ImageLoadUtils getImageLoadUtils() {
        return ImageLoadHoler.sInstance;
    }

    /**
     * 静态内部类
     */
    private static class ImageLoadHoler {
        private static final ImageLoadUtils sInstance = new ImageLoadUtils();
    }

    /*初始化图片加载配置,当前使用universial_imageloader图片加载框架*/
    public void initImageLoadConfig() {
        initImageLoader();
    }

    private void initImageLoader() {
        File file = new File(ResourceConst.LOCAL_RES.IMAGE_CACHE_PATH);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(APP.getContext().getApplicationContext())
                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiskCache(file))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(APP.getContext(), 5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
//                      .writeDebugLogs() // Remove for release app
                .build();//开始构建
        ImageLoader.getInstance().init(config);
    }

    private DisplayImageOptions options;

    private DisplayImageOptions getImageOption() {
        if (options == null) {
            options = new DisplayImageOptions.Builder()
                    .showImageForEmptyUri(R.mipmap.wei_image_faile)//设置图片Uri为空或是错误的时候显示的图片
                    .cacheInMemory(false)//设置下载的图片是否缓存在内存中
                    .cacheOnDisc(false)//设置下载的图片是否缓存在SD卡中
                    .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new SimpleBitmapDisplayer())
//                  .displayer(new FadeInBitmapDisplayer(100))//是否图片加载好后渐入的动画时间
                    .imageScaleType(ImageScaleType.NONE_SAFE)
                    .build();//构建完成
        }
        return options;
    }

    /*加载内部发存储图片的统一方法*/
    public void loadLocalImage(String path, ImageView imageView) {
        ImageLoader.getInstance().displayImage("file:///" + path, imageView, getImageOption());
    }

    private Animation getImageLoadAnimation(String animationId) {
        int playType = Integer.valueOf(animationId);
//        Log.e("animation.playType", "playType:" + playType);
        Animation imagePlayType = null;
        if (playType > 0) {
            imagePlayType = AnimationUtils.loadAnimation(APP.getContext(), AnimationInterface.imagePlayList[playType - 1]);
        }
        return imagePlayType;
    }

    /*加载内部发存储图片的统一方法*/
    public void loadLocalImage(final String path, final ImageView imageView, final View backView, final String animationId) {
        ImageLoader.getInstance().displayImage("file:///" + path, imageView, getImageOption(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                Animation animation = getImageLoadAnimation(animationId);
                if (animation != null) {
                    view.setAnimation(animation);
                    view.startAnimation(animation);
//                    animation.start();
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (backView != null) {
                                backView.setBackgroundDrawable(imageView.getDrawable());
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                } else {
                    try {
                        backView.setBackgroundDrawable(imageView.getDrawable());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /*配置加载网络图片的配置*/
    public void loadNetImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, new DisplayImageOptions.Builder()
                .cacheInMemory(false)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .showImageOnFail(R.mipmap.erwei_card)
                .displayer(new FadeInBitmapDisplayer(300))//是否图片加载好后渐入的动画时间
                .imageScaleType(ImageScaleType.NONE_SAFE)
                .build());
    }

    /*配置二维码加载的配置*/
    public void loadWeiChatErCode(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, new DisplayImageOptions.Builder()
                .cacheInMemory(false)//设置下载的图片是否缓存在内存中
                .cacheOnDisc(true)//设置下载的图片是否缓存在SD卡中
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .showImageOnFail(R.mipmap.wei_chat_no_connon)
                .displayer(new FadeInBitmapDisplayer(300))//是否图片加载好后渐入的动画时间
                .imageScaleType(ImageScaleType.NONE_SAFE).build());
    }
}
