package com.yunbiao.yunbiaolocal.layout.view.baseControls.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbiao.yunbiaolocal.R;

import java.util.List;

/**
 * Created by Administrator on 2016/7/26 0026.
 */
public class RateAdapter extends BaseAdapter {
    private List<RateEntity.DataBean> rateDatas;
    private LayoutInflater layoutInflater;
    private Context context;

    public RateAdapter(Context context, List<RateEntity.DataBean> rateDatas) {
        this.context = context;
        this.rateDatas = rateDatas;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return rateDatas == null ? 0 : rateDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return rateDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.rmb_rate_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //币种名字
        String currency = rateDatas.get(position).getC();
        //汇率
        String rate = rateDatas.get(position).getV();
        if ("澳大利亚元".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_australia);
        } else if ("欧元汇率".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_european);
        } else if ("英镑汇率".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_britain);
        } else if ("港元汇率".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_hongkong);
        } else if ("日元汇率".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_japan);
        } else if ("美元汇率".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_usa);
        } else if ("新加坡元".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_singapore);
        } else if ("瑞士法郎".equals(currency)) {
            setItem(holder, currency, rate, R.mipmap.flag_switzerland);
            holder.flagImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView flagImageView;
        TextView currencyTextView, rateTextView;

        ViewHolder(View convertView) {
            flagImageView = (ImageView) convertView.findViewById(R.id.iv_country_flag);
            currencyTextView = (TextView) convertView.findViewById(R.id.tv_currency);
            rateTextView = (TextView) convertView.findViewById(R.id.tv_rate);
        }
    }

    private void setItem(ViewHolder setHolder, String setCurrency, String setRate, int setId) {
        setHolder.currencyTextView.setText(setCurrency);
        setHolder.rateTextView.setText(setRate);
        setHolder.flagImageView.setImageResource(setId);
    }
}
