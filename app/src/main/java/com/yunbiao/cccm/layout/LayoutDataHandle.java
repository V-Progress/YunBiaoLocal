package com.yunbiao.cccm.layout;

import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.layout.bean.Center;
import com.yunbiao.cccm.layout.bean.Container;
import com.yunbiao.cccm.layout.bean.LayoutModel;
import com.yunbiao.cccm.layout.bean.LayoutPosition;
import com.yunbiao.cccm.common.HeartBeatClient;
import com.yunbiao.cccm.utils.LogUtil;
import com.yunbiao.cccm.utils.NetUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;

/**
 * Created by Administrator on 2018/12/11.
 */

public class LayoutDataHandle {


    private static LayoutDataHandle instance;

    public static synchronized LayoutDataHandle getInstance() {
        if (instance == null) {
            instance = new LayoutDataHandle();
        }
        return instance;
    }

    public void handleLayoutData() {
        //io线程中请求网络
        Observable.create(
                new ObservableOnSubscribe<LayoutModel>() {
                    @Override
                    public void subscribe(@NonNull final ObservableEmitter<LayoutModel> emitter) throws Exception {
                        //请求后台获取布局数据
                        HashMap<String, String> params = new HashMap<>();
                        params.put("deviceId", HeartBeatClient.getDeviceNo());
                        LogUtil.E("ObservableOnSubscribe当前线程：" + Thread.currentThread().getName());
                        NetUtil.getInstance().post(ResourceConst.REMOTE_RES.RESOURCE_URL, params, new StringCallback() {

                            @Override
                            public void onResponse(String response, int id) {
                                // 没有返回，或者请求错误
                                if (response.startsWith("\"")) {
                                    response = response.substring(1, response.length() - 1);

                                }
                                if (!response.equals("null") && !response.equals("faile") && !response.equals("[]")) {
                                    List<LayoutModel> list = new Gson().fromJson(response, new TypeToken<ArrayList<LayoutModel>>() {
                                    }.getType());
                                    LayoutModel layoutModel = list.get(0);
                                    emitter.onNext(layoutModel);
                                    emitter.onComplete();
                                }
                                LogUtil.E(response);
                            }

                            @Override
                            public void onError(Call call, Exception e, int id) {
                                emitter.onError(e);
                                call.cancel();
                            }
                        });
                    }
                }).subscribeOn(Schedulers.io())
                //计算线程中处理布局位置的创建
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<LayoutModel, List<View>>() {
                    @Override
                    public List<View> apply(@NonNull LayoutModel o) throws Exception {
                        LogUtil.E("map线程：" + Thread.currentThread().getName());
                        List<View> views = getLayoutView(o.getCenter());
                        return views;
                    }
                })
                //主线程中设置View
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<View>>() {
                    @Override
                    public void accept(List<View> views) throws Exception {
                        LogUtil.E("subscribe线程：" + Thread.currentThread().getName());
                        for (View view : views) {
                            LayoutRefresher.getInstance().addView(view);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    public List<View> getLayoutView(List<Center> centers) {
        List<View> viewList = new ArrayList<>();
        for (Center center : centers) {
            LayoutPosition lp;
            try{
                lp = getViewPostion(center.getContainer(), APP.getAbsoluteActivity().getWindowManager());
            }catch (Exception e){
                continue;
            }

            View view = LayoutViewCreater.getInstance(APP.getAbsoluteActivity())
                    .type(Integer.valueOf(center.getType()))
                    .content(center)
                    .layoutPosition(lp)
                    .create();
            viewList.add(view);
        }
        return viewList;
    }

    public static Integer TOOL_HEIGHT = 70;
    public static Integer FOOT_HEIGHT = 70;

    /**
     * 获取布局的位置
     */
    public LayoutPosition getViewPostion(Container container, WindowManager wm) {
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        int width = point.x;
        int height = point.y - TOOL_HEIGHT - FOOT_HEIGHT;

        String widths = container.getWidth();
        String heights = container.getHeight();
        String lefts = container.getLeft();
        String tops = container.getTop();

        float layoutHeight = Float.valueOf(heights.substring(0, heights.indexOf("%"))) / 100;
        float layoutWidth = Float.valueOf(widths.substring(0, widths.indexOf("%"))) / 100;
        float layoutLeft = Float.valueOf(lefts.substring(0, lefts.indexOf("%"))) / 100;
        float layoutTop = Float.valueOf(tops.substring(0, tops.indexOf("%"))) / 100;

        Float layHeight = (layoutHeight * height);
        Float layWidth = (layoutWidth * width);
        Float layLeft = (layoutLeft * width);
        Float layTop = (layoutTop * height);

        LayoutPosition layoutPostion = new LayoutPosition();
        layoutPostion.setHeight(getFloatToInt(layHeight, "#"));
        layoutPostion.setWidth(getFloatToInt(layWidth, "#"));
        layoutPostion.setLeft(getFloatToInt(layLeft, "#"));
        layoutPostion.setTop(getFloatToInt(layTop, "#") + TOOL_HEIGHT);

        return layoutPostion;
    }

    /**
     * 给参数返回指定小数点后几位的四舍五入
     *
     * @param sourceData 传入的要舍取的元数据
     * @param sf         取舍的格式（主要用到"#.0"的格式，此为小数点后1位；"#.00"为小数点后2位，以此类推）
     * @return 舍取后的 数据
     */
    public static Integer getFloatToInt(Float sourceData, String sf) {
        DecimalFormat df = new DecimalFormat(sf);
        String str = df.format(sourceData);
        return Integer.parseInt(str);
    }
}
