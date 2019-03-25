package com.yunbiao.cccm.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.janev.easyijkplayer.EasyIJKPlayer;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.activity.base.BaseActivity;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.net.resource.ResourceManager;
import com.yunbiao.cccm.net.resource.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by Administrator on 2019/3/14.
 */

public class PlayListActivity extends BaseActivity {
    @BindView(R.id.playlist)
    ListView listView;
    @BindView(R.id.fl_video_container)
    FrameLayout flVideo;
    @BindView(R.id.tv_preview)
    TextView tvPreview;
    @BindView(R.id.btn_close_playlist)
    Button btnClose;
    @BindView(R.id.ijk_player)
    EasyIJKPlayer easyIJKPlayer;

    private ArrayAdapter<String> arrayAdapter;

    private List<String> playList = new ArrayList<>();
    private Map<String, String> previewMap;

    @Override
    protected int setLayout() {
        return R.layout.fragment_playlist;
    }

    @Override
    protected void initView() {
        easyIJKPlayer.enableController(true,true);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void initData() {
        updateList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(UpdateEvent event) {
        if (event.getCode() == UpdateEvent.UPDATE_PLAYLIST) {
            List<String> pl = ResourceManager.getInstance().getPlayList();
            int position = playList.size();
            if (arrayAdapter != null) {
                playList.clear();
                playList.addAll(pl);
                previewMap = ResourceManager.getInstance().getPreview();
                arrayAdapter.notifyDataSetChanged();
            } else {
                updateList();
            }
            listView.smoothScrollToPositionFromTop(position, 120);
        }
    }

    private void updateList() {
        playList.clear();
        playList.addAll(ResourceManager.getInstance().getPlayList());
        previewMap = ResourceManager.getInstance().getPreview();
        LogUtil.E("ResourceManager", "playList:" + playList.toString());
        initList();
    }

    private void initList() {
        if (playList == null || playList.size() <= 0) {
            return;
        }
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playList) {
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView = LayoutInflater.from(PlayListActivity.this).inflate(android.R.layout.simple_list_item_1, null);
                TextView textView = (TextView) convertView;
                String text = getItem(position);
                textView.setText(text);
                if (text.contains(".")) {
                    textView.setTextColor(getResources().getColor(R.color.white));
                    textView.setSingleLine();
                    textView.setEllipsize(TextUtils.TruncateAt.END);
                } else {
                    textView.setBackgroundColor(getResources().getColor(R.color.trans_gray));
                }
                return convertView;
            }
        };

        listView.setDivider(getResources().getDrawable(R.drawable.divider_playlist));
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
                    Toast.makeText(PlayListActivity.this, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                LogUtil.E("ResourceManager", "要播放的地址：" + path);

                easyIJKPlayer.setVideoUri(path);
                easyIJKPlayer.setVisibility(View.VISIBLE);
                tvPreview.setVisibility(View.GONE);
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        easyIJKPlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        easyIJKPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        easyIJKPlayer.release();
        EventBus.getDefault().unregister(this);
    }

}
