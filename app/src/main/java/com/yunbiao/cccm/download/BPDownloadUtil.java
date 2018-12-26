package com.yunbiao.cccm.download;

import android.os.Environment;
import android.util.Log;

import com.yunbiao.cccm.common.ResourceConst;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Wei.Zhang on 2018/12/21.
 */

public class BPDownloadUtil {

    private boolean isLog = true;
    private final String TAG = getClass().getSimpleName();

    private static BPDownloadUtil instance;
    private final ScheduledExecutorService singlePool;//单线程下载

    //下载文件存放的目录
    private String directory = ResourceConst.LOCAL_RES.RES_SAVE_PATH;
    //OkHttp
    private final OkHttpClient okHttpClient;

    //请求超时时间
    private final long TIMEOUT_LONG = 30;
    //请求超时单位
    private final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    //下载时缓冲区大小
    private final int BUFFER_SIZE = 2048;
    //错误重试的延迟时间
    private long REPLAY_DELAY = 10000;

    public static BPDownloadUtil getInstance() {
        if (instance == null) {
            instance = new BPDownloadUtil();
        }
        return instance;
    }

    public BPDownloadUtil() {
        singlePool = Executors.newScheduledThreadPool(1);
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .readTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .writeTimeout(TIMEOUT_LONG, TIMEOUT_UNIT)
                .build();
    }

    public void close(){
        singlePool.shutdown();
    }

    /***
     *
     * @param fileUrlList
     * @param l
     */
    public void breakPointDownload(final List<String> fileUrlList, MutiFileDownloadListener l) {
        if (l == null) {
            l = lis;
        }

        final Queue<String> urlQueue = new LinkedList<>();
        urlQueue.addAll(fileUrlList);

        l.onBefore(urlQueue.size());

        final MutiFileDownloadListener finalL = l;
        singlePool.execute(new Runnable() {
            @Override
            public void run() {
                download(urlQueue, finalL);
            }
        });
    }

    private int currFileNum = 0;

    /***
     * 断点下载
     * @param urlQueue 下载地址的list
     * @param l 多文件下载的监听
     */
    private void download(Queue<String> urlQueue, MutiFileDownloadListener l) {
        if (urlQueue.size() <= 0) {
            l.onFinish();
            return;
        }

        //队列中取出并删除
        String downloadUrl = urlQueue.poll();

        //下载文件的名称
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));

        d("downloadUrl: "+downloadUrl);
        //获取本地目录
        File localDir = new File(directory);
        if (!localDir.exists()) {
            boolean mkdirs = localDir.mkdirs();
            if (!mkdirs) {
                l.onFailed(new Exception("create dir failed,please check permissions"));
                return;
            }
        }

        //取出本地文件的大小
        long localFileLength = 0;
        final File localFile = new File(localDir.getAbsolutePath(), fileName);
        if (localFile.exists()) {
            localFileLength = localFile.length();
        }

        //获取要下载的文件的长度（因为要计算文件的进度，所以即使本地文件不存在也应该获取）
        long contentLength = getContentLength(downloadUrl);
        d("download length: "+contentLength);
        //如果下载的文件长度为0，不下载
        if (contentLength == 0) {
            //下载失败时将该url添加回队列的尾部
            urlQueue.offer(downloadUrl);
            delayReplay(urlQueue, l);
            return;
        }

        //如果本地文件的长度和远程的相等，代表下载完成
        if (localFileLength == contentLength) {
//            onSuccess(l);
            download(urlQueue, l);
            return;
        }

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
                if (savedFile.length() != contentLength) {
                    boolean delete = localFile.delete();
                    l.onError(new Exception(localFile.getAbsolutePath() + " download failed, delete and replay"));
                    urlQueue.offer(downloadUrl);
                    delayReplay(urlQueue, l);
                    return;
                }

                onSuccess(l);
                download(urlQueue, l);
            }

        } catch (final IOException e) {
            l.onError(e);

            urlQueue.offer(downloadUrl);
            delayReplay(urlQueue, l);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
            } catch (final Exception e) {
                l.onError(e);

                urlQueue.offer(downloadUrl);
                delayReplay(urlQueue, l);
            }
        }
    }

    /***
     * 只有下载成功的时候才会增加文件索引
     * @param l
     */
    private void onSuccess(MutiFileDownloadListener l){
        d("download onSuccess...");
        l.onSuccess(currFileNum);
        currFileNum++;
    }

    //延迟下载
    private void delayReplay(final Queue<String> urlList, final MutiFileDownloadListener listener) {
        try {
            Thread.sleep(REPLAY_DELAY);
            download(urlList, listener);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private MutiFileDownloadListener lis = new MutiFileDownloadListener() {
        @Override
        public void onBefore(int totalNum) {

        }

        @Override
        public void onStart(int currNum) {

        }

        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onDownloadSpeed(long speed) {

        }

        @Override
        public void onSuccess(int currFileNum) {

        }

        @Override
        public void onError(Exception e) {

        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onFailed(Exception e) {

        }
    };

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
}
