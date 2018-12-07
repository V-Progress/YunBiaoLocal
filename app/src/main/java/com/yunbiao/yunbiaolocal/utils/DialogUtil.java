package com.yunbiao.yunbiaolocal.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.devicectrl.power.PowerControl;
import com.yunbiao.yunbiaolocal.devicectrl.ScreenShot;
import com.yunbiao.yunbiaolocal.resolve.VideoDataResolver;
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
    public void showPlayListDialog(final Activity mActivity, List<String> playList, final View.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        if (mActivity.isFinishing()) {
            return;
        }
        View mLinearLayout = LayoutInflater.from(mActivity).inflate(R.layout.alert_dialog, null);
        ListView mPlaylist = mLinearLayout.findViewById(R.id.playlist);
        TextView tvTime = mLinearLayout.findViewById(R.id.tv_time);
        Button btnClose = mLinearLayout.findViewById(R.id.btn_close_playlist);
        final VideoView mPreview = mLinearLayout.findViewById(R.id.preview);

        mPreview.setZOrderOnTop(true);//置顶显示，否则会被dialog遮挡，亮度变低
        tvTime.setText(VideoDataResolver.timer);
        mPlaylist.setDivider(mActivity.getResources().getDrawable(R.drawable.divider_playlist));

        mPlaylist.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, playList) {
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                TextView textView = (TextView) convertView;
                String text = getItem(position);
                textView.setText(text);
                if (text.contains(".")) {
                    textView.setTextColor(mActivity.getResources().getColor(R.color.white));
                    textView.setSingleLine();
                    textView.setEllipsize(TextUtils.TruncateAt.END);

                } else{
                    textView.setBackgroundColor(mActivity.getResources().getColor(R.color.trans_gray));
                }
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

        builder.setView(mLinearLayout);
        final AlertDialog alertDialog = builder.create();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (mPreview.isPlaying()) {
                    mPreview.stopPlayback();
                }
                onClickListener.onClick(v);
            }
        });
        alertDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        alertDialog.show();
    }

    /**
     * 插播广告Dialog
     *
     * @param type
     * @param content
     */
    public void showInsertDialog(int type, String content) {
        InsertPlayDialog insertPlayDialog = InsertPlayDialog.build(mActivity);
        insertPlayDialog.show(content, type);
    }
}
