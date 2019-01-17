//package com.yunbiao.cccm.act;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.drawable.BitmapDrawable;
//import android.graphics.drawable.Drawable;
//import android.text.Spannable;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.TextPaint;
//import android.text.style.BackgroundColorSpan;
//import android.text.style.ImageSpan;
//
//import com.yunbiao.cccm.R;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.HashMap;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import master.flame.danmaku.danmaku.loader.ILoader;
//import master.flame.danmaku.danmaku.loader.IllegalDataException;
//import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
//import master.flame.danmaku.danmaku.model.BaseDanmaku;
//import master.flame.danmaku.danmaku.model.DanmakuTimer;
//import master.flame.danmaku.danmaku.model.IDisplayer;
//import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
//import master.flame.danmaku.danmaku.model.android.DanmakuContext;
//import master.flame.danmaku.danmaku.model.android.DanmakuFactory;
//import master.flame.danmaku.danmaku.model.android.Danmakus;
//import master.flame.danmaku.danmaku.model.android.SimpleTextCacheStuffer;
//import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
//import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
//import master.flame.danmaku.danmaku.parser.IDataSource;
//import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser;
//import master.flame.danmaku.danmaku.util.IOUtils;
//import master.flame.danmaku.danmaku.util.SystemClock;
//import master.flame.danmaku.ui.widget.DanmakuView;
//
///**
// * Created by Administrator on 2019/1/15.
// */
//
//public class DanmuUtil {
//
//    private static DanmuUtil instance;
//    private static Context mContext;
//    private DanmakuContext danmakuContext;
//    private BaseDanmakuParser mParser;//解析器对象
//    private DanmakuView dmView;
//    private Timer timer;
//
//    public synchronized static DanmuUtil getInstance(Context context) {
//        mContext = context;
//        if (instance == null) {
//            instance = new DanmuUtil();
//        }
//        return instance;
//    }
//
//    /***
//     *  图文混排使用SpannedCacheStuffer
//     *  设置缓存绘制填充器，默认使用{@link SimpleTextCacheStuffer}只支持纯文字显示,
//     *  如果需要图文混排请设置{@link SpannedCacheStuffer}
//     *  如果需要定制其他样式请扩展{@link SimpleTextCacheStuffer}|{@link SpannedCacheStuffer}
//     */
//    public void init(final DanmakuView dmView) {
//        this.dmView = dmView;
//        danmakuContext = DanmakuContext.create();
//        // 设置弹幕的最大显示行数
//        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
//        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3); // 滚动弹幕最大显示3行
//
//        // 设置是否禁止重叠
//        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
//        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
//        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
//
//        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3) //设置描边样式
//                .setDuplicateMergingEnabled(false)
//                .setScrollSpeedFactor(1.2f) //是否启用合并重复弹幕
//                .setScaleTextSize(1.2f) //设置弹幕滚动速度系数,只对滚动弹幕有效
//                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter)
//                .setMaximumLines(maxLinesPair) //设置最大显示行数
//                .preventOverlapping(overlappingEnablePair); //设置防弹幕重叠，null为允许重叠
//
//        if (dmView != null) {
//            mParser = createParser(mContext.getResources().openRawResource(R.raw.comments)); //创建解析器对象，从raw资源目录下解析comments.xml文本
//            dmView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
//                @Override
//                public void updateTimer(DanmakuTimer timer) {
//                }
//
//                @Override
//                public void drawingFinished() {
//
//                }
//
//                @Override
//                public void danmakuShown(BaseDanmaku danmaku) {
//
//                }
//
//                @Override
//                public void prepared() {
//                    dmView.start();
//                }
//            });
//
//            dmView.prepare(mParser, danmakuContext);
//            dmView.showFPS(false); //是否显示FPS
//            dmView.enableDanmakuDrawingCache(true);
//        }
//    }
//
//    private void sendDanmu(){
//        Boolean b = (Boolean) mBtnSendDanmakus.getTag();
//        timer.cancel();
//        if (b == null || !b) {
//            mBtnSendDanmakus.setText(R.string.cancel_sending_danmakus);
//            timer = new Timer();
//            timer.schedule(new AsyncAddTask(), 0, 1000);
//            mBtnSendDanmakus.setTag(true);
//        } else {
//            mBtnSendDanmakus.setText(R.string.send_danmakus);
//            mBtnSendDanmakus.setTag(false);
//        }
//
//        Timer timer = new Timer();
//
//        class AsyncAddTask extends TimerTask {
//
//            @Override
//            public void run() {
//                for (int i = 0; i < 20; i++) {
//                    addDanmaku(true);
//                    SystemClock.sleep(20);
//                }
//            }
//        }
//    }
//
//    private void addDanmaku(boolean islive) {
//        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
//        if (danmaku == null || mDanmakuView == null) {
//            return;
//        }
//
//        danmaku.text = "这是一条弹幕" + System.nanoTime();
//        danmaku.padding = 5;
//        danmaku.priority = 0;  //0 表示可能会被各种过滤器过滤并隐藏显示 //1 表示一定会显示, 一般用于本机发送的弹幕
//        danmaku.isLive = islive; //是否是直播弹幕
//        danmaku.time = dmView.getCurrentTime() + 1200; //显示时间
//        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
//        danmaku.textColor = Color.RED;
//        danmaku.textShadowColor = Color.WHITE; //阴影/描边颜色
//        danmaku.borderColor = Color.GREEN; //边框颜色，0表示无边框
//        dmView.addDanmaku(danmaku);
//
//    }
//
//    private BaseDanmakuParser createParser(InputStream stream) {
//        if (stream == null) {
//            return new BaseDanmakuParser() {
//
//                @Override
//                protected Danmakus parse() {
//                    return new Danmakus();
//                }
//            };
//        }
//
//        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
//
//        try {
//            loader.load(stream);
//        } catch (IllegalDataException e) {
//            e.printStackTrace();
//        }
//        BaseDanmakuParser parser = new BiliDanmukuParser();
//        IDataSource<?> dataSource = loader.getDataSource();
//        parser.load(dataSource);
//        return parser;
//    }
//
//    private static class BackgroundCacheStuffer extends SpannedCacheStuffer {
//        // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
//        final Paint paint = new Paint();
//
//        @Override
//        public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
//            danmaku.padding = 10;  // 在背景绘制模式下增加padding
//            super.measure(danmaku, paint, fromWorkerThread);
//        }
//
//        @Override
//        public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
//            paint.setColor(0x8125309b);  //弹幕背景颜色
//            canvas.drawRect(left + 2, top + 2, left + danmaku.paintWidth - 2, top + danmaku.paintHeight - 2, paint);
//        }
//
//
//        @Override
//        public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
//            // 禁用描边绘制
//        }
//    }
//
//    private void addDanmaKuShowTextAndImage(boolean islive) {
//        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
//        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
//        drawable.setBounds(0, 0, 100, 100);
//        SpannableStringBuilder spannable = createSpannable(drawable);
//        danmaku.text = spannable;
//        danmaku.padding = 5;
//        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
//        danmaku.isLive = islive;
//        danmaku.time = mDanmakuView.getCurrentTime() + 1200;
//        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
//        danmaku.textColor = Color.RED;
//        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
//        danmaku.underlineColor = Color.GREEN;
//        mDanmakuView.addDanmaku(danmaku);
//    }
//
//    /**
//     * 创建图文混排模式
//     * @param drawable
//     * @return
//     */
//    private SpannableStringBuilder createSpannable(Drawable drawable) {
//        String text = "bitmap";
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
//        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
//        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.append("图文混排");
//        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//        return spannableStringBuilder;
//    }
//
//    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {
//
//        private Drawable mDrawable;
//
//        /**
//         * 在弹幕显示前使用新的text,使用新的text
//         * @param danmaku
//         * @param fromWorkerThread 是否在工作(非UI)线程,在true的情况下可以做一些耗时操作(例如更新Span的drawblae或者其他IO操作)
//         * @return 如果不需重置，直接返回danmaku.text
//         */
//        @Override
//        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
//            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
//                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
//                new Thread() {
//
//                    @Override
//                    public void run() {
//                        String url = "http://www.bilibili.com/favicon.ico";
//                        InputStream inputStream = null;
//                        Drawable drawable = mDrawable;
//                        if (drawable == null) {
//                            try {
//                                URLConnection urlConnection = new URL(url).openConnection();
//                                inputStream = urlConnection.getInputStream();
//                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
//                                mDrawable = drawable;
//                            } catch (MalformedURLException e) {
//                                e.printStackTrace();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            } finally {
//                                IOUtils.closeQuietly(inputStream);
//                            }
//                        }
//                        if (drawable != null) {
//                            drawable.setBounds(0, 0, 100, 100);
//                            SpannableStringBuilder spannable = createSpannable(drawable);
//                            danmaku.text = spannable;
//                            if (dmView != null) {
//                                dmView.invalidateDanmaku(danmaku, false);
//                            }
//                            return;
//                        }
//                    }
//                }.start();
//            }
//        }
//
//        @Override
//        public void releaseResource(BaseDanmaku danmaku) {
//            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
//        }
//    };
//
//    public void destroy() {
//        if (dmView != null) {
//            // dont forget release!
//            dmView.release();
//            dmView = null;
//        }
//    }
//}