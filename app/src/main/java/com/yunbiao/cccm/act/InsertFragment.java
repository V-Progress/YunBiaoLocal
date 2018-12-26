package com.yunbiao.cccm.act;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.yunbiao.cccm.utils.TimerExecutor;

import java.io.File;
import java.util.Date;

/**
 * Created by Administrator on 2018/12/26.
 */

public class InsertFragment extends Fragment {

    private View rootView;
    private FrameLayout flInsertRoot;
    private VideoView videoInsert;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_insert, container,false);
        flInsertRoot = rootView.findViewById(R.id.fl_insert_root);
        videoInsert = rootView.findViewById(R.id.vv_insert);
        return rootView;
    }

    public void updatePlayData(){

    }


    /*
     * 设置播放视频
     * @param startDate
     * @param endDate
     * @param fileurl
     */
    private void setVideo(final Date startDate, final Date endDate, String fileurl) {
        if (fileurl.endsWith(".avi") || fileurl.endsWith(".mp4") || fileurl.endsWith(".3gp")) {
            NetUtil.getInstance().downloadFile(fileurl, new NetUtil.OnDownLoadListener() {
                @Override
                public void onStart(String fileName) {
                    LogUtil.E("开始下载视频");
                }

                @Override
                public void onProgress(int progress) {
                    LogUtil.E("正在下载-" + progress + "%");
                }

                @Override
                public void onComplete(final File response) {
                    TimerExecutor.getInstance().addInTimerQueue(startDate, new TimerExecutor.OnTimeOutListener() {
                        @Override
                        public void execute() {

                        }
                    });
                    TimerExecutor.getInstance().addInTimerQueue(endDate, new TimerExecutor.OnTimeOutListener() {
                        @Override
                        public void execute() {

                        }
                    });
                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onError(Exception e) {
                    LogUtil.E("下载失败：" + e.getMessage());
                }
            });
        } else if ((fileurl.startsWith("http://") && fileurl.endsWith(".m3u8"))) {

        } else {

        }
    }

}
