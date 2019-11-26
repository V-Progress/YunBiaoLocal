package com.yunbiao.cccm.net2;

import android.content.Context;
import android.graphics.PixelFormat;
import android.net.TrafficStats;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.R;

/**
 * Created by Administrator on 2019/11/19.
 */

public class ConsoleDialog {
    public static StringBuffer logTextBuffer = new StringBuffer();
    public static StringBuffer programTextBuffer = new StringBuffer();
    public static StringBuffer downloadTextBuffer = new StringBuffer();

    private final ScrollView svConsole;
    private final TextView tvConsole;
    private boolean isShown = false;

    private final WindowManager mWindowManager;
    private final View inflate;
    private final WindowManager.LayoutParams params;
    private final TextView tvSpeed;
    private final TextView tvTotal;
    private final TextView tvIndex;
    private final TextView tvProgress;
    private final TextView tvDate;
    private final TextView tvName;

    private String bytesToXX(long bytes) {
        String result = "";
        if(bytes >= 1024){
            bytes = bytes / 1024;
            result = bytes + "Mb";
        } else {
            result = bytes + "Kb";
        }
        return result;
    }

    private long mLastUpBytes = 0;
    private long mLastDownBytes = 0;
    private void updateText() {
        //更新日志
        tvConsole.setText(logTextBuffer.toString() + "\n" + downloadTextBuffer.toString() + "\n" + programTextBuffer.toString());
        svConsole.fullScroll(View.FOCUS_DOWN);

        long upBytes = getUpBytes();
        long downBytes = getDownBytes();
        long realUpKb = upBytes - mLastUpBytes;
        long realDownKb = downBytes - mLastDownBytes;
        mLastUpBytes = upBytes;
        mLastDownBytes = downBytes;

        String up = bytesToXX(realUpKb);
        String down = bytesToXX(realDownKb);
        tvSpeed.setText("上行：" + up + "\n下行：" + down);

        //更新下载信息
        tvDate.setText("日期：" + date);
        tvTotal.setText("总数：" + total);
        tvIndex.setText("当前：" + index);
        tvProgress.setText("进度：" + progress);
        tvName.setText("名称：" + name);
    }

    private long getUpBytes() {
        long uidTxBytes = TrafficStats.getUidTxBytes(APP.getContext().getApplicationInfo().uid);
        if(uidTxBytes == TrafficStats.UNSUPPORTED){
            return 0;
        } else {
            return uidTxBytes / 1024;
        }
    }

    private long getDownBytes() {
        long uidRxBytes = TrafficStats.getUidRxBytes(APP.getContext().getApplicationInfo().uid);
        if(uidRxBytes == TrafficStats.UNSUPPORTED){
            return 0;
        } else {
            return uidRxBytes / 1024;
        }
    }

    public static void addTextLog(String log) {
        logTextBuffer.append("\n").append(log);
    }

    public static void addDownloadLog(String log) {
//        downloadTextBuffer.append("\n").append(log);
        logTextBuffer.append("\n").append(log);
    }

    public static void addProgramLog(String log) {
        programTextBuffer.append("\n").append(log);
    }

    public ConsoleDialog(Context context) {
        inflate = View.inflate(context, R.layout.layout_console, null);
        svConsole = inflate.findViewById(R.id.sv_console);
        tvConsole = inflate.findViewById(R.id.tv_console);
        tvSpeed = inflate.findViewById(R.id.tv_net_speed);
        tvTotal = inflate.findViewById(R.id.tv_total);
        tvIndex = inflate.findViewById(R.id.tv_index);
        tvDate = inflate.findViewById(R.id.tv_date);
        tvProgress = inflate.findViewById(R.id.tv_progress);
        tvName = inflate.findViewById(R.id.tv_name);

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;// 类型
        /**设置flag
         *  FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
         *  设置 FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按
         *  不设置这个flag的话，home页的划屏会有问题
         *  | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         *  如果设置了WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE，弹出的View收不到Back键的事件
         */
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;

        inflate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            }
        });

        isShown = false;
    }

    public synchronized void show() {
        if (!isShown) {
            mWindowManager.addView(inflate, params);
            tvConsole.post(runnable);
            isShown = true;
        }
    }

    public synchronized void dismiss() {
        if (isShown) {
            mWindowManager.removeViewImmediate(inflate);
            tvConsole.removeCallbacks(runnable);
            isShown = false;
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateText();
            tvConsole.postDelayed(runnable, 1000);
        }
    };

    private static String date = "";
    private static int total = 0;
    private static int index = 0;
    private static int progress = 0;
    private static String name = "";

    public static void updateDownloadDate(String dateS) {
        date = dateS;
    }

    public static void updateTotal(int size) {
        total = size;
    }

    public static void updateIndex(int indexS) {
        index = total - indexS;
    }

    public static void updateProgress(int progresss) {
        progress = progresss;
    }

    public static void updateName(String names) {
        name = names;
    }
}
