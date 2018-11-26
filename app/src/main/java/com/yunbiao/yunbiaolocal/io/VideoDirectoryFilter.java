package com.yunbiao.yunbiaolocal.io;

import java.io.File;
import java.io.FilenameFilter;

public class VideoDirectoryFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String filename) {
        return dir.isDirectory() && filename.matches("201\\d{5}-201\\d{5}");
    }
}
