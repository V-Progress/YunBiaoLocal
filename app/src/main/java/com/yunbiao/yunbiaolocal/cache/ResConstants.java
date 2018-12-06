package com.yunbiao.yunbiaolocal.cache;

import android.os.Environment;

import com.yunbiao.yunbiaolocal.Const;

public class ResConstants {

    private static final String TAG = "ResConstants";

    private static String WEB_BASE_URL = Const.BASE_URL;

    /**
     * 资源获取
     **/
    private static String RESOURCE_URL = WEB_BASE_URL + "device/service/getresource.html";

    /**
     * 广告资源获取
     **/
    private static String ADS_RESOURCE_URL = WEB_BASE_URL + "api/share/getDeviceAdvert.html";

    /**
     * 判断服务器和本地布局是否匹配
     **/
    public static String LAYOUT_CHANGE_STATUS = WEB_BASE_URL + "device/service/layoutchangestatus.html";

    /**
     * 判断服务器中的设备是否在线
     **/
    public static String DEVICE_ONLINE_STATUS = WEB_BASE_URL + "device/status/getrunstatus.html";

    /**
     * 前端布局
     */
    private static String LAYOUT_MENU_URL = WEB_BASE_URL + "device/service/getLayoutMenu.html";

    /**
     * 天气获取
     **/
    public static String WEATHER_URL = WEB_BASE_URL + "weather/city.html";

    /**
     * 限号获取
     **/
    public static String CARRUN_URL = WEB_BASE_URL + "weather/carrun.html";

    /**
     * 外币汇率获取
     **/
    public static String RMBRATE_URL = WEB_BASE_URL + "weather/exrate.html";

    /**
     * 版本检测
     **/
    public static String VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";

    /**
     * 开关机时间获取
     **/
    public static String POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";

    /**
     * 截图上传
     **/
    public static String SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";

    /**
     * 上传进度
     **/
    public static String RES_UPLOAD_URL = WEB_BASE_URL + "device/service/rsupdate.html";

    /**
     * SER_NUMBER
     **/
    public static String SER_NUMBER = WEB_BASE_URL + "device/status/getHasNumber.html";

    public static String SCAN_TO_CALL = WEB_BASE_URL + "/mobilebusself/mobilebusselfpost/selectbyordersernum.html";

    public static String SETTIME = WEB_BASE_URL + "common/service/getSystemTime.html";

    /**
     * 绑定设备
     */
    public static String DEC_NUM = WEB_BASE_URL + "device/status/binduser.html";

    /**
     * 获取续费二维码
     */
    public static String QRCODE = WEB_BASE_URL + "device/renewal/getopenrenewalqrcode.html";

    /**
     * 上传人脸识别
     */
    public static String UPLOADFACE = WEB_BASE_URL + "visitors/saveVisitors.html";

    /**
     * 上传下载进度
     */
    public static String NETSPEEFURL = WEB_BASE_URL + "device/status/uploadnetspeed.html";

    /**
     * 广告播放日志上传
     */
    public static String RECEIVELOGFILE_URL = WEB_BASE_URL + "api/loginterface/insertLogFace.html";

    /**
     * 音量调节值获取
     * http://tyiyun.com/device/service/getVolume.html?deviceId=ffffffff-
     * be09-eca9-756a-0d8000000000
     */
    private static String VOLUME_URL = WEB_BASE_URL + "device/service/getVolume.html";
    public static String UPLOAD_APP_VERSION_URL = WEB_BASE_URL + "device/service/uploadAppVersionNew.html";

    public static String UPLOAD_DISK_URL = WEB_BASE_URL + "device/service/uploadDisk.html";

    private static String CACHE_BASE_PATH = "/mnt/sdcard/hsd/";
    public static String IMAGE_CACHE_PATH = CACHE_BASE_PATH + "resource/";// 资源存储目录
    public static String WEI_CACHE_PATH = CACHE_BASE_PATH + "wei/";// 资源存储目录
    public static String PROPERTY_CACHE_PATH = CACHE_BASE_PATH + "property/";// 参数缓存存储目录
    public static String SCREEN_CACHE_PATH = CACHE_BASE_PATH + "screen/";//参数缓存存储目录
    public static String PUSH_CACHE_PATH = CACHE_BASE_PATH + "push/";// 资源存储目录

    public static String PLAYLOG_PATH = CACHE_BASE_PATH + "playLog/";//广告播放日志存储目录

    public static String RESOURSE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
//
//    public static void setNewResourcePath(boolean innerSd) {
//        if (innerSd) {
//            RESOURSE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
//        } else {
//            String path = TYTool.getSdcardPath();
//            if (!TextUtils.isEmpty(path)) {
//                RESOURSE_PATH = path;
//            }
//        }
//    }
//
//    public static void initWebConnect() {
//        WEB_BASE_URL = NetConstants.RESOURCE_URL;
//        RESOURCE_URL = WEB_BASE_URL + "device/service/getresource.html";
//        LAYOUT_MENU_URL = WEB_BASE_URL + "device/service/getLayoutMenu.html";
//        WEATHER_URL = WEB_BASE_URL + "weather/city.html";
//        VERSION_URL = WEB_BASE_URL + "device/service/getversion.html";
//        POWER_OFF_URL = WEB_BASE_URL + "device/service/poweroff.html";
//        SCREEN_UPLOAD_URL = WEB_BASE_URL + "device/service/uploadScreenImg.html";
//        RES_UPLOAD_URL = WEB_BASE_URL + "device/service/rsupdate.html";
//        SER_NUMBER = WEB_BASE_URL + "device/status/getHasNumber.html";
//        VOLUME_URL = WEB_BASE_URL + "device/service/getVolume.html";
//        UPLOAD_APP_VERSION_URL = WEB_BASE_URL + "device/service/uploadAppVersionNew.html";
//        UPLOAD_DISK_URL = WEB_BASE_URL + "device/service/uploadDisk.html";
//        SETTIME = WEB_BASE_URL + "common/service/getSystemTime.html";
//        DEC_NUM = WEB_BASE_URL + "device/status/binduser.html";
//        RMBRATE_URL = WEB_BASE_URL + "weather/exrate.html";
//        QRCODE = WEB_BASE_URL + "device/renewal/getopenrenewalqrcode.html";
//        CARRUN_URL = WEB_BASE_URL + "weather/carrun.html";
//        ADS_RESOURCE_URL = WEB_BASE_URL + "api/share/getDeviceAdvert.html";
//    }
//
//    /**
//     * 下载进度完成
//     */
//    static void finishUpLoad() {
//        Map<String, String> netSpeed = new HashMap<String, String>();
//        netSpeed.put("deviceNo", HeartBeatClient.getDeviceNo());
//        netSpeed.put("speed", "-1");
//        MyXutils.getInstance().post(NETSPEEFURL, netSpeed, new MyXutils.XCallBack() {
//            @Override
//            public void onSuccess(String result) {
//
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//    }
//
//    /**
//     * 实时下载进度
//     * 上传到服务器
//     */
//
//    static void upToServer(String speedStr) {
//        Map<String, String> netSpeed = new HashMap<String, String>();
//        netSpeed.put("deviceNo", HeartBeatClient.getDeviceNo());
//        netSpeed.put("speed", speedStr);
//        MyXutils.getInstance().post(NETSPEEFURL, netSpeed, new MyXutils.XCallBack() {
//            @Override
//            public void onSuccess(String result) {
//
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//    }
//
////    /**
////     * 音量调节
////     * <p/>
////     * param uid
////     */
////    public static void setVolume(String uid) {
////        HashMap<String, String> paramMap = new HashMap<String, String>();
////        paramMap.put("deviceId", uid);
////        String volume = NetTool.sendPost(VOLUME_URL, paramMap);
////        Double volumeD = Double.parseDouble(volume);
////        SoundControl.setMusicSound(volumeD);
////    }
//
    public static void uploadAppVersion() {
//        TYTool.showTitleTip("版本:" + MainActivity.versionName);
//        HashMap<String, String> paramMap = new HashMap<String, String>();
//        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
//        paramMap.put("version", MainActivity.versionName);
//        paramMap.put("type", VersionUpdateConstants.CURRENT_VERSION + "");
//        MyXutils.getInstance().post(UPLOAD_APP_VERSION_URL, paramMap, new MyXutils.XCallBack() {
//            @Override
//            public void onSuccess(String result) {
//
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
    }
//
//    /**
//     * 上传磁盘数据
//     */
//    public static void uploadDiskInfo() {
//        String diskInfo = TYTool.getSDDiskCon();
//        String ss = "磁盘:" + diskInfo;
//        TYTool.showTitleTip(ss);
//        HashMap<String, String> paramMap = new HashMap<String, String>();
//        paramMap.put("deviceNo", HeartBeatClient.getDeviceNo());
//        paramMap.put("diskInfo", diskInfo);
//        MyXutils.getInstance().post(UPLOAD_DISK_URL, paramMap, new MyXutils.XCallBack() {
//            @Override
//            public void onSuccess(String result) {
//
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//    }
//
//    public static void deleteOtherFile() {
//        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String savePath = sdPath + PROPERTY_CACHE_PATH;
//        FileCache acache = FileCache.get(new File(savePath));
//        String layoutJson = acache.getAsString("layoutJson");
//        //获取布局中的文件
//        List allResources = getResouceList(layoutJson);
//
//        // 获取本地磁盘已经存在的资源
//        String imagePath = RESOURSE_PATH + IMAGE_CACHE_PATH;
//        File resource = new File(imagePath);
//        Map<String, File> fileMap = new HashMap<String, File>();
//        if (resource.exists()) {
//            File[] files = resource.listFiles();
//            for (File hasFile : files) {
//                fileMap.put(hasFile.getName(), hasFile);
//            }
//        } else {
//            resource.mkdirs();
//        }
//
//        //删除人流量统计未删除的照片
//        File file = new File(FaceConstant.SCREEN_SAVE_PATH);
//        FileTool.delete(file);
//
//        //移除已经有的文件
//        for (int i = 0; i < allResources.size(); i++) {
//            String names = (String) allResources.get(i);
//            names = names.substring(names.lastIndexOf("/") + 1, names.length());
//            if (fileMap.get(names) != null) {
//                fileMap.remove(names);
//            }
//        }
//        //删除剩下的数据
//        Set<String> keySet = fileMap.keySet();
//        for (String fileTemp : keySet) {
//            File hasFile = (File) fileMap.get(fileTemp);
//            hasFile.delete();
//        }
//    }
//
//    //网速
//    private DownloadSpeed netHandler = new DownloadSpeed();
//
//    /**
//     * @param deviceId
//     * @param date
//     */
//    public void getDeviceAdsResource(String deviceId, final Date date) {
//        String startTime;
//        if (date == null) {
//            startTime = DateUtil.getInstance().dateToStr(new Date(), DateUtil.Y_M_D);
//        } else {
//            startTime = DateUtil.getInstance().dateToStr(date, DateUtil.Y_M_D);
//        }
//        HashMap<String, String> paramMap = new HashMap<>();
//        paramMap.put("deviceNo", deviceId);
//        paramMap.put("startTime", startTime);
//        Log.e(TAG, "getDeviceAdsResource: " + ADS_RESOURCE_URL);
//        MyXutils.getInstance().post(ResConstants.ADS_RESOURCE_URL, paramMap, new MyXutils.XCallBack() {
//            @Override
//            public void onSuccess(String result) {
//                Log.e(TAG, "onSuccess: ->" + result);
//                // 没有返回，或者请求错误
//                if (result.startsWith("\"")) {
//                    result = result.substring(1, result.length() - 1);
//                }
//                String layoutCache="";
//                if (!result.equals("null") && !result.equals("faile") && !result.equals("[]")) {
//                    layoutCache = CacheUtil.getLayoutCacheAsString();
//                    if (date == null) {
//                        //判断日志是否启动，防止机器状态更新不及时
//                        if (!AdsManager.isStart){
//                            AdsManager.getInstance().startAdsAlarm();
//                        }
//                        if (!TextUtils.isEmpty(layoutCache) && !result.equals(layoutCache)) {
//                            SpUtils.saveInt(APP.getContext(), SpUtils.ADVERT_INDEX, 0);
//                        }
//                        if (!result.equals(layoutCache)){
//                            putLayoutCache(result);
//                            downloadLocalLayoutResource(result);
//                        }
//                    } else {
//                        putAdsInfoTemp(result);
//                    }
//
//                } else {
//                    //该天没有广告资源播放
//                    String deviceType = SpUtils.getString(APP.getContext(), SpUtils.DEVICETYPE, "0");
//                    if ("1".equals(deviceType)&&date == null) {
//                        if (!TextUtils.isEmpty(layoutCache)){
//                            List<LayoutInfo> list = LayoutJsonTool.getLayoutInfo(layoutCache);
//                            if (list!=null&&list.size()>0){
//                                Integer type = list.get(0).getType();
//                                if (18==type){
//                                    putLayoutCache("");
//                                    TimerReceiver.screen();
//                                }
//                            }
//                        }
//                    }else if ("1".equals(deviceType)&&date!= null){
//                        putAdsInfoTemp("");
//                    }
//                }
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//                ex.printStackTrace();
//                Log.e(TAG, "onError: ->" + ex.getMessage());
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//    }
//
//    /**
//     * 获取资源
//     *
//     * @param deviceId
//     * @return String savePath = sdPath + PROPERTY_CACHE_PATH;
//     * FileCache acache = FileCache.get(new File(savePath));
//     * acache.put("layoutJson", deviceJson);
//     */
//    public void getDeviceResource(String deviceId) {
//        HashMap<String, String> paramMap = new HashMap<>();
//        paramMap.put("deviceId", deviceId);
//        MyXutils.getInstance().post(ResConstants.RESOURCE_URL, paramMap, new MyXutils.XCallBack() {
//            @Override
//            public void onSuccess(String result) {
//                // 没有返回，或者请求错误
//                if (result.startsWith("\"")) {
//                    result = result.substring(1, result.length() - 1);
//                }
//                if (!result.equals("null") && !result.equals("faile") && !result.equals("[]")) {
//                    Log.e(TAG, "onSuccess: result=="+result);
//                    putLayoutCache(result);
//                    downloadLocalLayoutResource(result);
//                }
//            }
//
//            @Override
//            public void onError(Throwable ex) {
//
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        });
//    }
//
//    public void setAPPImageResource(String imageJson) {
//        if (!imageJson.equals("null") && !imageJson.equals("faile") && !imageJson.equals("[]")) {
//            putLayoutCache(imageJson);
//        }
//        downloadLocalLayoutResource(imageJson);
//    }
//
//    public void downloadLocalLayoutResource(final String deviceJson) {
//        try {
//            // 没有返回，或者请求错误
//            if (!deviceJson.equals("null") && !deviceJson.equals("faile")) {
//                DownloadCounter counter = new DownloadCounter();
//                counter.setAllCount(getUrlCount(deviceJson));
//                //更新网速速度的显示
//                NetSpeed netSpeed = new NetSpeed(BaseActivity.getActivity(), netHandler, counter);
//                //启动下载计数线程
//                netSpeed.startCalculateNetSpeed();
//
//                //如果有需要下载的文件
//                if (counter.isEquals()) {
//                    JSONArray screenArray = CacheUtil.getLayoutCacheAsArray();
//                    if (screenArray != null && screenArray.length() > 0) {
//                        TimerReceiver.screen();
//                    }
//                } else {
//                    //初始化下载完成监听
//                    if (downListener == null) {
//                        setDownListener(new DownListener() {
//                            @Override
//                            public void onComPlete() {
//                                if (!TextUtils.isEmpty(deviceJson)) {
//                                    Log.d("布局信息3", "downloadLocalLayoutResource:下载完成监听-------");
//                                    JSONArray screenArray = CacheUtil.getLayoutCacheAsArray();
//                                    if (screenArray != null && screenArray.length() > 0) {
//                                        TimerReceiver.screen();
//                                    }
//                                }
//                            }
//                        });
//                    }
//
//                    //所有资源文件保存路径
//                    String imagePath = ResConstants.RESOURSE_PATH + IMAGE_CACHE_PATH;
//                    //当前无布局现实的时候屏幕会卡在这里
//                    //文件下载
//                    getResource(deviceJson, imagePath, counter);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static DownListener downListener;
//
//    public static void setDownListener(DownListener downListener) {
//        ResConstants.downListener = downListener;
//    }
//
//    /**
//     * 下载资源
//     *
//     * @param contentJson
//     * @param sdPath
//     */
//    public void getResource(String contentJson, String sdPath, DownloadCounter counter) {
//        try {
//            // 获取本地磁盘已经存在的资源
//            File resource = new File(sdPath);
//            Map<String, File> fileMap = new HashMap<>();
//
//            if (resource.exists()) {
//                File[] files = resource.listFiles();
//                for (File hasFile : files) {
//                    if (!hasFile.getName().contains("_ok")) {
//                        fileMap.put(hasFile.getName(), hasFile);
//                    }
//                }
//            } else {
//                resource.mkdirs();
//            }
//
//            //下载所有的文件
//            List<String> allDownloadList = getResouceList(contentJson);
//            for (int i = 0; i < allDownloadList.size(); i++) {
//                String url = allDownloadList.get(i);
//                String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
//                if (fileMap.get(fileName) != null) {
//                    fileMap.remove(fileName);
//                }
//                FileCacheTool.downWebCacheFile(url, sdPath + fileName, counter);
//            }
//            JSONArray layoutArray = new JSONArray(contentJson);
//            JSONTokener jsonParser = new JSONTokener(layoutArray.getJSONObject(0).toString());
//            JSONObject urlJsonObj = (JSONObject) jsonParser.nextValue();
//            Integer resClearModel = urlJsonObj.getInt("resClearModel");
//            if (resClearModel == 1) {// 如果开启的是删除模式就进行删除不在当前布局里的文件资源
//                // 删除不在当前资源的文件
//                Set<String> keySet = fileMap.keySet();
//                for (String fileTemp : keySet) {
//                    File hasFile = (File) fileMap.get(fileTemp);
//                    hasFile.delete();
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static List getAllLayoutFiles(String content, String rules) {
//        Pattern pattern = Pattern.compile(rules);
//        Matcher matcher = pattern.matcher(content);
//        List<String> allResourList = new ArrayList<String>();
//        Map resourseMap = new HashMap();//过滤穿过来的布局信息，防止重复下载
//
//        while (matcher.find()) {
//            String fileName = matcher.group();
//            if (FileUtil.isImage(fileName) || FileUtil.isMusic(fileName) || FileUtil.isVideo(fileName)) {
//                if (!resourseMap.containsKey(fileName)) {
//                    allResourList.add(fileName);
//                }
//                resourseMap.put(fileName, fileName);
//            }
//        }
//        return allResourList;
//    }
//
//    private static Integer getMoreCharCountByRules(String content, String rules) {
//        Pattern pattern = Pattern.compile(rules);
//        Matcher matcher = pattern.matcher(content);
//        StringBuffer buffer = new StringBuffer();
//        Integer count = 0;
//        Map countMap = new HashMap();//过滤用
//        while (matcher.find()) {
//            buffer.append(matcher.group() + "\r\n");
//            if (FileUtil.isImage(matcher.group())
//                    || FileUtil.isMusic(matcher.group())
//                    || FileUtil.isVideo(matcher.group())) {
//                if (!countMap.containsKey(matcher.group())) {
//                    count++;
//                }
//                countMap.put(matcher.group(), matcher.group());
//            }
//        }
//        return count;
//    }
//
//    /**
//     * 获取url的个数
//     *
//     * @param content
//     * @return
//     */
//    public static Integer getUrlCount(String content) {
//        String urlRules = "(http://|https://){1}[\\w\\.\\-/:]+";
//        Integer urlCount = getMoreCharCountByRules(content, urlRules);
//        return urlCount;
//    }
//
//    /**
//     * 获取url的个数
//     *
//     * @param content
//     * @return
//     */
//    public static List getResouceList(String content) {
//        String urlRules = "(http://|https://){1}[\\w\\.\\-/:]+";
//        List urlCount = getAllLayoutFiles(content, urlRules);
//        return urlCount;
//    }
//
//    public static JSONArray getResouceArray(String content) {
//
//        content = content.replaceAll("\\\\", "");
//
//        String urlRules = "(http://|https://){1}[\\w\\.\\-/:]+";
//        List urlCount = getAllLayoutFiles(content, urlRules);
//
//        JSONArray fileJson = new JSONArray();
//        for (int i = 0; i < urlCount.size(); i++) {
//            fileJson.put(urlCount.get(i));
//        }
//        return fileJson;
//    }
//
//    public static void deleteDownFile() {
//        String layoutStr = CacheUtil.getLayoutCacheAsString();
//        List<String> allDownloadList = getResouceList(layoutStr);
//
//        for (int i = 0; i < allDownloadList.size(); i++) {
//            String fileName = allDownloadList.get(i);
//            fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
//            String path = RESOURSE_PATH + IMAGE_CACHE_PATH + fileName;
//        }
//    }
//
//    public static void downloadLevelFile() {
//        String layoutStr = CacheUtil.getLayoutCacheAsString();
//        List<String> allDownloadList = getResouceList(layoutStr);
//
//        List<String> downLoads = new ArrayList<String>();
//        //没有下载完成的集合
//        List<String> noDownloads = new ArrayList<String>();
//
//        for (int i = 0; i < allDownloadList.size(); i++) {
//            String fileName = allDownloadList.get(i);
//            fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
//            String path = RESOURSE_PATH + IMAGE_CACHE_PATH + fileName;
//            if (new File(path + "_ok").exists()) {//如果存在ok文件就添加到播放列表中
//                if (new File(path).exists()) {
//                    downLoads.add(path);
//                } else {
//                    FileTool.delete(new File(path + "_ok"));
//                }
//            } else {
//                noDownloads.add(path);
//            }
//        }
//        if (noDownloads.size() > 0) {//如果有未下载完成的文件就重新启动下载的功能
//            TYTool.downloadLocalLayoutResource();
//        }
//    }
//
//    public static String downloadStatus() {
//        String layoutStr = CacheUtil.getLayoutCacheAsString();
//        List<String> allDownloadList = getResouceList(layoutStr);
//        List<String> downLoads = new ArrayList<>();
//        List<String> noDownloads = new ArrayList<>();
//
//        for (int i = 0; i < allDownloadList.size(); i++) {
//            String fileName = allDownloadList.get(i);
//            fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
//            String path = RESOURSE_PATH + IMAGE_CACHE_PATH + fileName;
//            if (new File(path + "_ok").exists()) {//如果存在ok文件就添加到播放列表中
//                downLoads.add(path);
//            } else {
//                noDownloads.add(path);
//            }
//        }
//        int allCount = downLoads.size() + noDownloads.size();
//        return downLoads.size() + "/" + allCount;
//    }
}
