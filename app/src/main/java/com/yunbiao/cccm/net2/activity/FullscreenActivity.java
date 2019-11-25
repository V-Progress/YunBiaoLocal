package com.yunbiao.cccm.net2.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.janev.easyijkplayer.EasyPlayer;
import com.yunbiao.cccm.R;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {

    private EasyPlayer easyPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        easyPlayer = findViewById(R.id.easy_player);
        easyPlayer.enableController(false);

        Bundle extras = getIntent().getExtras();
        String videoPath = extras.getString("videoPath");
        long position = extras.getLong("position");

        List<String> list = new ArrayList<>();
        list.add(videoPath);
        easyPlayer.setVideos(list);
//        easyPlayer.seekTo(position);
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
            case KeyEvent.KEYCODE_DPAD_CENTER:
                easyPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
        }
        return true;
    }

}
