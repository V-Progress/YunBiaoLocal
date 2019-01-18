package com.yunbiao.cccm.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.resolve.VideoDataResolver;
import com.yunbiao.cccm.utils.LogUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/12/27.
 */

public class PlayListFragment extends Fragment implements View.OnTouchListener {

    private VideoView videoView;
    private ListView listView;
    private Button btnClose;
    private FragmentActivity mActivity;
    private ArrayAdapter arrayAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        rootView.setOnTouchListener(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.playlist);
        videoView = view.findViewById(R.id.preview);
        btnClose = view.findViewById(R.id.btn_close_playlist);
    }

    public void updateList(){
        if(arrayAdapter != null){
            LogUtil.E("刷新");
            arrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, VideoDataResolver.playList == null
                ? new ArrayList<String>()
                : VideoDataResolver.playList) {
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = LayoutInflater.from(mActivity).inflate(android.R.layout.simple_list_item_1, null);
                TextView textView = (TextView) convertView;
                String text = getItem(position);
                textView.setText(text);
                if (text.contains(".")) {
                    textView.setTextColor(mActivity.getResources().getColor(R.color.white));
                    textView.setSingleLine();
                    textView.setEllipsize(TextUtils.TruncateAt.END);

                } else {
                    textView.setBackgroundColor(mActivity.getResources().getColor(R.color.trans_gray));
                }
                return convertView;
            }
        };

        videoView.setZOrderOnTop(true);//置顶显示，否则会被dialog遮挡，亮度变低
        listView.setDivider(mActivity.getResources().getDrawable(R.drawable.divider_playlist));
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                String text = textView.getText().toString();
                if (!text.matches("^.+\\.\\S+$")) {
                    return;
                }
                String path = VideoDataResolver.previewMap.get(/*yyyyMMdd + */text.substring(3));
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(mActivity, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                videoView.setVideoPath(path);
                videoView.start();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuActivity) mActivity).backFragment(PlayListFragment.this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        videoView.stopPlayback();
        videoView.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
