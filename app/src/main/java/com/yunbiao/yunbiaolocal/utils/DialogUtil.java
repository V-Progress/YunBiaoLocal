package com.yunbiao.yunbiaolocal.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
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
import android.widget.ListView;
import android.widget.TextView;

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

    private static String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());

    private static InsertPlayDialog insertPlayDialog;
    private static AlertDialog playlistDialog;

    /***
     * 展示播放列表dialog
     * @param onClickListener
     */
    public static void showPlayListDialog(final Activity mActivity, List<String> playList, final View.OnClickListener onClickListener) {
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(mActivity);
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

                } else {
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

        mDialogBuilder.setView(mLinearLayout);
        playlistDialog = mDialogBuilder.create();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playlistDialog.dismiss();
                if (mPreview.isPlaying()) {
                    mPreview.stopPlayback();
                }
                onClickListener.onClick(v);
            }
        });
        playlistDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        playlistDialog.show();
    }

    /**
     * 插播广告Dialog
     *
     * @param type
     * @param content
     */
    public static void showInsertDialog(Activity activity, int type, String content) {
        insertPlayDialog = InsertPlayDialog.build(activity);
        insertPlayDialog.show(content, type);
    }

    public static void showProgressDialog(Activity activity, String title, String message) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public static boolean isShowing(){
        return (insertPlayDialog!=null && insertPlayDialog.isShowing()) || (playlistDialog!=null && playlistDialog.isShowing());
    }

    public static Bitmap screenShot(){
        View decorView = null;
        if(insertPlayDialog != null && insertPlayDialog.isShowing()){
//            decorView = insertPlayDialog.getWindow().getDecorView();
            return insertPlayDialog.screenShot();
        }else if(playlistDialog != null && playlistDialog.isShowing()){
            decorView = playlistDialog.getWindow().getDecorView();
        }
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        return Bitmap.createBitmap(decorView.getDrawingCache());

    }

}
