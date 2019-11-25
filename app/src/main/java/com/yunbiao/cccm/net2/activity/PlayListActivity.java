package com.yunbiao.cccm.net2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.janev.easyijkplayer.EasyPlayer;
import com.yunbiao.cccm.R;
import com.yunbiao.cccm.net2.SystemVersion;
import com.yunbiao.cccm.net2.activity.base.BaseActivity;
import com.yunbiao.cccm.net2.PathManager;
import com.yunbiao.cccm.net2.db.Daily;
import com.yunbiao.cccm.net2.db.DaoManager;
import com.yunbiao.cccm.net2.db.ItemBlock;
import com.yunbiao.cccm.net2.db.TimeSlot;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @BindView(R.id.easy_player)
    EasyPlayer easyPlayer;
    @BindView(R.id.pb_playlist)
    ProgressBar pbPlayList;
    @BindView(R.id.tv_load_notice)
    TextView tvLoadNotice;

    private List<String> playList = new ArrayList<>();
    private PlayListAdapter playListAdapter;
    private ExecutorService executorService;

    @Override
    protected int setLayout() {
        return R.layout.fragment_playlist;
    }

    @Override
    protected void initView() {
        executorService = Executors.newSingleThreadExecutor();

        easyPlayer.enableController(true);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playListAdapter = new PlayListAdapter(this, android.R.layout.simple_list_item_1, playList);
        listView.setDivider(getResources().getDrawable(R.drawable.divider_playlist));
        listView.setAdapter(playListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = playListAdapter.getItem(position);
                if(!item.matches("^.+\\.\\S+$")){
                    return;
                }

                String filePath = null;
                if(SystemVersion.isLowVer()){
                    File resFileDir = PathManager.instance().getResFileDir();
                    File file = new File(resFileDir,item);

                    Log.e(TAG, "onItemClick: 文件目录：" + file.getPath());

                    if(file != null && file.exists()){
                        filePath = file.getPath();
                    }
                } else {
                    DocumentFile file = PathManager.instance().getResDocFileDir().findFile(item);
                    if(file != null && file.exists()){
                        filePath = file.getUri().toString();
                    }
                }

                if (TextUtils.isEmpty(filePath)) {
                    Toast.makeText(PlayListActivity.this, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> list = new ArrayList<>();
                list.add(filePath);
                easyPlayer.setVideos(list);
                easyPlayer.setVisibility(View.VISIBLE);
                tvPreview.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void initData() {
        initPlayList();
    }

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private void initPlayList(){
        pbPlayList.setVisibility(View.VISIBLE);
        tvLoadNotice.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                final List<Daily> dailies = DaoManager.get().queryAll(Daily.class);
                Collections.sort(dailies, new Comparator<Daily>() {
                    @Override
                    public int compare(Daily daily, Daily t1) {
                        try {
                            Date date1 = dateFormat.parse(daily.getDate());
                            Date date2 = dateFormat.parse(t1.getDate());
                            return date1.after(date2) ? 1 : 0;
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return 1;
                    }
                });

                for (Daily daily : dailies) {
                    playList.add(daily.getDate());
                    for (TimeSlot timeSlot : daily.getTimeSlots()) {
                        playList.add(timeSlot.getStart() + " - " + timeSlot.getEnd());
                        for (ItemBlock itemBlock : timeSlot.getItemBlocks()) {
                            String name = itemBlock.getName();

                            String filePath = null;
                            if(SystemVersion.isLowVer()){
                                File resFileDir = PathManager.instance().getResFileDir();
                                File file = new File(resFileDir, itemBlock.getName());
                                if(file != null && file.exists()){
                                    filePath = file.getPath();
                                }
                            } else {
                                DocumentFile file = PathManager.instance().getResDocFileDir().findFile(itemBlock.getName());
                                if(file != null && file.exists()){
                                    filePath = file.getUri().toString();
                                }
                            }
                            if(TextUtils.isEmpty(filePath)){
                                name += "（无）";
                            }
                            playList.add(name);
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbPlayList.setVisibility(View.GONE);
                        tvLoadNotice.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        playListAdapter.notifyDataSetChanged();
                        if(playList.size() <= 0){
                            btnClose.requestFocus();
                        } else {
                            listView.requestFocus();
                        }
                    }
                });
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
        easyPlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        easyPlayer.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        easyPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                easyPlayer.fastForward();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                easyPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_STEM_1:
                easyPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_NUMPAD_1:
            case KeyEvent.KEYCODE_MENU:
                easyPlayer.setCycle(false);
                String currentPath = easyPlayer.getCurrentPath();
                long currentPosition = easyPlayer.getCurrentPosition();
                Bundle bundle = new Bundle();
                bundle.putString("videoPath",currentPath);
                bundle.putLong("position",currentPosition);
                Intent i = new Intent(PlayListActivity.this,FullscreenActivity.class);
                i.putExtras(bundle);
                startActivity(i);
                break;
        }

        return true;
    }

}
