package com.yunbiao.cccm.net.download;

import android.support.annotation.NonNull;
import android.util.Log;

import com.yunbiao.cccm.net.listener.MultiFileDownloadListener;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2019/2/22.
 */

public abstract class BPDownload {
    //是否打印log
    private boolean isLog = true;

    //log的TAG
    private final String TAG = getClass().getSimpleName();

    //OkHttp
    private final OkHttpClient okHttpClient;

    //请求超时时间
    private final long TIMEOUT_LONG = 30;

    //请求超时单位
    private final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    //取消下载标识
    protected boolean cancel = false;

    //下载时缓冲区大小
    protected static final int BUFFER_SIZE = 4096;

    //下载监听
    protected MultiFileDownloadListener mListener;

    //当前文件标识
    protected int currFileNum = 1;

    //总文件数量
    protected int totalNum = 0;

    Object mTag;

    public BPDownload(@NonNull Object tag, @NonNull MultiFileDownloadListener listener) {
        mListener = listener;
        mTag = tag;
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .readTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .writeTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .build();
    }

    abstract void breakPointDownload(Queue<String> fileUrlList);

    /***
     * 只有下载成功的时候才会增加文件索引
     */
    protected void onSuccess(int totalNum, String fileName) {
        d("download onSuccess...");
        mListener.onSuccess(currFileNum, totalNum, fileName);
        currFileNum++;
    }

    protected void onError(Exception e, int totalNum, String fileName) {
        d("download onError...");
        mListener.onError(e, currFileNum, totalNum, fileName);
        currFileNum++;
    }


    /**
     * 得到下载内容的大小
     *
     * @param downloadUrl
     * @return
     */
    protected long getContentLength(String downloadUrl) {
        int retryNum = 0;
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.body().close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if(retryNum < 3){
                retryNum++;
                LogUtil.E(TAG,"获取远程文件大小失败，重试次数：3，当前次数："+retryNum);
                getContentLength(downloadUrl);
            }
        }
        return 0;
    }

    /***
     * 取消当前下载
     */
    public void cancel() {
        cancel = true;
        cancelTag(okHttpClient, mTag);
    }

    /**
     * 根据Tag取消请求
     */
    private static void cancelTag(OkHttpClient client, Object tag) {
        if (client == null || tag == null) return;
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 关闭流
     * @param closeable
     */
    protected void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 输出debug日志
     * @param msg
     */
    protected void d(String msg) {
        if (isLog) {
            Log.d(TAG, msg);
        }
    }

    /***
     * 下载流程异常
     */
    protected class DownloadException extends Exception {
        public static final int CODE_FAILED_CONTENT_LENGTH = -1;//获取远程文件大小失败(无需重试)
        public static final int CODE_SUCCESS_DOWNLOAD = 0;//下载成功(无需重试)
        public static final int CODE_CACHE_DOWNLOADED_RENAME_FAILED = 1;//缓存文件下载完成，更名失败(重试)
        public static final int CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED = 2;//存在但大小不正确，删除本地文件失败(重试)
        public static final int CODE_LOCAL_LENGTH_ERROR_DELETE = 3;//存在但大小不正确，删除本地文件成功(重试)
        public static final int CODE_CACHE_DOWNLOADED_LENGTH_ERROR = 4;//缓存下载完成后大小不一致，删除重新下载(重试)

        protected int errCode;
        protected String errMsg;

        @Override
        public String getMessage() {
            return errMsg;
        }

        public DownloadException(int errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }
    }
}
