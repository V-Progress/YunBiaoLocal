package com.yunbiao.cccm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2019/1/15.
 */

public class DanmuView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    //是否显示弹幕
    private boolean isShow = true;

    //SurfaceHolder
    SurfaceHolder surfaceHolder;

    //弹幕集合
    public List<DanmuTxt> barrages = new ArrayList<>();

    //用于随机生成弹幕的Y轴坐标
    Random random = new Random();

    public DanmuView(Context context) {
        super(context);
    }

    public DanmuView(Context context, AttributeSet attrs) {
        super(context, attrs);

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        //设置背景透明
        this.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
    }

    public DanmuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void run() {
        Canvas canvas;
        DanmuTxt danmuTxt;
        Paint paint = null;
        while (isShown()) {
            if (barrages.size() == 0) {
                continue;
            }

            try {
                //获取画布
                canvas = surfaceHolder.lockCanvas();
                //清空画布
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }


            for (int i = 0; i < barrages.size(); i++) {
                //取出弹幕
                danmuTxt = barrages.get(i);
                //如果弹幕中的画笔为空，则创建一个画笔
                if(danmuTxt.getPaint() == null){
                    if(paint == null){
                        paint = new Paint();
                    }
                    paint.setColor(danmuTxt.getColor());
                    paint.setTextSize(danmuTxt.getSize());
                    paint.setStrokeWidth(3f);
                }else{
                    paint = danmuTxt.getPaint();
                }
                //开始绘制
                canvas.drawText(danmuTxt.getTxt(),danmuTxt.getX(),danmuTxt.getY(),paint);
                //如果弹幕的X轴超过屏幕左侧，删除该弹幕，否则移动
                if(danmuTxt.getX()<-getWidth()){
                    barrages.remove(danmuTxt);
                }else{
                    danmuTxt.setX(danmuTxt.getX()-danmuTxt.getSpeed());
                }

                //解锁画布
                surfaceHolder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //添加弹幕
    public void add(DanmuTxt mText) {
        if (mText.getX() == 0) {
            mText.setX(getWidth());
        }
        if (mText.getY() == 0) {
            int i = getHeight();
            if (i > 0) {
                mText.setY(random.nextInt(i));
            }

        }
        barrages.add(mText);
    }

    public static class DanmuTxt {
        private String txt;
        private float size;
        private Integer color;
        private float x;
        private float y;
        private int speed;
        private Paint paint;

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = size;
        }

        public Integer getColor() {
            return color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public Paint getPaint() {
            return paint;
        }

        public void setPaint(Paint paint) {
            this.paint = paint;
        }
    }
}
