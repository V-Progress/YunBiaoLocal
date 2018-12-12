package com.yunbiao.yunbiaolocal.layout.view.baseControls.view;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.google.gson.Gson;
import com.yunbiao.yunbiaolocal.R;
import com.yunbiao.yunbiaolocal.common.ResourceConst;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.bean.RateAdapter;
import com.yunbiao.yunbiaolocal.layout.view.baseControls.bean.RateEntity;
import com.yunbiao.yunbiaolocal.utils.LogUtil;
import com.yunbiao.yunbiaolocal.utils.NetUtil;
import com.zhy.http.okhttp.callback.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by jsx on 2016/7/26 0026.
 */
public class RMBrateView {
    private Context context;

    public RMBrateView(Context context) {
        this.context = context;
        initView();
        setView();
    }

    private View view;

    public View getView() {
        return view;
    }

    private ListView rmbListView;
    private List<RateEntity.DataBean> rateDatas;
    private RateAdapter rateAdapter;
    private Timer timer = new Timer();

    private void setView() {
        rateDatas = new ArrayList<>();
        rateAdapter = new RateAdapter(context, rateDatas);
        rmbListView.setAdapter(rateAdapter);
        //2个小时 更新一次
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                downloadInfo();
            }
        }, 0, 1000 * 60 * 120);
    }

    private void downloadInfo() {
        NetUtil.getInstance().get(ResourceConst.REMOTE_RES.RMBRATE_URL, new Callback() {
            @Override
            public String parseNetworkResponse(Response response, int id) throws Exception {
                return response.body().string();
            }

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(Object result, int id) {
                String urlString = "{ data:" + result + "}";
                LogUtil.E(urlString);
                List<RateEntity.DataBean> list = new Gson().fromJson(urlString, RateEntity.class).getData();  //list.size = 26
                if (rateDatas.size() == 0) {
                    selectCurrency("美元", list, 0);
                    selectCurrency("欧元", list, 1);
                    selectCurrency("英镑", list, 2);
                    selectCurrency("瑞士", list, 3);
                    selectCurrency("澳大利亚", list, 4);
                    selectCurrency("港元", list, 5);
                    selectCurrency("新加坡", list, 6);
                    selectCurrency("日元", list, 7);
                    rateAdapter.notifyDataSetChanged();
                } else {
                    rateDatas.removeAll(list);
                    downloadInfo();
                }
            }
        });
    }

    private View initView() {
        view = View.inflate(context, R.layout.rmb_rate_layout, null);
        rmbListView = view.findViewById(R.id.lv_rmb_position);
        return view;
    }

    private void selectCurrency(String selected, List<RateEntity.DataBean> list, int position) {
        for (int i = 0; i < list.size(); i++) {
            String currency = list.get(i).getC();
            if (currency.contains(selected)) {
                rateDatas.add(position, list.get(i));
            }
        }
    }
}
