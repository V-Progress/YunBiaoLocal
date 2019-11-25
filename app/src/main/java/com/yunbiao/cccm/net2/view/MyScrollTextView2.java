package com.yunbiao.cccm.net2.view;

/**
 * Created by Administrator on 2019/11/22.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanmu on 2016/7/1.
 */
public class MyScrollTextView2 extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final SurfaceHolder mHolder;
    /**
     * 与SurfaceHolder绑定的Canvas
     */
    private Canvas mCanvas;

    /**
     * 线程的控制开关
     */
    private boolean isRunning;

    private static final Boolean Debug = Boolean.valueOf(false);
    private int mDirection = 3;//向上滚动0,向左滚动3,向右滚动2,向上滚动1
    private int mScrollSpeed = 1; //2高速,0低速,1中速,3更高速，4更更高速
    private int mWholeLen = 0;
    private Paint mPaint = null;
    private float step = 0.0F;
    private List<String> textList = new ArrayList();
    private float width = 0.0F;
    private float y_coordinate = 0.0F;

    private String text = "";
    private int textColor = Color.parseColor("#000000");
    private int backColor = Color.parseColor("#ffffff");
    private float textSize = 30f;
    private PorterDuff.Mode mode;

    public MyScrollTextView2(Context context) {
        this(context, null);
    }

    public MyScrollTextView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(textColor);
        mPaint.setTextSize(textSize);
        setBackgroundColor(backColor);
        //设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        setZOrderOnTop(true);
//        setZOrderMediaOverlay(true);
        this.setOnTouchListener(new MyScrollTextView2.TextTouchListener());
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        mPaint.setColor(textColor);
    }

    public void setBackColor(int backColor, PorterDuff.Mode mode) {
        this.backColor = backColor;
//        this.mode = mode;
        setBackgroundColor(backColor);
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        mPaint.setTextSize(textSize);
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * 设置text滚动速度
     *
     * @return
     */
    public void setTextScrollSpeed(float scrollSpeed) {
        //2高速,0低速,1中速,3更高速，4更更高速
        Log.e("滚动速度", "" + scrollSpeed);
        if (scrollSpeed == 1) {
            this.mScrollSpeed = 0;
        } else if (scrollSpeed == 2) {
            this.mScrollSpeed = 2;
        } else if (scrollSpeed == 3) {
            this.mScrollSpeed = 4;
        } else if (scrollSpeed == 0) {
            this.mScrollSpeed = 0;
        } else {
            this.mScrollSpeed = 1;
        }
    }

    /**
     * 设置滚动速度
     *
     * @return
     */
    public void setScrollSpeed(float scrollSpeed) {
        //2高速,0低速,1中速,3更高速，4更更高速
        if (scrollSpeed == 0.2) {
            this.mScrollSpeed = -1;
        } else if (scrollSpeed == 0.5) {
            this.mScrollSpeed = 0;
        } else if (scrollSpeed == 1) {
            this.mScrollSpeed = 1;
        } else if (scrollSpeed == 2) {
            this.mScrollSpeed = 2;
        } else if (scrollSpeed == 3) {
            this.mScrollSpeed = 3;
        } else {
            this.mScrollSpeed = 1;
        }
    }

    /**
     * 设置滚动方向
     *
     * @param direction
     */
    public void setDirection(int direction) {
        this.mDirection = direction;
    }

    /**
     * 用于绘制的线程
     */
    private Thread t;

    /**
     * 处理开始
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(PixelFormat.TRANSLUCENT);//透明
        // 开启线程
        isRunning = true;
        Log.d(TAG, "surfaceCreated: -----------");
        t = new Thread(this);
        t.start();
    }

    //销毁时激发，一般在这里将画图的线程停止、释放。
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * 处理结束
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 通知关闭线程
        isRunning = false;
    }

    @Override
    public void run() {
        // 不断的进行draw
        while (isRunning) {
            draw();
        }
    }

    public void notifyData(String text) {
        this.text = text;
        this.step = 0;
        invalidate();
    }

    public void draw() {
        synchronized (mHolder) {
            try {
                // 获得canvas
                mCanvas = mHolder.lockCanvas();
                if (mCanvas != null) {

//                    mCanvas.drawColor(backColor, mode);
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色

                    if (this.textList.size() != 0) {
                        if (this.mDirection == 0) {
                            for (int i = 0; i < this.textList.size(); ++i) {
                                mCanvas.drawText((String) this.textList.get(i), 0.0F, (float) this.getHeight() + (float) (i + 1) * this.mPaint.getTextSize() - this.step, this.mPaint);
                            }
                            setSpeed(this.mScrollSpeed);
                            if (this.step >= (float) this.getHeight() + (float) this.textList.size() * this.mPaint.getTextSize()) {
                                this.step = 0.0F;
                            }
                        } else if (this.mDirection == 1) {
                            for (int i = 0; i < this.textList.size(); ++i) {
                                mCanvas.drawText((String) this.textList.get(i), 0.0F, (float) (-(this.textList.size() - i)) * this.mPaint.getTextSize() + this.step, this.mPaint);
                            }
                            setSpeed(this.mScrollSpeed);
                            if (this.step >= (float) this.getHeight() + (float) this.textList.size() * this.mPaint.getTextSize()) {
                                this.step = 0.0F;
                            }
                        } else {
                            if (this.mDirection == 2) {
                                if (this.step >= (float) (this.getWidth() + this.mWholeLen)) {
                                    this.step = 0.0F;
                                }
                                setSpeed(this.mScrollSpeed);
                                mCanvas.drawText(this.text, (float) (-this.mWholeLen) + this.step, this.y_coordinate, this.mPaint);
                                return;
                            }
                            if (this.mDirection == 3) {
                                if (this.step >= (float) (this.getWidth() + this.mWholeLen)) {
                                    this.step = 0.0F;
                                }
                                setSpeed(this.mScrollSpeed);
                                mCanvas.drawText(this.text, (float) this.getWidth() - this.step, this.y_coordinate, this.mPaint);
                            }
                        }
                    }
                }
            } catch (Exception e) {

            } finally {
                if (mCanvas != null) {
                    mHolder.unlockCanvasAndPost(mCanvas);

                    //清除画布
//              mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
//              mCanvas.drawPaint(mPaint);
//              mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OVER));

                }
            }
        }

    }


    private void setSpeed(int mScrollSpeed) {
        switch (mScrollSpeed) {
            case 0:
                this.step += 0.5F;
                break;
            case 1:
                ++this.step;
                break;
            case 2:
                this.step += 2.0F;
                break;
            case 3:
                this.step += 5.0F;
                break;
            case 4:
                this.step += 9.0F;
        }
    }


    protected void onMeasure(int context, int attrs) {
        super.onMeasure(context, attrs);
        this.width = (float) MeasureSpec.getSize(context);
        if (MeasureSpec.getMode(context) != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only can mCurScreen run at EXACTLY mode!");
        } else {
            float width = 0.0F;
            if (this.text != null && this.text.length() != 0) {
                this.textList.clear();
                StringBuilder stringbuilder = new StringBuilder();
                for (int i = 0; i < this.text.length(); ++i) {
                    char textValue = this.text.charAt(i);
                    float onlywidth = this.mPaint.measureText(this.text.substring(i, i + 1));
                    width += onlywidth;

                    if (width < this.width) {
                        stringbuilder.append(textValue);
                        if (textValue == 10) {//判断是否换行
                            this.textList.add(stringbuilder.toString());
                            stringbuilder.delete(0, -1 + stringbuilder.length());
                            width = 0;
                        } else {
                            if (i == -1 + this.text.length()) {
                                this.textList.add(stringbuilder.toString());
                            }
                        }
                    } else {
                        this.textList.add(stringbuilder.toString().substring(0, -1 + stringbuilder.toString().length()));
                        stringbuilder.delete(0, -1 + stringbuilder.length());
                        width = 0;
                        --i;
                    }
                }

                this.mWholeLen = 0;

                for (int k = 0; k < this.text.length(); ++k) {
                    this.mWholeLen = (int) ((float) this.mWholeLen + this.mPaint.measureText(this.text.substring(k, k + 1)));
                }

                Paint.FontMetricsInt fontMetricsInt = this.mPaint.getFontMetricsInt();
                this.y_coordinate = (float) (MeasureSpec.getSize(attrs) / 2 - (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.top);
                if (Debug) {
                    Log.d("-----", "y_coordinate:\t" + this.y_coordinate + "\n" + "height:\t" + this.getHeight() + "\n" + "width:\t" + this.getWidth() + "\n" + "measureWidth:\t" + MeasureSpec.getSize(context) + "\n" + "measureHeight:\t" + MeasureSpec.getSize(attrs) + "\n");
                }
            }
        }
    }

    private static final String TAG = "MyTextView2";

    private class TextTouchListener implements OnTouchListener {
        private int mMode;
        private PointF startPoint;
        private float start_step;

        private TextTouchListener() {
            this.startPoint = null;
            this.start_step = 0.0F;
            this.mMode = 0;
        }

        public boolean onTouch(View context, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onTouch: 0000000000");
                    this.startPoint = new PointF(event.getX(), event.getY());
                    this.start_step = MyScrollTextView2.this.step;
                    this.mMode = 1;
                    return true;
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "onTouch: 11111111111");
                    this.startPoint = null;
                    this.start_step = 0.0F;
                    this.mMode = 0;

                    return true;
                case MotionEvent.ACTION_MOVE:
                    Log.d(TAG, "onTouch: 22222222222");

                    if (this.mMode == 1) {
                        int direction = MyScrollTextView2.this.mDirection;
                        float i = 0.0F;
                        switch (direction) {
                            case 0:
                                i = this.startPoint.y - event.getY();
                                break;
                            case 1:
                                i = event.getY() - this.startPoint.y;
                                break;
                            case 2:
                                i = event.getX() - this.startPoint.x;
                                break;
                            case 3:
                                i = this.startPoint.x - event.getX();
                        }

                        MyScrollTextView2.this.step = i + this.start_step;

                        MyScrollTextView2.this.invalidate();
                        return true;
                    }
                default:
                    return true;
            }
        }
    }

    /**
     * 设置文本字体 2：宋体  3：楷体
     */
    public void setTextFont(Context context, String fontFamily) {
        if (!isNumeric(fontFamily)) {
            return;
        }
        int index = Integer.parseInt(fontFamily);
        if (index == 4 || index == 5) {
            index = 1;
        }
        if (index != 1) {
            Typeface typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/" + fonts[index - 2]);
            mPaint.setTypeface(typeFace);
        }
    }

    public static String[] fonts = new String[]{"song.ttf", "kai.ttf"};

    public static boolean isNumeric(String str) {
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
