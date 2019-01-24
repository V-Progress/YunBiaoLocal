package com.yunbiao.cccm.resource;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;

import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.common.YunBiaoException;
import com.yunbiao.cccm.utils.DateUtil;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 以队列形式实现
 * 先对文件进行比对,相同大小的视为下载完成,不同大小的保存信息,
 * <p>
 * Created by Wei.Zhang on 2018/12/21.
 */

public class BPDownloadUtil {

    //是否打印log
    private boolean isLog = true;
    //log的TAG
    private final String TAG = getClass().getSimpleName();

    //下载文件存放的目录
    private String directory = ResourceConst.LOCAL_RES.RES_SAVE_PATH;
    //OkHttp
    private final OkHttpClient okHttpClient;
    //下载线程
    private Thread downloadThread;

    //请求超时时间
    private final long TIMEOUT_LONG = 30;
    //请求超时单位
    private final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
    //下载时缓冲区大小
    private final int BUFFER_SIZE = 4096;

    //下载标识
    private Object mTag;
    //取消下载标识
    private boolean cancel = false;
    //当前文件标识
    private int currFileNum = 1;

    //多文件下载监听
    private MultiFileDownloadListener l;

    private int totalNum;


    public BPDownloadUtil(Object tag, FileDownloadListener fileDownloadListener) {
        if (fileDownloadListener == null) {
            l = new FileDownloadListener() {

            };
        } else {
            l = fileDownloadListener;
        }

        mTag = tag;
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .readTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .writeTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .build();
    }

    /***
     * 断点下载
     * @param fileUrlList
     */
    public void breakPointDownload(final List<String> fileUrlList) {
        downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                breakPointDownload(directory, fileUrlList);
            }
        });
        downloadThread.start();
    }

    /***
     * 取消当前下载
     */
    public void cancel() {
        cancel = true;
        cancelTag(okHttpClient, mTag);
    }

    private void breakPointDownload(String localPath, final List<String> fileUrlList) {
        if (l == null) {
            l = new FileDownloadListener() {
            };
        }

        //获取本地目录
        File localDir = new File(localPath);
        if (!localDir.exists()) {
            boolean mkdirs = localDir.mkdirs();
            if (!mkdirs) {
                l.onFailed(new YunBiaoException(YunBiaoException.ERROR_FILE_PERMISSION, null));//文件权限异常
                return;
            }
        }

        //初始化一个队列，便于递归使用
        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.addAll(fileUrlList);

        //下载之前先调用一下before
        totalNum = urlQueue.size();
        l.onBefore(totalNum);

        //开始下载
        cacheDownload(localDir.getAbsolutePath(), urlQueue);
    }

    /***
     * 断点下载（缓存类，先下载缓存文件，待缓存文件下载完成后，大小一致修改名称，大小不一致删掉重下）
     */
    private void cacheDownload(String localPath, Queue<String> urlQueue) {
        if (urlQueue.size() <= 0) {
            l.onFinish();
            return;
        }

        if (cancel) {
            return;
        }

        //取出URL
        String downloadUrl = urlQueue.poll();
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")).substring(1);

        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {
            //获取要下载的文件的长度（因为要计算文件的进度，所以即使本地文件不存在也应该获取）
            long contentLength = getContentLength(downloadUrl);
            //如果下载的文件长度为0并且重试次数未超
            if (contentLength == 0) {
                throw new DownloadException(DownloadException.CODE_FAILED_CONTENT_LENGTH, "Get File's Length Error");
            }

            //取出本地文件（确保本地文件存在即完整）
            final File localFile = new File(localPath, fileName);
            if (localFile.exists()) {
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
            File cacheFile = new File(localPath, "cache_" + fileName);
            //如果本地文件的长度和远程的相等，代表下载完成
            if (cacheFile.length() == contentLength) {
                boolean b = cacheFile.renameTo(localFile);
                if (!b) {//改名失败
                    throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED, "download success, rename cacheFile failed");
                }

                throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
            }

            d("start download...");
            l.onStart(currFileNum);

            /**
             * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
             * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
             * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
             * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
             *
             * HttpServletResponse respresp.setHeader("Content-Length", ""+file.length());
             */
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + cacheFile.length() + "-")  //断点续传要用到的，指示下载的区间
                    .url(downloadUrl)
                    .tag(mTag)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(cacheFile, "rw");
                savedFile.seek(cacheFile.length());//跳过已经下载的字节

                int realProgress = 0;

                byte[] b = new byte[BUFFER_SIZE];
                int len;
                while ((len = is.read(b)) != -1) {
                    l.onDownloadSpeed(len);

                    if (cancel) {
                        return;
                    }

                    savedFile.write(b, 0, len);

                    //计算已经下载的百分比
                    int progress = 0;
                    if (contentLength != 0) {
                        progress = (int) (cacheFile.length() * 100 / contentLength);
                    }
                    if (realProgress != progress) {
                        realProgress = progress;
                        l.onProgress(realProgress);
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
        } catch (DownloadException e) {
            switch (e.errCode) {
                case DownloadException.CODE_SUCCESS_DOWNLOAD:
                    onSuccess(totalNum, fileName);
                    break;
                case DownloadException.CODE_FAILED_CONTENT_LENGTH:
                    onError(e, totalNum, fileName);
                    break;
                case DownloadException.CODE_CACHE_DOWNLOADED_LENGTH_ERROR:
                case DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED:
                case DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE:
                case DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED:
                    urlQueue.offer(downloadUrl);
                    break;
            }
        } catch (final IOException e) {
//            e.printStackTrace();
            LogUtil.E(e.getMessage());
            // TODO: 2019/1/21 检测到EIO，表示找不到存储设备，停止所有请求
            if (TextUtils.equals("EIO", e.getMessage())) {
                l.onFailed(new YunBiaoException(YunBiaoException.ERROR_STORAGE,e));
                cancel();
                return;
            }
            if (e instanceof SocketTimeoutException) {
                urlQueue.offer(downloadUrl);
            }
            onError(e, totalNum, fileName);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
            } catch (final Exception e) {
                onError(e, totalNum, fileName);
            }
        }
        cacheDownload(localPath, urlQueue);
    }

    /***
     * 只有下载成功的时候才会增加文件索引
     */
    private void onSuccess(int totalNum, String fileName) {
        d("download onSuccess...");
        l.onSuccess(currFileNum, totalNum, fileName);
        currFileNum++;
    }

    private void onError(Exception e, int totalNum, String fileName) {
        d("download onError...");
        l.onError(e, currFileNum, totalNum, fileName);
        currFileNum++;
    }

    /**
     * 得到下载内容的大小
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
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
        }
        return 0;
    }

    private void d(String msg) {
        if (isLog) {
            Log.d(TAG, msg);
        }
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

    public static class DownloadException extends Exception {
        public static final int CODE_FAILED_CONTENT_LENGTH = -1;//获取远程文件大小失败(无需重试)
        public static final int CODE_SUCCESS_DOWNLOAD = 0;//下载成功(无需重试)
        public static final int CODE_CACHE_DOWNLOADED_RENAME_FAILED = 1;//缓存文件下载完成，更名失败(重试)
        public static final int CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED = 2;//存在但大小不正确，删除本地文件失败(重试)
        public static final int CODE_LOCAL_LENGTH_ERROR_DELETE = 3;//存在但大小不正确，删除本地文件成功(重试)
        public static final int CODE_CACHE_DOWNLOADED_LENGTH_ERROR = 4;//缓存下载完成后大小不一致，删除重新下载(重试)


        private int errCode;
        private String errMsg;

        @Override
        public String getMessage() {
            return errMsg;
        }

        public DownloadException(int errCode, String errMsg) {
            this.errCode = errCode;
            this.errMsg = errMsg;
        }
    }

    /*public void download(Context context, String sdPath, Queue<String> urlQueue, FileDownloadListener fileDownloadListener) {
        if (urlQueue.size() <= 0) {
            fileDownloadListener.onFinish();
            return;
        }

        if (cancel) {
            return;
        }

        //获取url
        String downloadUrl = urlQueue.poll();
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")).substring(1);

        InputStream is = null;
        OutputStream os = null;
        try {
            long contentLength = getContentLength(downloadUrl);
            //如果下载的文件长度为0并且重试次数未超
            if (contentLength == 0) {
                throw new DownloadException(DownloadException.CODE_FAILED_CONTENT_LENGTH, "Get File's Length Error");
            }

            //文件路径
            Uri fileUri = Uri.parse(sdPath + "/" + fileName);

            //判断uri是否符合标准
            boolean documentUri = DocumentFile.isDocumentUri(context, fileUri);
            if (documentUri) {
                throw new DownloadException(7, "File Uri Error,Please Check:" + fileUri);
            }

            DocumentFile localFile = DocumentFile.fromTreeUri(context, fileUri);
            //判断文件是否存在
            if (localFile.exists()) {
                if (localFile.length() == contentLength) {
                    throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
                } else {
                    boolean delete = localFile.delete();
                    if (!delete) {
                        throw new DownloadException(DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED, "local file's length error, delete failed");
                    }
                    throw new DownloadException(DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE, "local file's length error, delete");
                }
            }

            //重新创建缓存文件的uri
            fileUri = Uri.parse(sdPath + "/" + "cache_" + fileName);
            DocumentFile cacheFile = DocumentFile.fromTreeUri(context, fileUri);
            if (!cacheFile.exists()) {
                cacheFile = cacheFile.createFile(cacheFile.getType(), cacheFile.getName());
            }

            if (cacheFile.length() == contentLength) {
                boolean b = cacheFile.renameTo(localFile.getName());
                if (!b) {//改名失败
                    throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED, "download success, rename cacheFile failed");
                }

                throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
            }

            *//**
     * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
     * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
     * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
     * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
     *
     * HttpServletResponse respresp.setHeader("Content-Length", ""+file.length());
     *//*
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + cacheFile.length() + "-")  //断点续传要用到的，指示下载的区间
                    .url(downloadUrl)
                    .tag(this)
                    .build();
            Response response = new OkHttpClient().newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                os = context.getContentResolver().openOutputStream(cacheFile.getUri());

                int realProgress = 0;

                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    l.onDownloadSpeed(len);
                    if (cancel) {
                        return;
                    }
                    os.write(buffer, 0, len);

                    int progress = 0;
                    if (contentLength != 0) {
                        progress = (int) (cacheFile.length() * 100 / contentLength);
                    }
                    if (realProgress != progress) {
                        realProgress = progress;
                        l.onProgress(realProgress);
                    }
                }

                response.body().close();

                if (cacheFile.length() != contentLength) {
                    cacheFile.delete();
                    throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_LENGTH_ERROR, "cacheFile download length error");
                } else {
                    boolean rename = cacheFile.renameTo(localFile.getName());
                    if (rename) {//改名成功
                        throw new DownloadException(DownloadException.CODE_SUCCESS_DOWNLOAD, DateUtil.yyyy_MM_dd_HH_mm_Format(new Date()));
                    } else {//改名失败
                        cacheFile.delete();//删除
                        throw new DownloadException(DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED, "download success, rename cacheFile failed");
                    }
                }
            }

        } catch (DownloadException e) {
            switch (e.errCode) {
                case DownloadException.CODE_SUCCESS_DOWNLOAD:
                    onSuccess(totalNum, fileName);
                    break;
                case DownloadException.CODE_FAILED_CONTENT_LENGTH:
                    onError(e, totalNum, fileName);
                    break;
                case DownloadException.CODE_CACHE_DOWNLOADED_LENGTH_ERROR:
                case DownloadException.CODE_CACHE_DOWNLOADED_RENAME_FAILED:
                case DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE:
                case DownloadException.CODE_LOCAL_LENGTH_ERROR_DELETE_FAILED:
                    urlQueue.offer(downloadUrl);
                    break;
                default:
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            onError(e, totalNum, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (final Exception e) {
                onError(e, totalNum, fileName);
            }
        }
        download(context, sdPath, urlQueue, fileDownloadListener);
    }*/

}
