package com.yunbiao.cccm.net2;

import android.os.FileUtils;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.yunbiao.cccm.PathManager;
import com.yunbiao.cccm.net2.db.Daily;
import com.yunbiao.cccm.net2.db.DaoManager;
import com.yunbiao.cccm.net2.db.ItemBlock;
import com.yunbiao.cccm.net2.db.TimeSlot;
import com.yunbiao.cccm.net2.utils.DateUtil;
import com.yunbiao.cccm.net2.utils.NetUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;

/**
 * 优先下载今天的，如果今天的没有再检测明天的，明天的完事再检测之后的
 * Created by Administrator on 2019/11/19.
 */

public class Downloader {
    private static final String TAG = "NewDownloader";
    private static Downloader downloader = new Downloader();
    private ScheduledExecutorService scheduledExecutorService;
    private int REQ_DAY_NUM = 7;
    private final long dayMilliSec = 24 * 60 * 60 * 1000;
    private Queue<String> dateQueue = new LinkedList<>();

    private Downloader() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    public static Downloader getInstance() {
        return downloader;
    }

    /***
     * 自动检查之后的数据
     */
    public void autoCheck() {
        Date todayDate = DateUtil.getTodayDate();
        for (int i = 0; i < REQ_DAY_NUM; i++) {
            if (i <= 1) {
                continue;
            }
            String date = DateUtil.yyyy_MM_dd_Format(new Date(todayDate.getTime() + (dayMilliSec * i)));
            dateQueue.offer(date);
        }
        checkDateList();
    }

    /***
     * 自动检查dateList
     */
    public void checkDateList() {
        if (dateQueue == null || dateQueue.size() <= 0) {
            return;
        }
        String poll = dateQueue.poll();
        Daily daily = DaoManager.get().queryByDate(poll);
        //如果没有当日的数据则跳过
        if (daily == null) {
            checkDateList();
            return;
        }
        //否则开始下载
        check(poll, new AutoLogDownListener() {
            @Override
            public void onFinished(String date) {
                super.onFinished(date);
                checkDateList();
            }
        });
    }

    /***
     * 检查明天的
     * @param multiDownloadListener
     */
    public void checkTomm(final MultiDownloadListener multiDownloadListener) {
        check(DateUtil.getTomm_str(), multiDownloadListener);
    }

    /***
     * 检查某个日期的
     * @param date
     * @param multiDownloadListener
     */
    public void check(final String date, final MultiDownloadListener multiDownloadListener) {
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                Daily daily = DaoManager.get().queryByDate(date);

                List<ItemBlock> readyList = new ArrayList<>();
                Queue<ItemBlock> downloadQueue = new LinkedList<>();

                if (daily != null) {
                    List<TimeSlot> timeSlots = daily.getTimeSlots();
                    for (TimeSlot timeSlot : timeSlots) {
                        List<ItemBlock> itemBlocks = timeSlot.getItemBlocks();
                        for (ItemBlock itemBlock : itemBlocks) {
                            if (SystemVersion.isLowVer()) {
                                File resFileDir = PathManager.instance().getResFileDir();
                                File file = new File(resFileDir, itemBlock.getName());
                                if (file != null && file.exists()) {
                                    Log.e(TAG, "checkToday: " + itemBlock.getName() + " ----- " + (file != null && file.exists() ? "已存在" : "不存在"));
                                    readyList.add(itemBlock);
                                    continue;
                                }
                            } else {
                                DocumentFile file = PathManager.instance().getResDocFileDir().findFile(itemBlock.getName());
                                if (file != null && file.exists()) {
                                    Log.e(TAG, "checkToday: " + itemBlock.getName() + " ----- " + (file != null && file.exists() ? "已存在" : "不存在"));
                                    readyList.add(itemBlock);
                                    continue;
                                }
                            }

                            downloadQueue.offer(itemBlock);
                        }
                    }
                }
                if (multiDownloadListener != null) {
                    multiDownloadListener.onReadyProgram(daily.getDate(), readyList.size() > 0);
                }

                if (multiDownloadListener != null) {
                    multiDownloadListener.onStart(daily.getDate(), downloadQueue.size());
                }

                //重试次数设置为3倍大小
                mRetryNum = downloadQueue.size() * 2;

                downloadQueue(daily.getDate(), downloadQueue, multiDownloadListener);
            }
        }, 3, TimeUnit.SECONDS);
    }

    private int mRetryNum = 0;

    private void downloadQueue(String date, Queue<ItemBlock> itemBlockQueue, MultiDownloadListener downloadListener) {
        if (itemBlockQueue.size() <= 0) {
            if (downloadListener != null) {
                downloadListener.onFinished(date);
            }
            return;
        }

        ItemBlock poll = itemBlockQueue.poll();
        if (downloadListener != null) {
            downloadListener.onSingleStart(poll, itemBlockQueue.size());
        }

        Log.e(TAG, "downloadQueue: 当前下载地址：" + poll.getUrl());

        DownloadResponse response;
        if (SystemVersion.isLowVer()) {
            response = download_l(poll, downloadListener);
        } else {
            response = download_h(poll, downloadListener);
        }

        if (response.resultCode <= 0) {
            Log.e(TAG, "downloadQueue: 返回值：" + response.resultCode);
            if (mRetryNum > 0) {
                mRetryNum--;
                itemBlockQueue.offer(poll);
            }
            if (downloadListener != null) {
                downloadListener.onFailed(poll, response.exception);
            }
        } else {
            if (downloadListener != null) {
                downloadListener.onComplete(poll);
            }
        }

        downloadQueue(date, itemBlockQueue, downloadListener);
    }

    private final static int FLAG_COMPLETE = 1;
    private final static int FLAG_GET_LENGTH_FAILED = -1;
    private final static int FLAG_GET_FILE_FAILED = -2;
    private final static int FLAG_DOWNLOAD_EXCEPTION_FILE_NOT_FOUND = -3;
    private final static int FLAG_DOWNLOAD_EXCEPTION_IO = -4;

    public DownloadResponse download_l(ItemBlock itemBlock, MultiDownloadListener listener) {
        return download_l(itemBlock.getUrl(), itemBlock.getName(), listener);
    }

    public DownloadResponse download_h(ItemBlock itemBlock, MultiDownloadListener listener) {
        return download_h(itemBlock.getUrl(), itemBlock.getName(), listener);
    }

    public DownloadResponse download_l(String url, String name, MultiDownloadListener listener) {
        Log.e(TAG, "download_l: ---------------------------------");
        Log.e(TAG, "download_l: 开始下载");
        Log.e(TAG, "download_l: 请求地址：" + url);

        long fileLength = getFileLength(url, 3);
        Log.e(TAG, "download_l: 获取文件长度：" + fileLength);
        if (fileLength == -1) {
            return new DownloadResponse(FLAG_GET_LENGTH_FAILED, new Exception("Failed to get file length"));
        }

        //检查缓存文件是否存在
        File resFileDir = PathManager.instance().getResFileDir();
        File file = new File(resFileDir, name);
        Log.e(TAG, "download_l: 资源文件地址：" + file.getPath() + " ---是否存在： " + file.exists());
        if (file != null && file.exists()) {
            return new DownloadResponse(FLAG_COMPLETE);
        }

        String cacheName = "cache_" + name;
        File cacheFile = new File(resFileDir, cacheName);
        Log.e(TAG, "download_l: 缓存文件地址：" + cacheFile.getPath() + " ---是否存在： " + cacheFile.exists());
        if (cacheFile == null || !cacheFile.exists()) {
            try {
                cacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (cacheFile.length() == fileLength) {
            cacheFile.renameTo(file);
            return new DownloadResponse(FLAG_COMPLETE);
        }

        Response response = getFile(url, 3, "RANGE", "bytes=" + cacheFile.length() + "-");
        if (response == null) {
            Log.e(TAG, "download_l: 获取文件错误");
            return new DownloadResponse(FLAG_GET_FILE_FAILED, new Exception("Failed to get file"));
        }
        Log.e(TAG, "download_l: 获取文件结果：" + response.code());

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            InputStream is = response.body().byteStream();
            bis = new BufferedInputStream(is);
            OutputStream os = new FileOutputStream(cacheFile, true);
            bos = new BufferedOutputStream(os);
            byte[] bytes = new byte[2048];
            int length = 0;
            int realProgress = 0;
            while ((length = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);

                if (listener != null) {
                    //计算已经下载的百分比
                    int progress = 0;
                    if (fileLength != 0) {
                        progress = (int) (cacheFile.length() * 100 / fileLength);
                    }
                    if (realProgress != progress) {
                        realProgress = progress;
                        listener.onProgress(realProgress);
                    }
                }
            }
            cacheFile.renameTo(file);
            return new DownloadResponse(FLAG_COMPLETE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new DownloadResponse(FLAG_DOWNLOAD_EXCEPTION_FILE_NOT_FOUND, e);
        } catch (IOException e) {
            e.printStackTrace();
            return new DownloadResponse(FLAG_DOWNLOAD_EXCEPTION_IO, e);
        } finally {
            response.body().close();
            close(bis);
            close(bos);
        }
    }

    public DownloadResponse download_h(String url, String name, MultiDownloadListener listener) {
        Log.e(TAG, "download_h: ---------------------------------");
        Log.e(TAG, "download_h: 开始下载");
        Log.e(TAG, "download_h: 请求地址：" + url);

        //获取文件长度，如果为-1代表3次获取失败
        long fileLength = getFileLength(url, 3);
        Log.e(TAG, "download_h: 获取文件长度：" + fileLength);
        if (fileLength == -1) {
            //下载失败
            return new DownloadResponse(FLAG_GET_LENGTH_FAILED, new Exception("Failed to get file length"));
        }

        DocumentFile resDocFileDir = PathManager.instance().getResDocFileDir();
        DocumentFile file = resDocFileDir.findFile(name);
        Log.e(TAG, "download_h: 资源文件地址：" + (file == null ? "NULL" : file.getUri().getPath() + " ---是否存在： " + file.exists()));
        //如果存在就是已下载完毕
        if (file != null && file.exists()) {
            //下载失败
            return new DownloadResponse(FLAG_COMPLETE);
        }

        //取出缓存文件名
        String cacheName = "cache_" + name;
        DocumentFile cacheFile = resDocFileDir.findFile(cacheName);
        //如果缓存不存在就创建
        if (cacheFile == null || !cacheFile.exists()) {
            cacheFile = resDocFileDir.createFile("video/x-msvideo", cacheName);
        }
        Log.e(TAG, "download_h: 缓存文件地址：" + (cacheFile == null ? "NULL" : cacheFile.getUri().getPath() + " ---是否存在： " + cacheFile.exists()));

        if (cacheFile.length() == fileLength) {
            cacheFile.renameTo(name);
            return new DownloadResponse(FLAG_COMPLETE);
        }

        Response response = getFile(url, 3, "RANGE", "bytes=" + cacheFile.length() + "-");
        if (response == null) {
            Log.e(TAG, "download_l: 获取文件错误");
            return new DownloadResponse(FLAG_GET_FILE_FAILED, new Exception("Failed to get file"));
        }
        Log.e(TAG, "download_l: 获取文件结果：" + response.code());

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            InputStream is = response.body().byteStream();
            bis = new BufferedInputStream(is);
            OutputStream os = PathManager.instance().openOutputStream(cacheFile.getUri());
            bos = new BufferedOutputStream(os);

            byte[] bytes = new byte[2048];
            int length = 0;

            int realProgress = 0;

            while ((length = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, length);

                if (listener != null) {
                    //计算已经下载的百分比
                    int progress = 0;
                    if (fileLength != 0) {
                        progress = (int) (cacheFile.length() * 100 / fileLength);
                    }
                    if (realProgress != progress) {
                        realProgress = progress;
                        listener.onProgress(realProgress);
                    }
                }
            }
            bos.flush();
            cacheFile.renameTo(name);
            return new DownloadResponse(FLAG_COMPLETE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new DownloadResponse(FLAG_DOWNLOAD_EXCEPTION_FILE_NOT_FOUND, e);
        } catch (IOException e) {
            e.printStackTrace();
            return new DownloadResponse(FLAG_DOWNLOAD_EXCEPTION_IO, e);
        } finally {
            response.body().close();
            close(bis);
            close(bos);
        }
    }

    class DownloadResponse {
        public DownloadResponse(int resultCode) {
            this.resultCode = resultCode;
        }

        public DownloadResponse(int resultCode, Exception exception) {
            this.resultCode = resultCode;
            this.exception = exception;
        }

        private int resultCode;
        private Exception exception;

        public int getResultCode() {
            return resultCode;
        }

        public void setResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Response getFile(String url, int time, String key, String value) {
        return NetUtil.getInstance().postSync(url, key, value);
    }

    private long getFileLength(String url, int time) {
        long contentLength = -1;
        for (int i = 0; i < time; i++) {
            Response response = NetUtil.getInstance().getSync(url);
            if (response == null) {
                continue;
            }

            int code = response.code();
            if (code != 200) {
                return contentLength;
            }

            long length = response.body().contentLength();
            if (length > 0) {
                contentLength = length;
                break;
            }
        }
        return contentLength;
    }

    public interface MultiDownloadListener {

        void onReadyProgram(String date, boolean hasProgram);

        void onStart(String date, int total);

        void onSingleStart(ItemBlock itemBlock, int index);

        void onProgress(int progress);

        void onFailed(ItemBlock itemBlock, Exception e);

        void onComplete(ItemBlock itemBlock);

        void onFinished(String date);
    }

    public abstract static class AutoLogDownListener implements MultiDownloadListener {
        @Override
        public void onReadyProgram(String date, boolean hasProgram) {

        }

        @Override
        public void onStart(String date, int total) {
            ConsoleDialog.addDownloadLog("开始下载 " + date);
            ConsoleDialog.updateDownloadDate(date);
            ConsoleDialog.updateTotal(total);
        }

        @Override
        public void onSingleStart(ItemBlock itemBlock, int index) {
            ConsoleDialog.updateIndex(index);
            ConsoleDialog.updateName(itemBlock.getName());
        }

        @Override
        public void onProgress(int progress) {
            ConsoleDialog.updateProgress(progress);
        }

        @Override
        public void onFailed(ItemBlock itemBlock, Exception e) {
            ConsoleDialog.updateProgress(0);
            ConsoleDialog.addDownloadLog("下载失败：" + itemBlock.getName() + "，" + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        @Override
        public void onComplete(ItemBlock itemBlock) {
            ConsoleDialog.addDownloadLog("下载成功：" + itemBlock.getName());
        }

        @Override
        public void onFinished(String date) {
            ConsoleDialog.addDownloadLog("全部下载结束 " + date);
        }
    }

}
