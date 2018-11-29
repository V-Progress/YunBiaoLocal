package com.yunbiao.yunbiaolocal.devicectrl;

import android.media.AudioManager;

import com.yunbiao.yunbiaolocal.APP;
import com.yunbiao.yunbiaolocal.act.MainActivity;


public class SoundControl {

    public static Integer CURRENT_SOUND = 0;

    public static void setMusicSound(Double volume) {
        MainActivity mainActivity = APP.getMainActivity();
//        Main2Activity mainActivity = (MainActivity) LayoutRefresher.getInstance().getMainActivity();
        int max = mainActivity.audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//volume 0.0-1.0  volumD1-15
        Integer volumD = ((Double) (volume * max)).intValue();
        mainActivity.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumD, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
//		mainActivity.audioManager.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public static void stopCurrentVolume() {
        MainActivity mainActivity = APP.getMainActivity();
        if (mainActivity.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
            CURRENT_SOUND = mainActivity.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        mainActivity.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

//    /**
//     * 叫完号码之后让声音恢复正常
//     */
//    public static void restartCurrentVolume() {
//        HandleMessageUtils.getInstance().runInThread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                MainActivity mainActivity = HeartBeatClient.getInstance().getMainActivity();
//                if (CURRENT_SOUND > 0) {
//                    mainActivity.audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, CURRENT_SOUND, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//                }
//            }
//        });
//    }

    /**
     * 获取当前音量
     *
     * @return
     */
    public static int getCurrentVolume() {
        MainActivity mainActivity = APP.getMainActivity();
        int currentSound = mainActivity.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentSound;
    }

}
