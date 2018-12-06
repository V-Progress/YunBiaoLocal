package com.yunbiao.yunbiaolocal.copy;

/**
 * Created by Administrator on 2018/11/30.
 */

public interface copyFileListener {

    /**
     * 开始拷贝
     */
    void onCopyStart(String usbFilePath);

    /**
     * 拷贝中
     * @param i
     */
    void onCopying(int i);

    /**
     * 拷贝完成
     */
    void onCopyComplete();

    /**
     * 全部结束
     */
    void onFinish();

    /**
     * 检查文件
     * @param count
     */
    void onFileCount(int count);

    /**
     * 删除文件
     * @param path
     */
    void onDeleteFile(String path);

    /**
     * 没有文件
     */
    void onNoFile();

}
