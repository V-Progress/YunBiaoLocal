package com.yunbiao.cccm.local.model;

import java.util.List;

/**
 * Created by Administrator on 2018/12/6.
 */

public class InsertDataModel {

    private Config config;
    private List<Play> playlist;

    public static class Config{
        private String layerType;

        public String getLayerType() {
            return layerType;
        }

        public void setLayerType(String layerType) {
            this.layerType = layerType;
        }

        @Override
        public String toString() {
            return "Config{" +
                    ", layerType='" + layerType + '\'' +
                    '}';
        }
    }

    public static class Play{
        private String playday;
        private List<Rule> rules;

        public static class Rule{
            private String date;
            private String isCycle;
            private String res;

            public String getIsCycle() {
                return isCycle;
            }

            public void setIsCycle(String isCycle) {
                this.isCycle = isCycle;
            }

            public String getDate() {
                return date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getRes() {
                return res;
            }

            public void setRes(String res) {
                this.res = res;
            }

            @Override
            public String toString() {
                return "Rule{" +
                        "date='" + date + '\'' +
                        ", isCycle='" + isCycle + '\'' +
                        ", res='" + res + '\'' +
                        '}';
            }
        }

        public String getPlayday() {
            return playday;
        }

        public void setPlayday(String playday) {
            this.playday = playday;
        }

        public List<Rule> getRules() {
            return rules;
        }

        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }

        @Override
        public String toString() {
            return "Play{" +
                    "playday='" + playday + '\'' +
                    ", rules=" + rules +
                    '}';
        }
    }

    public List<Play> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Play> playlist) {
        this.playlist = playlist;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "InsertDataModel{" +
                "config=" + config +
                ", playlist=" + playlist +
                '}';
    }
}
