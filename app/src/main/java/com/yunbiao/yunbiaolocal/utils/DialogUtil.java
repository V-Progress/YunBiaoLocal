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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.act.MainActivity;
import com.yunbiao.yunbiaolocal.io.Video;

import android.widget.Toast;
import android.widget.VideoView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2018/12/3.
 */

public class DialogUtil {

    private static DialogUtil instance;
    private AlertDialog.Builder mDialogBuilder;
    private static Activity mActivity;
    private String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());

    public static synchronized DialogUtil getInstance(Activity activity){
        mActivity = activity;
        if(instance == null){
            instance = new DialogUtil();
        }
        return instance;
    }

    public DialogUtil() {
        mDialogBuilder = new AlertDialog.Builder(mActivity);
        mDialogBuilder.setCancelable(false);
    }

    /***
     * 展示播放列表dialog
     * @param onClickListener
     */
    public void showPlayListDialog(List<String> playList ,final DialogInterface.OnClickListener onClickListener){
        LinearLayout mLinearLayout = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.alert_dialog, null);
        ListView mPlaylist = mLinearLayout.findViewById(R.id.playlist);
        TextView tvTime = mLinearLayout.findViewById(R.id.tv_time);
        final VideoView mPreview = mLinearLayout.findViewById(R.id.preview);

        mPlaylist.setAdapter(new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, playList){
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
                if (!text.matches("^.+\\.\\S+$")){
                    return;
                }
                if (mPreview.isPlaying()){
                    mPreview.stopPlayback();
                }
                String path = Video.previewMap.get(yyyyMMdd + text.substring(3));
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(mActivity, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPreview.setVideoPath(path);
                mPreview.start();
            }
        });

        tvTime.setText(Video.timer);

        mDialogBuilder.setView(mLinearLayout);
        mDialogBuilder.setTitle("播放列表");
        mDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mPreview.isPlaying()){
                    mPreview.stopPlayback();
                }
                onClickListener.onClick(dialog,which);
            }
        });

        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        if(!mActivity.isFinishing()){
            alertDialog.show();
        }
    }

    /***
     * 展示插播dialog
     */
    public void showInsertDialog(){


    }
}
