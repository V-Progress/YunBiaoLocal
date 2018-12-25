package com.yunbiao.cccm.devicectrl;

import android.media.AudioManager;

import com.yunbiao.cccm.APP;


public class SoundControl {

    public static Integer CURRENT_SOUND = 0;

    public static void setMusicSound(Double volume) {
        AudioManager audioManager = APP.getAudioManager();
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Integer volumD = ((Double) (volume * max)).intValue();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumD, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
    }

    public static void stopCurrentVolume() {
        AudioManager audioManager = APP.getAudioManager();
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
            CURRENT_SOUND = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    /**
     * 获取当前音量
     *
     * @return
     */
    public static int getCurrentVolume() {
        AudioManager audioManager = APP.getAudioManager();
        int currentSound = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentSound;
    }

}
