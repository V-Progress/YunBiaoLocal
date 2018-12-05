package com.yunbiao.yunbiaolocal.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.devicectrl.PowerControl;
import com.yunbiao.yunbiaolocal.devicectrl.ScreenShot;
import com.yunbiao.yunbiaolocal.devicectrl.SoundControl;
import com.yunbiao.yunbiaolocal.io.VideoDataResolver;
import com.yunbiao.yunbiaolocal.view.InsertPlayDialog;
import com.yunbiao.yunbiaolocal.R;

import android.widget.Toast;
import android.widget.VideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/12/3.
 */

public class DialogUtil {

    public static final int INSERT_VIDEO = 0;
    public static final int INSERT_TEXT = 1;
    public static final int INSERT_LIVE = 2;

    private static DialogUtil instance;
    private AlertDialog.Builder mDialogBuilder;
    private static Activity mActivity;
    private String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());

    public static synchronized DialogUtil getInstance(Activity activity) {
        mActivity = activity;
        if (instance == null) {
            instance = new DialogUtil();
        }
        return instance;
    }

    public DialogUtil() {
        mDialogBuilder = new AlertDialog.Builder(mActivity);
        mDialogBuilder.setCancelable(false);// TODO: 2018/12/4 暂时关闭
    }

    /***
     * 展示播放列表dialog
     * @param onClickListener
     */
    public void showPlayListDialog(List<String> playList, final DialogInterface.OnClickListener onClickListener) {
        LinearLayout mLinearLayout = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.alert_dialog, null);
        ListView mPlaylist = mLinearLayout.findViewById(R.id.playlist);
        TextView tvTime = mLinearLayout.findViewById(R.id.tv_time);
        final VideoView mPreview = mLinearLayout.findViewById(R.id.preview);

        mPlaylist.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, playList) {
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                TextView textView = (TextView) convertView;
                String text = getItem(position);
                textView.setText(text);
                if (text.contains(".")) {
                    textView.setSingleLine();
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                } else
                    textView.setBackgroundColor(Color.parseColor("#333333"));
                return convertView;
            }
        });
        mPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String text = textView.getText().toString();
                if (!text.matches("^.+\\.\\S+$")) {
                    return;
                }
                if (mPreview.isPlaying()) {
                    mPreview.stopPlayback();
                }
                String path = VideoDataResolver.previewMap.get(yyyyMMdd + text.substring(3));
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(mActivity, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPreview.setVideoPath(path);
                mPreview.start();
            }
        });

        tvTime.setText(VideoDataResolver.timer);

        mDialogBuilder.setView(mLinearLayout);
        mDialogBuilder.setTitle("播放列表");
        mDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mPreview.isPlaying()) {
                    mPreview.stopPlayback();
                }
                onClickListener.onClick(dialog, which);
            }
        });

        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        if (!mActivity.isFinishing()) {
            alertDialog.show();
        }
    }

    /**
     * 插播广告Dialog
     *
     * @param type
     * @param content
     */
    public void showInsertDialog(int type, String content) {
        InsertPlayDialog insertPlayDialog = InsertPlayDialog.build(mActivity, type);
        insertPlayDialog.show(content);
    }

    public void showTestController(){
        View inflate = LayoutInflater.from(mActivity).inflate(R.layout.layout_controller, null);
        Button btnUp = inflate.findViewById(R.id.btn_vol_up);
        Button btnDown = inflate.findViewById(R.id.btn_vol_down);
        Button btnSS = inflate.findViewById(R.id.btn_vol_screenshot);
        Button btnC = inflate.findViewById(R.id.btn_vol_close);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_vol_up:
                        break;
                    case R.id.btn_vol_down:
                        break;
                    case R.id.btn_vol_screenshot:
                        ScreenShot.getInstanse().shootScreen();
                        break;
                    case R.id.btn_vol_close:
                        PowerControl.getInstance().setPowerRunTime();
                        break;
                }
            }
        };
        btnUp.setOnClickListener(onClickListener);
        btnDown.setOnClickListener(onClickListener);
        btnSS.setOnClickListener(onClickListener);
        btnC.setOnClickListener(onClickListener);

        mDialogBuilder.setView(inflate);
        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
        alertDialog.getWindow().setLayout(-1,-1);
    }
}
