package com.yunbiao.cccm.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.umeng.commonsdk.debug.E;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.net.resource.ResourceManager;
import com.yunbiao.cccm.net.resource.model.VideoDataModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/12/27.
 */
// TODO: 2019/3/8  
public class PlayListFragment extends Fragment implements View.OnTouchListener {

    private VideoView videoView;
    private ListView listView;
    private Button btnClose;
    private FragmentActivity mActivity;
    private ArrayAdapter arrayAdapter;
    private List<String> playList;
    private Map<String, String> previewMap;
    public static final int UPDATE_TAG = 101;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        playList = ResourceConst.getPlayList();
        previewMap = ResourceConst.getPreviewMap();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);
        rootView.setOnTouchListener(this);

        EventBus.getDefault().register(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.playlist);
        videoView = view.findViewById(R.id.preview);
        btnClose = view.findViewById(R.id.btn_close_playlist);
        videoView.setZOrderOnTop(true);
        videoView.setMediaController(new MediaController(mActivity,true));

        List<VideoDataModel.Play> playList = ResourceManager.getInstance().getPlayList();
        LogUtil.E("ResourceManager","初始化playList："+playList.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(int messageEvent) {
        if(messageEvent == UPDATE_TAG){
            LogUtil.E("ResourceManager","收到列表更新通知:"+playList.toString());
        }
    }

    public void updateList(){
        LogUtil.E("收到列表更新通知");
        playList = ResourceConst.getPlayList();
        previewMap = ResourceConst.getPreviewMap();
        if(arrayAdapter != null){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    arrayAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1,playList) {
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

                String path = previewMap.get(text.substring(3));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
