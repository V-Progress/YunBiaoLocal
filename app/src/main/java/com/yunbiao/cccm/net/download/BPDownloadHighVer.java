package com.yunbiao.cccm.net.download;

import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.common.YunBiaoException;
import com.yunbiao.cccm.sdOperator.HighVerSDOperator;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.SDUtil;
import com.yunbiao.cccm.net.listener.MultiFileDownloadListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Queue;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2019/2/22.
 */

public class BPDownloadHighVer extends BPDownload {

    private HighVerSDOperator fileOperator;

    public BPDownloadHighVer(@NonNull Object tag, MultiFileDownloadListener listener) {
        super(tag,listener);
        fileOperator = HighVerSDOperator.instance();

        String sdUri = PreferenceManager.getDefaultSharedPreferences(APP.getContext()).getString(SDUtil.PREF_DEFAULT_URI, null);
        Uri uri = Uri.parse(sdUri);
        fileOperator.generateStoragePath(uri);
    }

    @Override
    public void breakPointDownload(Queue<String> urlQueue) {
        if (urlQueue.size() <= 0) {
            mListener.onFinish();
            return;
        }

        if (cancel) {
            return;
        }

        //取出URL
        String downloadUrl = urlQueue.poll();

        //取出文件名称
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")).substring(1);
        String cacheFileName = "cache_" + fileName;
        LogUtil.E("准备下载的文件名：" + fileName);

        InputStream is = null;
        OutputStream os = null;

        try {
            //获取远程文件长度
            long contentLength = getContentLength(downloadUrl);
            LogUtil.E("远程文件大小：" + contentLength);
            //如果下载的文件长度为0并且重试次数未超
            if (contentLength == 0) {
                throw new DownloadException(DownloadException.CODE_FAILED_CONTENT_LENGTH, "Get File's Length Error");
            }

            //查找该文件
            DocumentFile localFile = fileOperator.findResource(fileName);
            if(localFile != null && localFile.exists()){//如果文件存在
                if(localFile.length() == contentLength){//并且大小等于远程
                    LogUtil.E("文件已下载完成，结束");
                    throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
                }else{
                    boolean delete = localFile.delete();
                    if (!delete) {
                        throw new DownloadException(DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED, "local file's length error, delete failed");
                    }
                    throw new DownloadException(DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE, "local file's length error, delete");
                }
            }

            DocumentFile cacheFile = fileOperator.findResource(cacheFileName);
            if(cacheFile != null && cacheFile.exists()){//如果缓存文件存在
                if(cacheFile.length() == contentLength){//并且已下载完
                    LogUtil.E("缓存文件已下载完毕，修改名称");
                    boolean renameTo = cacheFile.renameTo(fileName);
                    LogUtil.E("修改名称结果："+renameTo);
                    if (!renameTo) {//改名失败
                        throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED, "download success, rename cacheFile failed");
                    }

                    throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));

                }
            }else{//缓存文件不存在，创建
                cacheFile = fileOperator.createVideoRes(cacheFileName);
            }
            LogUtil.E("缓存文件的uri：" + cacheFile.getUri().toString());


            d("start download...");
            mListener.onStart(currFileNum);

            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + cacheFile.length() + "-")  //断点续传要用到的，指示下载的区间
                    .url(downloadUrl)
                    .tag(this)
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                os = fileOperator.getOutputStream(cacheFile);

                int realProgress = 0;

                byte[] b = new byte[BUFFER_SIZE];
                int len;
                while ((len = is.read(b)) != -1) {
                    mListener.onDownloadSpeed(len);

                    if (cancel) {
                        return;
                    }

                    os.write(b, 0, len);

                    //计算已经下载的百分比
                    int progress = 0;
                    if (contentLength != 0) {
                        progress = (int) (cacheFile.length() * 100 / contentLength);
                    }
                    if (realProgress != progress) {
                        realProgress = progress;
                        mListener.onProgress(realProgress);
                    }
                }
                response.body().close();

                //如果當前下載文件長度和遠程文件不相同，則刪除掉重新下載
                if (cacheFile.length() != contentLength) {
                    cacheFile.delete();
                    throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_LENGTH_ERROR, "cacheFile download length error");
                } else {
                    boolean rename = cacheFile.renameTo(fileName);
                    if (rename) {//改名成功
                        throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
                    } else {//改名失败
                        cacheFile.delete();//删除
                        throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED, "download success, rename cacheFile failed");
                    }
                }
            }
        }  catch (Exception e) {
            // 检测到EIO，表示找不到存储设备，停止所有请求
            if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("EIO")) {
                mListener.onError(new YunBiaoException(YunBiaoException.ERROR_STORAGE, e), currFileNum, totalNum, fileName);
                cancel();
                return;
            }

            //具体异常捕获
            if (e instanceof IOException) {//不可预知的IO异常一律认为是网络异常
                onError(new YunBiaoException(YunBiaoException.ERROR_DOWNLOAD_NET_EXCEPTION, e), totalNum, fileName);
            } else if (e instanceof DownloadException) {//流程中异常
                DownloadException downloadException = (DownloadException) e;
                switch (downloadException.errCode) {
                    case DownloadException.CODE_SUCCESS_DOWNLOAD:
                        onSuccess(totalNum, fileName);
                        break;
                    case DownloadException.CODE_FAILED_CONTENT_LENGTH:
                        onError(new YunBiaoException(YunBiaoException.FAILED_CONTENT_LENGTH, e), totalNum, fileName);
                        break;
                    case DownloadException.CODE_CACHE_DOWNLOADED_LENGTH_ERROR:
                    case DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED:
                    case DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE:
                    case DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED:
                        urlQueue.offer(downloadUrl);
                        break;
                }
            }
        } finally {
            close(is);
            close(os);
        }
        breakPointDownload(urlQueue);
    }
}
