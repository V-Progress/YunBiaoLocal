package com.yunbiao.cccm.download;

import android.util.Log;

import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
    private final int BUFFER_SIZE = 2048;

    //下载标识
    private Object mTag;
    //取消下载标识
    private boolean cancel = false;
    //当前文件标识
    private int currFileNum = 1;

    private MultiFileDownloadListener l;

    public BPDownloadUtil(Object tag, FileDownloadListener fileDownloadListener) {
        if (fileDownloadListener == null) {
            l = new FileDownloadListener() {};
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
            l = new FileDownloadListener() {};
        }

        //获取本地目录
        File localDir = new File(localPath);
        if (!localDir.exists()) {
            boolean mkdirs = localDir.mkdirs();
            if (!mkdirs) {
                l.onFailed(new Exception("create dir failed,please check permissions"));
                return;
            }
        }

        //初始化一个队列，便于递归使用
        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.addAll(fileUrlList);

        //下载之前先调用一下before
        l.onBefore(urlQueue.size());

        //真正的下载地址队列
        Queue<DownloadInfo> downloadInfos = new LinkedList<>();
        equalsFile(localDir.getAbsolutePath(), urlQueue, downloadInfos);

        //开始下载
        download(downloadInfos);
    }

    private void equalsFile(String localPath, Queue<String> urlQueue, Queue<DownloadInfo> downloadInfos) {
        if (urlQueue.size() <= 0) {
            return;
        }

        String downloadUrl = urlQueue.poll();
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/")).substring(1);

        //取出本地文件的大小
        long localFileLength = 0;
        final File localFile = new File(localPath, fileName);
        if (localFile.exists()) {
            localFileLength = localFile.length();
        }

        //获取要下载的文件的长度（因为要计算文件的进度，所以即使本地文件不存在也应该获取）
        long contentLength = getContentLength(downloadUrl);

        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setUrl(downloadUrl);
        downloadInfo.setContentLength(contentLength);
        downloadInfo.setLocalFileLength(localFileLength);
        downloadInfo.setLocalFile(localFile);
        downloadInfo.setFileName(fileName);

        //如果下载的文件长度为0并且重试次数未超
        if (contentLength == 0) {
            LogUtil.E(TAG, "获取文件大小失败，重试");

            onError(new Exception("Get File's Length Error"),downloadInfo);
            equalsFile(localPath, urlQueue, downloadInfos);
            return;
        }

        //如果本地文件的长度和远程的相等，代表下载完成
        if (localFileLength == contentLength) {
            onSuccess(downloadInfo);
            equalsFile(localPath, urlQueue, downloadInfos);
            return;
        }

        downloadInfos.add(downloadInfo);
        equalsFile(localPath, urlQueue, downloadInfos);
    }

    /***
     * 断点下载
     * @param downloadInfos 下载地址的list
     */
    private void download(Queue<DownloadInfo> downloadInfos) {
        if (downloadInfos.size() <= 0) {
            l.onFinish();
            return;
        }

        if (cancel) {
            return;
        }

        DownloadInfo downloadInfo = downloadInfos.poll();
        String downloadUrl = downloadInfo.getUrl();
        long localFileLength = downloadInfo.getLocalFileLength();
        long contentLength = downloadInfo.getContentLength();
        File localFile = downloadInfo.getLocalFile();

        d("start download...");
        l.onStart(currFileNum);

        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {
            /**
             * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
             * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
             * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
             * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
             *
             * HttpServletResponse respresp.setHeader("Content-Length", ""+file.length());
             */
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + localFileLength + "-")  //断点续传要用到的，指示下载的区间
                    .url(downloadUrl)
                    .tag(mTag)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(localFile, "rw");
                savedFile.seek(localFileLength);//跳过已经下载的字节

                int realProgress = 0;

                byte[] b = new byte[BUFFER_SIZE];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    l.onDownloadSpeed(len);

                    if (cancel) {
                        return;
                    }

                    total += len;
                    savedFile.write(b, 0, len);

                    //计算已经下载的百分比
                    int progress = 0;
                    if (contentLength != 0) {
                        progress = (int) ((total + localFileLength) * 100 / contentLength);
                    }
                    if (realProgress != progress) {
                        realProgress = progress;
                        l.onProgress(realProgress);
                    }
                }
                response.body().close();

                //如果當前下載文件長度和遠程文件不相同，則刪除掉重新下載

                if (localFile.length() != contentLength) {
                    boolean delete = localFile.delete();
                    downloadInfos.offer(downloadInfo);
                } else {
                    onSuccess(downloadInfo);
                }
            }
        } catch (final IOException e) {
            onError(e, downloadInfo);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
            } catch (final Exception e) {
                onError(e,downloadInfo);
            }
        }
        download(downloadInfos);
    }

    /***
     * 只有下载成功的时候才会增加文件索引
     */
    private void onSuccess(DownloadInfo downloadInfo) {
        d("download onSuccess...");
        l.onSuccess(currFileNum, downloadInfo);
        currFileNum++;
    }

    private void onError(Exception e,DownloadInfo downloadInfo){
        d("download onError...");
        l.onError(currFileNum,e,downloadInfo);
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

    public class DownloadInfo {
        private String url;
        private long contentLength;
        private long localFileLength;
        private File localFile;
        private String fileName;

        public File getLocalFile() {
            return localFile;
        }

        public void setLocalFile(File localFile) {
            this.localFile = localFile;
        }

        public long getLocalFileLength() {
            return localFileLength;
        }

        public void setLocalFileLength(long localFileLength) {
            this.localFileLength = localFileLength;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public long getContentLength() {
            return contentLength;
        }

        public void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

}
