package com.yunbiao.cccm.activity;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.yunbiao.cccm.net.view.NumberProgressBar;
import com.yunbiao.cccm.sd.HighVerSDController;
import com.yunbiao.cccm.sd.LowVerSDController;
import com.yunbiao.cccm.utils.ThreadUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * Created by Administrator on 2019/3/14.
 */

public class PlayListActivity extends BaseActivity {
    private static final String TAG = "PlayListActivity";
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
    @BindView(R.id.pb_playlist)
    ProgressBar pbPlayList;
    @BindView(R.id.npb_playlist)
    NumberProgressBar npbPlayList;

    private List<String> playList = new ArrayList<>();
    private Map<String, String> previewMap = new HashMap<>();
    private PlayListAdapter playListAdapter;

    @Override
    protected int setLayout() {
        return R.layout.fragment_playlist;
    }

    @Override
    protected void initView() {
        easyIJKPlayer.enableController(true, true);
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
        initList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(UpdateEvent event) {
        LogUtil.E("收到通知----------");
        if (event.getCode() == UpdateEvent.UPDATE_PLAYLIST) {
            initList();
        }
    }

    private void initList() {
        npbPlayList.setVisibility(View.VISIBLE);
        pbPlayList.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        ThreadUtil.getInstance().runInCommonThread(new Runnable() {
            @Override
            public void run() {
                playList.clear();
                previewMap.clear();

                final List<String> tempList;
                if (CacheManager.SP.getMode() == 0) {
                    tempList = ResourceManager.getInstance().getPlayList();

                    if (tempList == null || tempList.size() <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pbPlayList.setVisibility(View.GONE);
                                npbPlayList.setVisibility(View.GONE);
                            }
                        });
                        return;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            npbPlayList.setMax(tempList.size());
                        }
                    });


                    for (int i = 0; i < tempList.size(); i++) {
                        String s = tempList.get(i);
                        if (s.startsWith("*")) {
                            playList.add(s.replace("*", ""));
                            continue;
                        }

                        String[] split = s.split("\\*");
                        if (split == null || split.length < 2) {
                            continue;
                        }

                        String videoName = split[1];
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            //生成File
                            File video = LowVerSDController.instance().findResource(videoName);
                            if (!video.exists()) {
                                playList.add(split[0] + split[1] + "（无）");
                                continue;
                            }
                            playList.add(split[0] + split[1]);
                            previewMap.put(split[1], Uri.fromFile(video).toString());
                        } else {
                            DocumentFile video = HighVerSDController.instance().findResource(videoName);
                            if (video == null || (!video.exists())) {
                                playList.add(split[0] + split[1] + "（无）");
                                continue;
                            }
                            playList.add(split[0] + split[1]);
                            previewMap.put(split[1], video.getUri().toString());
                        }

                        Log.e(TAG, "run: 111111111111111");
                        final int finalI = i;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                npbPlayList.setProgress(finalI);
                            }
                        });
                    }
                } else {
                    tempList = LocalManager.getInstance().getPlayList();
                    Log.e(TAG, "run: " + tempList.toString());
                    playList = tempList;
                    previewMap = LocalManager.getInstance().getPreview();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbPlayList.setVisibility(View.GONE);
                        npbPlayList.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        initListView();
                    }
                });
            }
        });
    }

    private void initListView() {
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


                String key = text.substring(3);
                String path = previewMap.get(key);
                Log.e("123", "onItemClick: -----" + key + "---" + path);

                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(PlayListActivity.this, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }

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
