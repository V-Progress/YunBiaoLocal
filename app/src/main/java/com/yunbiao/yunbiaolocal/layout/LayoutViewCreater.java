package com.yunbiao.yunbiaolocal.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.layout.bean.Center;
import com.yunbiao.yunbiaolocal.layout.bean.LayoutPosition;
import com.yunbiao.yunbiaolocal.layout.bean.TextDetail;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.BusinessBase;
import com.yunbiao.yunbiaolocal.layout.view.web.MyWebView;
import com.yunbiao.yunbiaolocal.layout.view.web.MyXWalkView;
import com.yunbiao.yunbiaolocal.utils.CommonUtils;
import com.yunbiao.yunbiaolocal.view.MyScrollTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/12/12.
 */

public class LayoutViewCreater {
    private static LayoutViewCreater instance;

    private Integer mType;

    private static Context mContext;
    private AbsoluteLayout.LayoutParams mLayoutParams;
    private Center mContent;

    private final int LAYOUT_MUSIC = -3;//音乐
    private final int LAYOUT_TEXT = 2;//音乐
    private final int LAYOUT_BG = 0;//大背景处理,纯色或图片
    private final int LAYOUT_IMG_VIDEO = 1;//图片或视频
    private final int LAYOUT_BGM = 6;//背景音乐
    private final int LAYOUT_VIDEO = 3;//视频处理 目前没用跟图片合并
    private final int LAYOUT_WECHAT = 4;// 微信处理
    private final int LAYOUT_WEB = 5;//网页

    private final int LAYOUT_LCOALREC = 8;//本地资源
    private final int LAYOUT_CAMERA = 12;//摄像头
    private final int LAYOUT_QUEUE = 13;//排队叫好
    private final int LAYOUT_BASE_ = 14;//基础控件
    private final int LAYOUT_TOUCH_QUERY = 15;//触摸查询
    private final int LAYOUT_BAIDU_ADS = 17;//百度广告联盟
    private final int LAYOUT_SELF_ADS = 18;//自运营广告

    public static synchronized LayoutViewCreater getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new LayoutViewCreater();
        }
        return instance;
    }

    public LayoutViewCreater type(@NonNull Integer type) {
        mType = type;
        return instance;
    }

    public LayoutViewCreater layoutPosition(@NonNull LayoutPosition layoutPosition) {
        mLayoutParams
                = new AbsoluteLayout.LayoutParams(layoutPosition.getWidth()
                , layoutPosition.getHeight()
                , layoutPosition.getLeft()
                , layoutPosition.getTop());
        return instance;
    }

    public LayoutViewCreater content(Center center) {
        mContent = center;
        return instance;
    }

    public View create() {
        View view = null;
        switch (mType) {
            case LAYOUT_TEXT:
                view = createTextView(mContext, mLayoutParams, mContent);
                break;
            case LAYOUT_BG:

                break;
            case LAYOUT_IMG_VIDEO:
                view = getImageView(mContext, mLayoutParams, mContent);
                break;
            case LAYOUT_BGM:

                break;
            case LAYOUT_VIDEO:

                break;
            case LAYOUT_WECHAT:

                break;
            case LAYOUT_WEB:
                view = createWebView(mContext, mLayoutParams, mContent);
                break;
            case LAYOUT_CAMERA:

                break;
            case LAYOUT_QUEUE:

                break;
            case LAYOUT_BASE_:
                view = createBaseView(mContext,mLayoutParams,mContent);
                break;
            case LAYOUT_LCOALREC:

                break;
            case LAYOUT_TOUCH_QUERY:

                break;
            case LAYOUT_BAIDU_ADS:

                break;
            case LAYOUT_SELF_ADS:

                break;
        }
        return view;
    }

    private View createTextView(Context context, AbsoluteLayout.LayoutParams layoutParams, Center center) {

        TextDetail textDetail = center.getTextDetail();
        String[] content = center.getContent();

        Boolean isScroll = Boolean.valueOf(textDetail.getIsPlay());
        //判断是否是滚动字幕
        if (isScroll) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setPadding(0, 0, 0, 0);

            final MyScrollTextView scrollTv = new MyScrollTextView(context);
            scrollTv.setTextSize(textDetail.getFontSize());
            scrollTv.setTextColor(Color.parseColor(textDetail.getFontColor()));

            scrollTv.setLayoutParams(layoutParams);
            scrollTv.setTextFont(context, textDetail.getFontFamily());
            //判断处理文本内容
            StringBuilder scrollSb = new StringBuilder("");

            for (String aContent : content) {
                scrollSb.append(aContent).append("  ");
            }
            scrollTv.setText(scrollSb.toString());

            scrollTv.setScrollSpeed(textDetail.getPlayTime());

            if (Integer.parseInt(textDetail.getPlayType()) == 0) {
                scrollTv.setDirection(3);//向上滚动0,向左滚动3,向右滚动2,向上滚动1
            } else if (Integer.parseInt(textDetail.getPlayType()) == 1) {
                scrollTv.setDirection(0);
            }

            scrollTv.setBackColor(Color.parseColor(textDetail.getBackground()));

            linearLayout.addView(scrollTv);

            return linearLayout;

        } else {
            final TextView marquee = new TextView(context);
            marquee.setTextSize(textDetail.getFontSize());

            StringBuilder sb = new StringBuilder("");
            for (String aContent : content) {
                sb.append(aContent).append("  ");
            }
            marquee.setText(sb.toString());

            String textAlign = textDetail.getTextAlign();
            if (TextUtils.isEmpty(textAlign) || textAlign.equals("center")) {
                marquee.setGravity(Gravity.CENTER);
            } else if (textAlign.equals("left")) {
                marquee.setGravity(Gravity.LEFT);
            } else if (textAlign.equals("right")) {
                marquee.setGravity(Gravity.RIGHT);
            }

            marquee.setLayoutParams(layoutParams);
            try {
                marquee.setTextColor(Color.parseColor(textDetail.getFontColor()));
            } catch (NumberFormatException e) {
                marquee.setTextColor(Color.parseColor("#000000"));
            }
            try {
                marquee.setBackgroundColor(Color.parseColor(textDetail.getBackground()));
            } catch (NumberFormatException e) {
                marquee.setBackgroundColor(Color.parseColor("#ffffff"));
            }
            setTextFont(context, marquee, textDetail.getFontFamily());

            return marquee;
        }
    }

    private View getImageView(final Context context, AbsoluteLayout.LayoutParams layoutParams, Center center) {
        ImageLoaderConfiguration loaderConfiguration = ImageLoaderConfiguration.createDefault(context);
        ImageLoader.getInstance().init(loaderConfiguration);
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(layoutParams);
        ImageLoader.getInstance().displayImage("http://pic24.nipic.com/20121010/3798632_184253198370_2.jpg", imageView);
        return imageView;
    }

    private View createWebView(Context context, AbsoluteLayout.LayoutParams layoutParams, Center center) {
        String webType = center.getWebDetail().getWebType();
        if (TextUtils.isEmpty(webType)) {
            return addWebPageView(context, center, layoutParams);
        } else {
            if (webType.equals("1")) {//网页
                return addWebPageView(context, center, layoutParams);
            } else if (webType.equals("2")) {//直播流
                return addLiveRadioView(context, center, layoutParams);
            }
        }
        return null;
    }

    /**
     * 添加网页 XWalkView
     */
    public static View addWebPageView(Context context, Center center, AbsoluteLayout.LayoutParams layoutParams) {
        Integer broadTye = CommonUtils.getBroadType();
        String release = Build.VERSION.RELEASE;
        if ((broadTye == 0 || broadTye == 4) && !release.startsWith("6") && !release.startsWith("7")) {
            MyXWalkView xWalk = new MyXWalkView(context, center);
            View xWalkView = xWalk.getView();
            xWalkView.setLayoutParams(layoutParams);
            return xWalkView;
        } else {
            MyWebView web = new MyWebView(context, center);
            View webView = web.getView();
            webView.setLayoutParams(layoutParams);
            return webView;
        }
    }

    /***
     * 添加直播控件
     * @param context
     * @param layoutInfo
     * @param layoutParams
     * @return
     */
    public static FrameLayout addLiveRadioView(Context context, Center layoutInfo, AbsoluteLayout.LayoutParams layoutParams) {
        FrameLayout linearLayout = new FrameLayout(context);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setBackgroundResource(R.color.black);

        final String url = layoutInfo.getContent()[0];
        final VideoView liveView = new VideoView(context);
        try {
            liveView.setVideoPath(url);
            liveView.requestFocus();
            liveView.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        liveView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

        liveView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(APP.getContext(), R.string.play_error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        liveView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    Toast.makeText(APP.getContext(), R.string.play_cache, Toast.LENGTH_SHORT).show();
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    if (mp.isPlaying()) {
                        Toast.makeText(APP.getContext(), R.string.cache_complete, Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });

        FrameLayout.LayoutParams logoViewParams = new FrameLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams
                .MATCH_PARENT);
        logoViewParams.gravity = Gravity.CENTER;
        linearLayout.addView(liveView, logoViewParams);

        return linearLayout;
    }

    /***
     * 创建基础控件
     * @param context
     * @param layoutParams
     * @param center
     * @return
     */
    private View createBaseView(Context context, AbsoluteLayout.LayoutParams layoutParams, Center center) {
        Integer windowType = mContent.getWindowType();
        if (windowType == null) {
            return null;
        }
        return BusinessBase.getInstance().runBaseControlsView(context, center, layoutParams);
    }

    /**
     * 设置文本字体
     */
    private static void setTextFont(Context context, TextView textView, String fontFamily) {
        if (!isNumeric(fontFamily)) {
            return;
        }
        int index = Integer.parseInt(fontFamily);
        if (index == 4 || index == 5) {
            index = 1;
        }
        if (index != 1) {
            Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/" + fonts[index - 2]);
            textView.setTypeface(typeFace);
        }
    }

    private static String[] fonts = new String[]{"song.ttf", "kai.ttf"};

    private static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


}
