package com.yunbiao.yunbiaolocal.layout.view.baseControls.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/7/27 0027.
 */
public class RateEntity {

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    /**
     * c : 外汇币种
     * v : 现汇买入价
     */

    public static class DataBean {
        private String c;
        private String v;

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }
    }
}
