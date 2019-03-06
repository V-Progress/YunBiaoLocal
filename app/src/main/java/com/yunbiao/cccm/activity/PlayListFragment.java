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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yunbiao.cccm.R;
import com.yunbiao.cccm.common.ResourceConst;
import com.yunbiao.cccm.log.LogUtil;

/**
 * Created by Administrator on 2018/12/27.
 */

public class PlayListFragment extends Fragment implements View.OnTouchListener {

    private VideoView videoView;
    private ListView listView;
    private Button btnClose;
    private FragmentActivity mActivity;
    private ArrayAdapter arrayAdapter;
    private ProgressBar pbPreview;
    private SeekBar skbPreview;

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
        skbPreview = view.findViewById(R.id.sb_preview);
        videoView.setZOrderOnTop(true);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                skbPreview.setMax(0);
                skbPreview.setProgress(0);
                skbPreview.setEnabled(false);
            }
        });
    }

    public void updateList(){
        if(arrayAdapter != null){
            arrayAdapter.notifyDataSetChanged();
        }
    }

    Handler previewHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                if(videoView.isPlaying()){
                    skbPreview.setMax(videoView.getDuration());
                    skbPreview.setProgress(videoView.getCurrentPosition());
                }
            }
            previewHandler.sendEmptyMessageDelayed(0,1000);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, ResourceConst.getPlayList()) {
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

                String path = ResourceConst.getPreviewMap().get(/*yyyyMMdd + */text.substring(3));
                if (TextUtils.isEmpty(path)) {
                    Toast.makeText(mActivity, "没有视频", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (videoView.isPlaying()) {
                    videoView.stopPlayback();
                }
                videoView.setVideoPath(path);
                videoView.start();
                previewHandler.removeMessages(0);
                previewHandler.sendEmptyMessage(0);

                skbPreview.setEnabled(true);
                skbPreview.setMax(videoView.getDuration());
                skbPreview.setProgress(videoView.getCurrentPosition());
                skbPreview.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(videoView.canSeekBackward() && videoView.canSeekForward()){
                            videoView.seekTo(progress);
                        }
                    }


                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
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
