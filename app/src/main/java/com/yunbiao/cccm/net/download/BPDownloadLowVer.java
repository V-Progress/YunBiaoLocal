package com.yunbiao.cccm.net.download;

import android.text.TextUtils;

import com.yunbiao.cccm.common.YunBiaoException;
import com.yunbiao.cccm.sd.LowVerSDController;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.net.listener.MultiFileDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
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

public class BPDownloadLowVer extends BPDownload {

    private final LowVerSDController fileOperator;

    public BPDownloadLowVer(Object tag,int tn,MultiFileDownloadListener mListener) {
        super(tag,mListener);
        totalNum = tn;
        fileOperator = LowVerSDController.instance();

//        String sdPath = PreferenceManager.getDefaultSharedPreferences(APP.getContext()).getString(SDManager.PREF_DEFAULT_URI, null);
//        fileOperator.generateStoragePath(sdPath);

        /*resourceDir = checkRootDir(localPath);
        if(resourceDir == null){
            this.mListener.onError(new YunBiaoException(YunBiaoException.ERROR_FILE_PERMISSION, null), currFileNum, totalNum, "");
            return;
        }*/
    }

    private File checkRootDir(String localPath){
        //获取本地目录
        File file = new File(localPath);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                // 文件权限
                return null;
            }
        }
        return file;
    }

    @Override
    void downloadSingle(String downloadUrl) {
        if(TextUtils.isEmpty(downloadUrl)){
            return;
        }

        if (cancel) {
            return;
        }

        d("start download...");
        mListener.onStart(currFileNum);

        //取出URL
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")).substring(1);
        String cacheFileName = "cache_" + fileName;
        d("准备下载的文件名：" + fileName);

        InputStream is = null;
        OutputStream os = null;

        try {
            //获取远程文件长度
            long contentLength = getContentLength(downloadUrl);
            d("远程文件大小：" + contentLength);
            //如果下载的文件长度为0并且重试次数未超
            if (contentLength == 0) {
                mListener.onError(null,0,0,fileName);
                return;
            }

            File localFile = fileOperator.findResource(fileName);
            if(localFile.exists()){
                //如果本地文件存在，并且大小等于远程，则下载完成，跳转下一个
                if (localFile.length() == contentLength) {
                    mListener.onSuccess(0,0,fileName);
                    return;
                } else {//存在但大小不正确，删除，重新下载
                    boolean delete = localFile.delete();
                    mListener.onError(null,0,0,fileName);
                    return;
                }
            }
            //如果本地文件不存在，创建缓存文件
            File cacheFile = fileOperator.findResource(cacheFileName);
            //如果本地文件的长度和远程的相等，代表下载完成
            if (cacheFile.length() == contentLength) {
                boolean b = cacheFile.renameTo(localFile);
                if (!b) {//改名失败
                    mListener.onError(null,0,0,fileName);
                    return;
                }
                mListener.onSuccess(0,0,fileName);
                return;
            }

            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + cacheFile.length() + "-")  //断点续传要用到的，指示下载的区间
                    .url(downloadUrl)
                    .tag(mTag)
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                os = new FileOutputStream(cacheFile,true);

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
                    mListener.onError(null,0,0,fileName);
                } else {
                    boolean rename = cacheFile.renameTo(localFile);
                    if (rename) {//改名成功
                        mListener.onSuccess(0,0,fileName);
                    } else {//改名失败
                        cacheFile.delete();//删除
                        mListener.onError(null,0,0,fileName);
                    }
                }
            }
        }  catch (Exception e) {
            mListener.onError(null,0,0,fileName);
        } finally {
            close(is);
            close(os);
        }
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

        d("start download...");
        mListener.onStart(currFileNum);

        //取出URL
        String downloadUrl = urlQueue.poll();
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")).substring(1);
        String cacheFileName = "cache_" + fileName;
        d("准备下载的文件名：" + fileName);

        InputStream is = null;
        OutputStream os = null;

        try {
            //获取远程文件长度
            long contentLength = getContentLength(downloadUrl);
            d("远程文件大小：" + contentLength);
            //如果下载的文件长度为0并且重试次数未超
            if (contentLength == 0) {
                throw new DownloadException(DownloadException.CODE_FAILED_CONTENT_LENGTH, "Get File's Length Error");
            }

            File localFile = fileOperator.findResource(fileName);
            if(localFile.exists()){
                //如果本地文件存在，并且大小等于远程，则下载完成，跳转下一个
                if (localFile.length() == contentLength) {
                    throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
                } else {//存在但大小不正确，删除，重新下载
                    boolean delete = localFile.delete();
                    if (!delete) {
                        throw new DownloadException(DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED, "local file's length error, delete failed");
                    }

                    throw new DownloadException(DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE, "local file's length error, delete");
                }
            }
            //如果本地文件不存在，创建缓存文件
            File cacheFile = fileOperator.findResource(cacheFileName);
            //如果本地文件的长度和远程的相等，代表下载完成
            if (cacheFile.length() == contentLength) {
                boolean b = cacheFile.renameTo(localFile);
                if (!b) {//改名失败
                    throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED, "download success, rename cacheFile failed");
                }

                throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
            }

            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + cacheFile.length() + "-")  //断点续传要用到的，指示下载的区间
                    .url(downloadUrl)
                    .tag(mTag)
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                os = new FileOutputStream(cacheFile,true);

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
                    boolean rename = cacheFile.renameTo(localFile);
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
