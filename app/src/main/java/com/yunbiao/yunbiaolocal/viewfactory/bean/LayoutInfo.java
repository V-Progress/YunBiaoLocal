package com.yunbiao.yunbiaolocal.viewfactory.bean;


public class LayoutInfo {
    private String id;
    private Integer type; // 1 图片 2文本 3视频 4 微信 5网页 6背景音乐 7微信打印 11触摸查询 12摄像头 13叫号界面显示  0背景
    private Container container;
    private String[] content;
    private TextDetail textDetail;// 文本详细
    private ImageDetail imageDetail;// 图片内容详细
    private VideoDetail videoDetail;// 视频内容详细
    private WeiDetail weiDetail;// 微信内容详细
    private WebDetail webDetail;// 网页内容详细
    private MusicDetail musicDetail;// 音乐内容详细
//    private TouchQueryDetail touchQueryDetail;//触摸查询
//    private CallQueueOrderDetail callQueueOrderDetail;//叫号界面显示
    private Integer windowType; //type 14 下的二级windowType  1天气 2日历 3汇率 4倒计时
    private ControlsDetail controlsDetail;//倒计时内容详细
    private CaramDetail caramDetail;
    private AdsDetail adsDetail;

    private AdsInfo adsInfo;//自运营广告

    public AdsInfo getAdsInfo() {
        return adsInfo;
    }

    public void setAdsInfo(AdsInfo adsInfo) {
        this.adsInfo = adsInfo;
    }

    public AdsDetail getAdsDetail() {
        return adsDetail;
    }

    public void setAdsDetail(AdsDetail adsDetail) {
        this.adsDetail = adsDetail;
    }

    public CaramDetail getCaramDetail() {
        return caramDetail;
    }

    public void setCaramDetail(CaramDetail caramDetail) {
        this.caramDetail = caramDetail;
    }

    public ControlsDetail getControlsDetail() {
        return controlsDetail;
    }

    public void setControlsDetail(ControlsDetail controlsDetail) {
        this.controlsDetail = controlsDetail;
    }

    public Integer getWindowType() {
        return windowType;
    }

    public void setWindowType(Integer windowType) {
        this.windowType = windowType;
    }

//    public CallQueueOrderDetail getCallQueueOrderDetail() {
//        return callQueueOrderDetail;
//    }
//
//    public void setCallQueueOrderDetail(CallQueueOrderDetail callQueueOrderDetail) {
//        this.callQueueOrderDetail = callQueueOrderDetail;
//    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    public TextDetail getTextDetail() {
        return textDetail;
    }

    public void setTextDetail(TextDetail textDetail) {
        this.textDetail = textDetail;
    }

    public ImageDetail getImageDetail() {
        return imageDetail;
    }

    public void setImageDetail(ImageDetail imageDetail) {
        this.imageDetail = imageDetail;
    }

    public VideoDetail getVideoDetail() {
        return videoDetail;
    }

    public void setVideoDetail(VideoDetail videoDetail) {
        this.videoDetail = videoDetail;
    }

    public WeiDetail getWeiDetail() {
        return weiDetail;
    }

    public void setWeiDetail(WeiDetail weiDetail) {
        this.weiDetail = weiDetail;
    }

    public WebDetail getWebDetail() {
        return webDetail;
    }

    public void setWebDetail(WebDetail webDetail) {
        this.webDetail = webDetail;
    }

    public MusicDetail getMusicDetail() {
        return musicDetail;
    }

    public void setMusicDetail(MusicDetail musicDetail) {
        this.musicDetail = musicDetail;
    }

//    public TouchQueryDetail getTouchQueryDetail() {
//        return touchQueryDetail;
//    }
//
//    public void setTouchQueryDetail(TouchQueryDetail touchQueryDetail) {
//        this.touchQueryDetail = touchQueryDetail;
//    }
}
