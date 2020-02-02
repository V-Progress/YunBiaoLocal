package com.yunbiao.cccm.yunbiaolocal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yunbiao.cccm.*;
import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.net2.activity.MenuActivity;
import com.yunbiao.cccm.net2.cache.CacheManager;
import com.yunbiao.cccm.yunbiaolocal.io.Video;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayListActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private ListView mPlaylist;
    private TextView mTimer;
    private EasyIJKPlayer ijkPreview;
    private Button btnClose;

    private List<String> nameList = new ArrayList<>();
    private Map<String,String> previewMap = new HashMap<>();
    private String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(new Date());
    private LinearLayout llContainer;
    private Button btnOnline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        com.yunbiao.cccm.APP.addActivity(this);

        initView();

        initPlayList();
    }

    private void initView(){
        llContainer = findViewById(R.id.ll_container);
        mPlaylist = findViewById(R.id.playlist);
        mTimer = findViewById(R.id.timer);
        btnClose = findViewById(R.id.btn_close);
        btnOnline = findViewById(R.id.btn_online);

        ijkPreview = findViewById(R.id.ijk_player_preview);
        ijkPreview.initSoLib();
        ijkPreview.enableController(true,true);
        ijkPreview.enableListLoop(false);
        ijkPreview.setFullScreenEnable(true);
        ijkPreview.setOnFullScreenCallback(new EasyIJKPlayer.OnFullScreenCallback() {
            @Override
            public void onFullScreen(View playerView) {
                String currentVideo = ijkPreview.getCurrentVideo();
                long currentPosition = ijkPreview.getCurrentPosition();
                Log.e("123", "onFullScreen: ---------------要全屏的视频：" + currentVideo);

                ijkPreview.stop();

                Bundle bundle = new Bundle();
                bundle.putString("videoPath",currentVideo);
                bundle.putLong("position",currentPosition);
                Intent i = new Intent(PlayListActivity.this,FullscreenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
            }
        });
    }

    private void initPlayList(){
        nameList.clear();
        previewMap.clear();

        nameList.addAll(Video.playList);
        previewMap.putAll(Video.previewMap);

        mPlaylist.setAdapter(new PlayListAdapter(this, android.R.layout.simple_list_item_1, nameList));
        mPlaylist.setOnItemClickListener(this);
        mTimer.setText(Video.timer);

        btnClose.setOnClickListener(this);
        btnOnline.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkPreview.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkPreview.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkPreview.release();

        com.yunbiao.cccm.APP.removeActivity(this);
    }

    class PlayListAdapter extends ArrayAdapter<String> {
        public PlayListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            TextView textView = (TextView) convertView;
            String text = getItem(position);
            textView.setText(text);
            if (text.contains(".")) {
                textView.setSingleLine();
                textView.setEllipsize(TextUtils.TruncateAt.END);
            } else
                textView.setBackgroundColor(Color.parseColor("#333333"));
            return convertView;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = (TextView) view;
        String text = textView.getText().toString();
        if (!text.matches("^.+\\.\\S+$"))
            return;

        String path = Video.previewMap.get(yyyyMMdd + text.substring(3));
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "没有视频", Toast.LENGTH_SHORT).show();
            return;
        }
        ijkPreview.setVideoUri(path);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                this.finish();
                break;
            case R.id.btn_online:
                CacheManager.SP.putMode(1);

                APP.restart();

//                startActivity(new Intent(this, SplashActivity.class));
//
//                APP.exit();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                ijkPreview.fastForword();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                ijkPreview.fastBackward();
                break;
            case KeyEvent.KEYCODE_STEM_1:
                ijkPreview.toggle();
                break;
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
        }

        return true;
    }
}
