package com.yunbiao.cccm.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by jsx on 2016/10/28 0028.
 */

public class FileTool {
    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     * */
    public static File renameFile(String oldPath, String newPath) {
        if(TextUtils.isEmpty(oldPath)) {
            return null;
        }

        if(TextUtils.isEmpty(newPath)) {
            return null;
        }

        File file = new File(oldPath);
        File newFile = new File(newPath);
        file.renameTo(newFile);
        return newFile;
    }
    /**
     * 文件拷贝
     */
    public static int copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        if (!root.exists()) {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();
        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (File currentFile : currentFiles) {
            if (currentFile.isDirectory()) {//如果当前项为子目录 进行递归
                copy(currentFile.getPath() + "/", toFile + currentFile.getName() + "/");
            } else {//如果当前项为文件则进行文件拷贝
                CopySdcardFile(currentFile.getPath(), toFile + currentFile.getName());
            }
        }
        return 0;
    }

    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    private static int CopySdcardFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }

    /**
     * 删除文件
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (File childFile : childFiles) {
                delete(childFile);
            }
            file.delete();
        }
    }

    /**
     * 云歌时代创  根目录下 建配置文件
     */
    public static void created(String value, String fileName) {
        String path = Environment.getExternalStorageDirectory().getPath() + fileName;
        File file = new File(path);
        try {
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        print(path, value);
    }

    //向已创建的文件中写入数据
    public static void print(String path, String value) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(path, true);// 创建FileWriter对象，用来写入字符流
            bw = new BufferedWriter(fw);// 将缓冲对文件的输出

            bw.write(value); // 写入文件
            bw.newLine();
            bw.flush(); // 刷新该流的缓冲
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                bw.close();
                fw.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }
    }

    //向已创建的文件中写入数据(覆盖文本)
    public static void writ(String path, String value) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(path);// 创建FileWriter对象，用来写入字符流
            bw = new BufferedWriter(fw);// 将缓冲对文件的输出

            bw.write(value); // 写入文件
            bw.newLine();
            bw.flush(); // 刷新该流的缓冲
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                bw.close();
                fw.close();
            } catch (IOException e1) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读文件内容
     */
    public static String getString(File file) {
        InputStreamReader inputStreamReader = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException | FileNotFoundException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void copyFilesFromAssets(Context context, String assetsPath, String savePath){
        try {
            InputStream is = context.getAssets().open(assetsPath);
            File file = new File(savePath);
            String absolutePath = file.getAbsolutePath();
            File aFile = new File(savePath.substring(0,savePath.lastIndexOf("/")));
            if (!aFile.exists()){
                aFile.mkdirs();
            }
            if (!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                // buffer字节
                fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
            }
            fos.flush();// 刷新缓冲区
            is.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
