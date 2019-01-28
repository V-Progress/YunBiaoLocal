package com.yunbiao.cccm.common;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.utils.ImageUtil;
import com.yunbiao.cccm.utils.ThreadUtil;

import java.util.HashMap;
import java.util.Random;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.ui.widget.DanmakuView;

/**
 * Created by Administrator on 2019/1/23.
 */

public class DanmakuManager {
    private final String TAG = getClass().getSimpleName();

    private int DANMAKU_TYPE = BaseDanmaku.TYPE_SCROLL_RL;//弹幕滚动类型
    private int MAX_LINE_NUM = 5;//最大行数
    private int DANMAKU_STYLE = IDisplayer.DANMAKU_STYLE_STROKEN;//弹幕风格
    private int DANMAKU_STROKEN_WIDTH = 3;//描边弹幕宽度（由弹幕风格决定）
    private float DANMAKU_SCROLL_SPEED = 1.2f;//弹幕滚动速度
    private float DANMAKU_SCALE_TEXT_SIZE = 1.2f;//弹幕缩放大小
    private int DANMAKU_MARGIN = 40;//弹幕间距
    private boolean DANMAKU_MERGE_DUPLICATE = false;//合并重复弹幕

    //普通弹幕设定参数
    //弹幕尺寸（单位SP）
    private int DANMAKU_SIZE_SP = 20;
    //弹幕内间距
    private int DANMAKU_PADDING = 5;
    //弹幕颜色
    private int DANMAKU_COLOR = Color.WHITE;
    //弹幕边框颜色
    private int DANMAKU_BORDER_COLOR = Color.GREEN;
    //弹幕绘制缓存
    private boolean DANMAKU_DRAWING_CACHE = true;
    //弹幕显示优先级
    private byte DANMAKU_PRIORITY = 1;

    private static DanmakuManager instance;
    private Activity mActivity;
    private DanmakuView danmakuView;
    private DanmakuContext danmakuContext;

    //弹幕解析器
    private BaseDanmakuParser parser = new BaseDanmakuParser() {
        @Override
        protected IDanmakus parse() {
            return new Danmakus();
        }
    };

    //图文混排要设置此填充器
    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕

                if(danmakuView != null) {
                    danmakuView.invalidateDanmaku(danmaku, false);
                }


                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
               /* new Thread() {

                    @Override
                    public void run() {
                        String url = "http://www.bilibili.com/favicon.ico";
                        InputStream inputStream = null;
                        Drawable drawable = mDrawable;
                        if(drawable == null) {
                            try {
                                URLConnection urlConnection = new URL(url).openConnection();
                                inputStream = urlConnection.getInputStream();
                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
                                mDrawable = drawable;
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(inputStream);
                            }
                        }
                        if (drawable != null) {
                            drawable.setBounds(0, 0, 100, 100);
                            SpannableStringBuilder spannable = createSpannable(danmaku.text.toString(),drawable);
                            danmaku.text = spannable;
                            if(danmakuView != null) {
                                danmakuView.invalidateDanmaku(danmaku, false);
                            }
                            return;
                        }
                    }
                }.start();*/

            }
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
        }
    };

    public interface DanmakuReadyListener {
        void Ready();
    }

    public synchronized static DanmakuManager getInstance() {
        if (instance == null) {
            instance = new DanmakuManager();
        }
        return instance;
    }

    /***
     * 初始化弹幕
     * @param activity
     * @param dmv
     * @param danmakuReadyListener
     */
    public void init(Activity activity, final DanmakuView dmv, final DanmakuReadyListener danmakuReadyListener) {
        if(activity == null){
            throw new RuntimeException("activity can not be null");
        }
        mActivity = activity;

        if(dmv == null){
            throw new RuntimeException("danmakuView can not be null");
        }
        this.danmakuView = dmv;

        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(DANMAKU_TYPE, MAX_LINE_NUM); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        danmakuContext = DanmakuContext.create();
        danmakuContext.setDanmakuStyle(DANMAKU_STYLE, DANMAKU_STROKEN_WIDTH)
                .setDuplicateMergingEnabled(DANMAKU_MERGE_DUPLICATE)
                .setScrollSpeedFactor(DANMAKU_SCROLL_SPEED)
                .setScaleTextSize(DANMAKU_SCALE_TEXT_SIZE)
                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair)
                .setDanmakuMargin(DANMAKU_MARGIN);
        //        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
        danmakuView.setCallback(new DrawHandler.Callback() {
            @Override
            public void prepared() {
                danmakuView.start();
                if(danmakuReadyListener != null){
                    danmakuReadyListener.Ready();
                }
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {

            }

            @Override
            public void drawingFinished() {

            }
        });
        danmakuView.prepare(parser, danmakuContext);
//        danmakuView.showFPS(true);
        danmakuView.enableDanmakuDrawingCache(DANMAKU_DRAWING_CACHE);
    }

    public void open(){
        if(danmakuView != null){
            Log.e(TAG,"弹幕：开");
            danmakuView.prepare(parser, danmakuContext);
        }
    }

    public void close(){
        if(danmakuView != null && danmakuView.isPrepared()){
            Log.e(TAG,"弹幕：关");
            danmakuView.stop();
            danmakuView.clear();
            danmakuView.release();
        }
    }

    /***
     * 发送纯文字弹幕
     * @param text
     */
    public void addDanmaku(String text){
        Drawable drawable = null;
        addDanmaku(text,drawable);
    }

    /***
     * 发送图文弹幕
     * @param text
     * @param imgUrl
     */
    public void addDanmaku(final String text, final String imgUrl){
        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                Drawable drawable = ImageUtil.getDrawable(imgUrl);
                addDanmaku(text,drawable);
            }
        });
    }

    /***
     * 发送图文弹幕
     * @param text
     * @param drawable
     */
    public void addDanmaku(String text, Drawable drawable){
        if(danmakuView == null || !danmakuView.isPrepared()){
            return;
        }
        addDanmaKuShowTextAndImage(text,drawable,true);
    }

    private void addDanmaKuShowTextAndImage(String text, Drawable drawable, boolean islive) {
        BaseDanmaku danmaku = danmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);

        danmaku.isLive = islive;
        danmaku.padding = DANMAKU_PADDING;
        danmaku.priority = DANMAKU_PRIORITY;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.setTime(danmakuView.getCurrentTime() + 1200);
        danmaku.textSize = sp2px(DANMAKU_SIZE_SP);
        danmaku.textColor = DANMAKU_COLOR;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低

        SpannableStringBuilder spannable;
        if(drawable == null){
            spannable = createSpannable(null,text,null);
        }else{
            drawable.setBounds(0, 0, (int) danmaku.textSize*2, (int)danmaku.textSize*2);
            spannable = createSpannable(null,text,drawable);
        }
        danmaku.text = spannable;

        danmakuView.addDanmaku(danmaku);
    }

    private SpannableStringBuilder createSpannable(String imgDescribe, String str, Drawable drawable) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if(drawable != null){
            String text = "bitmap";
            if(!TextUtils.isEmpty(imgDescribe)){
                text = imgDescribe;
            }
            spannableStringBuilder = new SpannableStringBuilder(text);
            ImageSpan span = new ImageSpan(drawable);
            spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        spannableStringBuilder.append(str);
        return spannableStringBuilder;
    }

    /**
     * 生命周期
     * @param newConfig
     */
    public void configurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            danmakuView.getConfig().setDanmakuMargin(20);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            danmakuView.getConfig().setDanmakuMargin(40);
        }
    }

    /**
     * 生命周期
     */
    public void resume() {
        if (danmakuView != null) {
            danmakuView.resume();
        }
    }

    /**
     * 生命周期
     */
    public void pause() {
        if (danmakuView != null && danmakuView.isPrepared()) {
            danmakuView.pause();
        }
    }

    /**
     * 生命周期
     */
    public void destroy() {
        if (danmakuView != null) {
            danmakuView.release();
            danmakuView = null;
        }
    }

    /**
     * sp转px的方法。
     */
    public int sp2px(float spValue) {
        final float fontScale = mActivity.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 生命周期
     */
    public void generateSomeDanmaku() {
        final Drawable drawable = mActivity.getResources().getDrawable(R.mipmap.ic_launcher);

        final String imgUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1548592736401&di=b9d8a4ea1dd9b89927f6ba022da147c4&imgtype=0&src=http%3A%2F%2Fimg.bimg.126.net%2Fphoto%2FZZ5EGyuUCp9hBPk6_s4Ehg%3D%3D%2F5727171351132208489.jpg";
        final String imgUrl1 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1549249406&di=b911a14b216a30c86b3d716b874723b6&imgtype=jpg&er=1&src=http%3A%2F%2F5b0988e595225.cdn.sohucs.com%2Fimages%2F20180903%2Fd4b6378b81ab43569fc0048b62b6fedc.jpeg";
        final String imgUrl3 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1549249423&di=b8fbd63a78bb6d1c1fb2c3b68624ff04&imgtype=jpg&er=1&src=http%3A%2F%2Fd.ifengimg.com%2Fw600%2Fp0.ifengimg.com%2Fpmop%2F2018%2F0731%2FC3F50D7D9CFC4BE1A511C3655B3D1CF240263640_size49_w640_h400.jpeg";
        final String imgUrl5 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1548654718075&di=c074f2045ca72cc24b921b1fb7f9db77&imgtype=0&src=http%3A%2F%2Fimg.xiami.net%2Fimages%2Fappv5%2Fcommon%2F16302%2F5a4735ee81c6e_tSb7_1514616302.jpg";
        final String imgUrl7 = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3438269454,3376038619&fm=26&gp=0.jpg";
        final String imgUrl9 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1549249763&di=0c3be64cb97d07e1ef5ba38569023081&imgtype=jpg&er=1&src=http%3A%2F%2Fimg01.vgtime.com%2Fgame%2Fcover%2F2017%2F11%2F08%2F171108181503200.jpg";
        final String[] imgArray = {imgUrl,imgUrl1,imgUrl3,imgUrl5,imgUrl7,imgUrl9};

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int time = new Random().nextInt(20);
                    String content = "" + (time + time) * 135;
                    if(time>=0 && time <= 5){
                        DanmakuManager.getInstance().addDanmaku(content,imgArray[time]);
                    }else{
                        DanmakuManager.getInstance().addDanmaku(content);
                    }
                    try {
                        Thread.sleep(time*100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
