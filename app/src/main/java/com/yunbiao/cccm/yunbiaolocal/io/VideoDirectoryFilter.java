package com.yunbiao.cccm.yunbiaolocal.io;

import java.io.File;
import java.io.FilenameFilter;

public class VideoDirectoryFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String filename) {
        return dir.isDirectory() && filename.matches("20\\d{6}-20\\d{6}");
    }
}
