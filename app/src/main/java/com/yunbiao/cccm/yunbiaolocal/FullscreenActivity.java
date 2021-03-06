package com.yunbiao.cccm.yunbiaolocal;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.yunbiao.cccm.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends Activity {

    private EasyIJKPlayer ijkPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        com.yunbiao.cccm.APP.addActivity(this);

        ijkPlayer = findViewById(R.id.ijk_player);
        ijkPlayer.initSoLib();
        ijkPlayer.enableController(true,true);
        ijkPlayer.enableListLoop(false);
        ijkPlayer.setFullScreenEnable(false);

        Bundle extras = getIntent().getExtras();
        String videoPath = extras.getString("videoPath");
        long position = extras.getLong("position");
        ijkPlayer.setVideoUri(videoPath);
        if(position > 0){
            ijkPlayer.seekTo(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkPlayer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkPlayer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkPlayer.release();

        com.yunbiao.cccm.APP.removeActivity(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT://快进
                ijkPlayer.fastForword();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT://快退
                ijkPlayer.fastBackward();
                break;
            case KeyEvent.KEYCODE_STEM_1:
                ijkPlayer.toggle();
                break;
            case KeyEvent.KEYCODE_BACK:
                this.finish();
                break;
        }

        return true;
    }
}
