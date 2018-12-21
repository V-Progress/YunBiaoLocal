package com.yunbiao.yunbiaolocal.netcore;

import android.os.Environment;

import com.yunbiao.yunbiaolocal.common.Const;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/12/21.
 */

public class BPDownloadUtil {

    private static BPDownloadUtil instance;
    private static ExecutorService singlePool;
//    private static int currFileNum;
    private static int mProgress;
    //下载文件存放的目录
    private String directory = ResourceConst.LOCAL_RES.RES_SAVE_PATH;

    public static BPDownloadUtil getInstance() {
        if (instance == null) {
            instance = new BPDownloadUtil();
        }
        return instance;
    }

    public BPDownloadUtil() {
        singlePool = Executors.newSingleThreadExecutor();
    }

    public void breakPointDownload(final List<String> fileUrlList, final MutiFileDownloadListener l) {
        if (l != null) {
            l.onBefore(fileUrlList.size());
        }

//        for (int i = 0; i < fileUrlList.size(); i++) {
//            currFileNum = i + 1;
//            if (l != null) {
//                l.onProgress(currFileNum, 0);//下载开始之前progress为0
//            }
//            final String url = fileUrlList.get(i);
//            LogUtil.D("123", "当前下载地址为：" + url);
//            singlePool.execute(new Runnable() {
//                @Override
//                public void run() {
//                    breakDownload(url,l);
//                }
//            });
//
//            if (i == fileUrlList.size() - 1) {
//                l.onFinish();
//            }
//        }

        singlePool.execute(new Runnable() {
            @Override
            public void run() {
                dgDownload(fileUrlList,l);
            }
        });
    }

    private int currFileNum = 0;

    private void dgDownload(List<String> urlList,MutiFileDownloadListener l){
        String url = urlList.get(0);
        currFileNum++;
        if (l != null) {
            l.onProgress(currFileNum, 0);//下载开始之前progress为0
        }

        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        long downloadLength = 0;   //记录已经下载的文件长度

        //文件下载地址
        String downloadUrl = url;

        //下载文件的名称
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));

        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //创建一个文件
        file = new File(directory + fileName);
        if (file.exists()) {
            //如果文件存在的话，得到文件的大小
            downloadLength = file.length();
        }

        //得到下载内容的大小
        long contentLength = getContentLength(downloadUrl);
        LogUtil.E("123","当前下载文件的长度：" + contentLength);
        if (contentLength == 0) {
            if (l != null) {
                l.onError(new Exception("download Failed"));
            }
        } else if (contentLength == downloadLength) {
            //已下载字节和文件总字节相等，说明已经下载完成了
            if (l != null) {
                l.onSuccess(currFileNum);
                urlList.remove(0);
                dgDownload(urlList,l);
            }
        }

        OkHttpClient client = new OkHttpClient();
        /**
         * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
         * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
         * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
         * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
         *
         * HttpServletResponse respresp.setHeader("Content-Length", ""+file.length());
         */
        Request request = new Request.Builder()
                .addHeader("RANGE", "bytes=" + downloadLength + "-")  //断点续传要用到的，指示下载的区间
//                            .addHeader("Content-Length", "bytes=" + downloadLength + "-")
                .url(downloadUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadLength);//跳过已经下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    total += len;
                    savedFile.write(b, 0, len);
                    //计算已经下载的百分比
                    int progress = 0;
                    if (contentLength != 0) {
                        progress = (int) ((total + downloadLength) * 100 / contentLength);
                    }
                    if (mProgress != progress) {
                        mProgress = progress;
                        if (l != null) {
                            l.onProgress(currFileNum, mProgress);
                        }
                    }
                    //注意：在doInBackground()中是不可以进行UI操作的，如果需要更新UI,比如说反馈当前任务的执行进度，
                    //可以调用publishProgress()方法完成。

                }
                response.body().close();
            }
            l.onSuccess(currFileNum);
            urlList.remove(0);
            dgDownload(urlList,l);
        } catch (IOException e) {
            e.printStackTrace();
            if (l != null) {
                l.onError(e);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                l.onError(e);
            }
        }
    }

//    private synchronized void  breakDownload(String url, MutiFileDownloadListener l) {
////        synchronized (BPDownloadUtil.class) {
//            InputStream is = null;
//            RandomAccessFile savedFile = null;
//            File file = null;
//            long downloadLength = 0;   //记录已经下载的文件长度
//
//            //文件下载地址
//            String downloadUrl = url;
//
//            //下载文件的名称
//            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
//
//            //下载文件存放的目录
//            String directory = ResourceConst.LOCAL_RES.RES_SAVE_PATH;
//            File dir = new File(directory);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            //创建一个文件
//            file = new File(directory + fileName);
//            if (file.exists()) {
//                //如果文件存在的话，得到文件的大小
//                downloadLength = file.length();
//            }
//
//            //得到下载内容的大小
//            long contentLength = getContentLength(downloadUrl);
//            LogUtil.E("当前下载文件的长度：" + contentLength);
//            if (contentLength == 0) {
//                if (l != null) {
//                    l.onError(new Exception("download Failed"));
//                }
//            } else if (contentLength == downloadLength) {
//                //已下载字节和文件总字节相等，说明已经下载完成了
//                if (l != null) {
//                    l.onSuccess(currFileNum);
//                    notify();
//                }
//            }
//
//            OkHttpClient client = new OkHttpClient();
//            /**
//             * HTTP请求是有一个Header的，里面有个Range属性是定义下载区域的，它接收的值是一个区间范围，
//             * 比如：Range:bytes=0-10000。这样我们就可以按照一定的规则，将一个大文件拆分为若干很小的部分，
//             * 然后分批次的下载，每个小块下载完成之后，再合并到文件中；这样即使下载中断了，重新下载时，
//             * 也可以通过文件的字节长度来判断下载的起始点，然后重启断点续传的过程，直到最后完成下载过程。
//             *
//             * HttpServletResponse respresp.setHeader("Content-Length", ""+file.length());
//             */
//            Request request = new Request.Builder()
//                    .addHeader("RANGE", "bytes=" + downloadLength + "-")  //断点续传要用到的，指示下载的区间
////                            .addHeader("Content-Length", "bytes=" + downloadLength + "-")
//                    .url(downloadUrl)
//                    .build();
//            try {
//                Response response = client.newCall(request).execute();
//                if (response != null) {
//                    is = response.body().byteStream();
//                    savedFile = new RandomAccessFile(file, "rw");
//                    savedFile.seek(downloadLength);//跳过已经下载的字节
//                    byte[] b = new byte[1024];
//                    int total = 0;
//                    int len;
//                    while ((len = is.read(b)) != -1) {
//                        total += len;
//                        savedFile.write(b, 0, len);
//                        //计算已经下载的百分比
//                        int progress = 0;
//                        if (contentLength != 0) {
//                            progress = (int) ((total + downloadLength) * 100 / contentLength);
//                        }
//                        if (mProgress != progress) {
//                            mProgress = progress;
//                            if (l != null) {
//                                l.onProgress(currFileNum, mProgress);
//                            }
//                        }
//                        //注意：在doInBackground()中是不可以进行UI操作的，如果需要更新UI,比如说反馈当前任务的执行进度，
//                        //可以调用publishProgress()方法完成。
//
//                    }
//                    response.body().close();
//                }
//                l.onSuccess(currFileNum);
//                notify();
//            } catch (IOException e) {
//                e.printStackTrace();
//                if (l != null) {
//                    l.onError(e);
//                }
//            } finally {
//                try {
//                    if (is != null) {
//                        is.close();
//                    }
//                    if (savedFile != null) {
//                        savedFile.close();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    l.onError(e);
//                }
//            }
////        }
//    }

    /**
     * 得到下载内容的大小
     *
     * @param downloadUrl
     * @return
     */
    private long getContentLength(String downloadUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        try {
            Response response = client.newCall(request).execute();
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

    public interface MutiFileDownloadListener {
        void onBefore(int totalNum);

        void onProgress(int currNum, int progress);

        void onSuccess(int currFileNum);

        void onFinish();

        void onError(Exception e);
    }
}
