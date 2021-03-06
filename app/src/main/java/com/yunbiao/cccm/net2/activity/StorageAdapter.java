package com.yunbiao.cccm.net2.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yunbiao.cccm.R;

import java.util.List;

/**
 * Created by Administrator on 2018/10/8.
 */

public class StorageAdapter extends BaseAdapter {

    private List<String> mList;
    private Context mContext;

    public StorageAdapter(Context pContext, List<String> pList) {
        this.mContext = pContext;
        this.mList = pList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //最主要代码
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        convertView = layoutInflater.inflate(R.layout.item_storage_spinner, parent, false);

        if (convertView != null) {
            TextView _TextView1 = convertView.findViewById(R.id.tv_depart);
            _TextView1.setText(mList.get(position));
        }
        return convertView;
    }
}
