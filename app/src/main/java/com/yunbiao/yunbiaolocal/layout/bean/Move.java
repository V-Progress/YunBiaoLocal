package com.yunbiao.yunbiaolocal.layout.bean;

/**
 * Created by Administrator on 2018/11/27.
 */

public class Move{
    BGM bgmusic;
    String type;
    public class BGM{
        String music;

        public String getMusic() {
            return music;
        }

        public void setMusic(String music) {
            this.music = music;
        }

        @Override
        public String toString() {
            return "BGM{" +
                    "music='" + music + '\'' +
                    '}';
        }
    }

    public BGM getBgmusic() {
        return bgmusic;
    }

    public void setBgmusic(BGM bgmusic) {
        this.bgmusic = bgmusic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Move{" +
                "bgmusic=" + bgmusic +
                ", type='" + type + '\'' +
                '}';
    }
}