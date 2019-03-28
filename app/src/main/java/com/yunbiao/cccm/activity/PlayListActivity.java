package com.yunbiao.cccm.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
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
import com.yunbiao.cccm.cache.CacheManager;
import com.yunbiao.cccm.local.LocalManager;
import com.yunbiao.cccm.log.LogUtil;
import com.yunbiao.cccm.net.resource.ResourceManager;
import com.yunbiao.cccm.net.resource.UpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
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

    private List<String> playList = new ArrayList<>();
    private Map<String, String> previewMap = new HashMap<>();
    private PlayListAdapter playListAdapter;

    @Override
    protected int setLayout() {
        return R.layout.fragment_playlist;
    }

    @Override
    protected void initView() {
        easyIJKPlayer.enableController(true,true);
        EventBus.getDefault().register(this);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        updateList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(UpdateEvent event) {
        LogUtil.E("收到通知----------");
        if (event.getCode() == UpdateEvent.UPDATE_PLAYLIST) {
            int mode = CacheManager.SP.getMode();
            if(mode != 0){
                updateList();
                return;
            }

            List<String> pl= ResourceManager.getInstance().getPlayList();
            if(pl == null || pl.size()<=0){
                return;
            }
            int position = playList.size();
            if (playListAdapter != null) {
                playList.clear();
                playList.addAll(pl);
                previewMap = ResourceManager.getInstance().getPreview();
                playListAdapter.notifyDataSetChanged();
            } else {
                updateList();
            }
            listView.smoothScrollToPositionFromTop(position, 120);
        }
    }

    private void updateList() {
        playList.clear();
        previewMap.clear();
        if(CacheManager.SP.getMode() == 0){
            playList.addAll(ResourceManager.getInstance().getPlayList());
            previewMap = ResourceManager.getInstance().getPreview();
        } else {
            playList.addAll(LocalManager.getInstance().getPlayList());
            previewMap = LocalManager.getInstance().getPreview();
        }

        LogUtil.E("PlayListActivity", "playList:" + playList.toString());
        initList();
    }

    private void initList() {
        if (playList == null || playList.size() <= 0) {
            return;
        }

        playListAdapter = new PlayListAdapter(this, android.R.layout.simple_list_item_1, playList);
        listView.setDivider(getResources().getDrawable(R.drawable.divider_playlist));
        listView.setAdapter(playListAdapter);
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

    }

    class PlayListAdapter extends ArrayAdapter<String> {
        public PlayListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                easyIJKPlayer.fastForword();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                easyIJKPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_STEM_1:
                easyIJKPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
        }

        return true;
    }

}
