package com.yunbiao.cccm.local;

import android.support.v4.provider.DocumentFile;
import android.util.Xml;

import com.yunbiao.cccm.APP;
import com.yunbiao.cccm.local.model.InsertDataModel;
import com.yunbiao.cccm.local.model.VideoDataModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLParse {

    /***
     * 解析File格式的XML
     * @param file
     * @return
     */
    public VideoDataModel parseVideoModel(File file) {
        VideoDataModel videoDataModel = new VideoDataModel();
        FileInputStream fileInputStream = null;
        try {
            if (!file.isFile()) {
                file = new File(file, "config.xml");
            }
            fileInputStream = new FileInputStream(file);
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, "utf-8");
            for (int eventType = xmlPullParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlPullParser.next()) {
                if (eventType != XmlPullParser.START_TAG)
                    continue;
                switch (xmlPullParser.getName()) {
                    case ("config"):
                        VideoDataModel.Config config = new VideoDataModel.Config();
                        videoDataModel.setConfig(config);
                        break;
                    case ("start"):
                        videoDataModel.getConfig().setStart(xmlPullParser.nextText());
                        break;
                    case ("end"):
                        videoDataModel.getConfig().setEnd(xmlPullParser.nextText());
                        break;
                    case ("playlist"):
                        List<VideoDataModel.Play> playList = new ArrayList<>();
                        videoDataModel.setPlaylist(playList);
                        break;
                    case ("play"):
                        videoDataModel.getPlaylist().add(new VideoDataModel.Play());
                        break;
                    case ("playday"):
                        List<VideoDataModel.Play> playList1 = videoDataModel.getPlaylist();
                        playList1.get(playList1.size() - 1).setPlayday(xmlPullParser.nextText());
                        break;
                    case ("rules"):
                        List<VideoDataModel.Play> playList2 = videoDataModel.getPlaylist();
                        playList2.get(playList2.size() - 1).setRules(new ArrayList<VideoDataModel.Play.Rule>());
                        break;
                    case ("rule"):
                        xmlPullParser.nextTag();
                        VideoDataModel.Play.Rule rule = new VideoDataModel.Play.Rule();

                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        xmlPullParser.nextTag();
                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        List<VideoDataModel.Play> playList3 = videoDataModel.getPlaylist();
                        List<VideoDataModel.Play.Rule> rules = playList3.get(playList3.size() - 1).getRules();
                        rules.add(rule);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return videoDataModel;
    }

    /***
     * 解析insert.xml
     * @param insertFile
     * @return
     */
    public InsertDataModel parseInsertModel(File insertFile){
        InsertDataModel insertDataModel = new InsertDataModel();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(insertFile);
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, "utf-8");
            for (int eventType = xmlPullParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlPullParser.next()) {
                if (eventType != XmlPullParser.START_TAG)
                    continue;
                switch (xmlPullParser.getName()) {
                    case ("config"):
                        InsertDataModel.Config config = new InsertDataModel.Config();
                        insertDataModel.setConfig(config);
                        break;
                    case ("layerType"):
                        insertDataModel.getConfig().setLayerType(xmlPullParser.nextText());
                        break;
                    case ("playlist"):
                        List<InsertDataModel.Play> playList = new ArrayList<>();
                        insertDataModel.setPlaylist(playList);
                        break;
                    case ("play"):
                        insertDataModel.getPlaylist().add(new InsertDataModel.Play());
                        break;
                    case ("playday"):
                        List<InsertDataModel.Play> playList1 = insertDataModel.getPlaylist();
                        playList1.get(playList1.size() - 1).setPlayday(xmlPullParser.nextText());
                        break;
                    case ("rules"):
                        List<InsertDataModel.Play> playList2 = insertDataModel.getPlaylist();
                        playList2.get(playList2.size() - 1).setRules(new ArrayList<InsertDataModel.Play.Rule>());
                        break;
                    case ("rule"):
                        xmlPullParser.nextTag();
                        InsertDataModel.Play.Rule rule = new InsertDataModel.Play.Rule();

                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("isCycle".equals(xmlPullParser.getName())) {
                            rule.setIsCycle(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        xmlPullParser.nextTag();
                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("isCycle".equals(xmlPullParser.getName())) {
                            rule.setIsCycle(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        List<InsertDataModel.Play> playList3 = insertDataModel.getPlaylist();
                        List<InsertDataModel.Play.Rule> rules = playList3.get(playList3.size() - 1).getRules();
                        rules.add(rule);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return insertDataModel;
    }
    /***
     * 解析File格式的XML
     * @param file
     * @return
     */
    public VideoDataModel parseVideoModel(DocumentFile file) {
        VideoDataModel videoDataModel = new VideoDataModel();
        InputStream fileInputStream = null;
        try {
            fileInputStream = APP.getContext().getContentResolver().openInputStream(file.getUri());
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, "utf-8");
            for (int eventType = xmlPullParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlPullParser.next()) {
                if (eventType != XmlPullParser.START_TAG)
                    continue;
                switch (xmlPullParser.getName()) {
                    case ("config"):
                        VideoDataModel.Config config = new VideoDataModel.Config();
                        videoDataModel.setConfig(config);
                        break;
                    case ("start"):
                        videoDataModel.getConfig().setStart(xmlPullParser.nextText());
                        break;
                    case ("end"):
                        videoDataModel.getConfig().setEnd(xmlPullParser.nextText());
                        break;
                    case ("playlist"):
                        List<VideoDataModel.Play> playList = new ArrayList<>();
                        videoDataModel.setPlaylist(playList);
                        break;
                    case ("play"):
                        videoDataModel.getPlaylist().add(new VideoDataModel.Play());
                        break;
                    case ("playday"):
                        List<VideoDataModel.Play> playList1 = videoDataModel.getPlaylist();
                        playList1.get(playList1.size() - 1).setPlayday(xmlPullParser.nextText());
                        break;
                    case ("rules"):
                        List<VideoDataModel.Play> playList2 = videoDataModel.getPlaylist();
                        playList2.get(playList2.size() - 1).setRules(new ArrayList<VideoDataModel.Play.Rule>());
                        break;
                    case ("rule"):
                        xmlPullParser.nextTag();
                        VideoDataModel.Play.Rule rule = new VideoDataModel.Play.Rule();

                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        xmlPullParser.nextTag();
                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        List<VideoDataModel.Play> playList3 = videoDataModel.getPlaylist();
                        List<VideoDataModel.Play.Rule> rules = playList3.get(playList3.size() - 1).getRules();
                        rules.add(rule);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return videoDataModel;
    }
    /***
     * 解析insert.xml
     * @param insertFile
     * @return
     */
    public InsertDataModel parseInsertModel(DocumentFile insertFile){
        InsertDataModel insertDataModel = new InsertDataModel();
        InputStream fileInputStream = null;
        try {
            fileInputStream = APP.getContext().getContentResolver().openInputStream(insertFile.getUri());
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, "utf-8");
            for (int eventType = xmlPullParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlPullParser.next()) {
                if (eventType != XmlPullParser.START_TAG)
                    continue;
                switch (xmlPullParser.getName()) {
                    case ("config"):
                        InsertDataModel.Config config = new InsertDataModel.Config();
                        insertDataModel.setConfig(config);
                        break;
                    case ("layerType"):
                        insertDataModel.getConfig().setLayerType(xmlPullParser.nextText());
                        break;
                    case ("playlist"):
                        List<InsertDataModel.Play> playList = new ArrayList<>();
                        insertDataModel.setPlaylist(playList);
                        break;
                    case ("play"):
                        insertDataModel.getPlaylist().add(new InsertDataModel.Play());
                        break;
                    case ("playday"):
                        List<InsertDataModel.Play> playList1 = insertDataModel.getPlaylist();
                        playList1.get(playList1.size() - 1).setPlayday(xmlPullParser.nextText());
                        break;
                    case ("rules"):
                        List<InsertDataModel.Play> playList2 = insertDataModel.getPlaylist();
                        playList2.get(playList2.size() - 1).setRules(new ArrayList<InsertDataModel.Play.Rule>());
                        break;
                    case ("rule"):
                        xmlPullParser.nextTag();
                        InsertDataModel.Play.Rule rule = new InsertDataModel.Play.Rule();

                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("isCycle".equals(xmlPullParser.getName())) {
                            rule.setIsCycle(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        xmlPullParser.nextTag();
                        if ("date".equals(xmlPullParser.getName())) {
                            rule.setDate(xmlPullParser.nextText());
                        } else if ("isCycle".equals(xmlPullParser.getName())) {
                            rule.setIsCycle(xmlPullParser.nextText());
                        } else if ("res".equals(xmlPullParser.getName())) {
                            rule.setRes(xmlPullParser.nextText());
                        }

                        List<InsertDataModel.Play> playList3 = insertDataModel.getPlaylist();
                        List<InsertDataModel.Play.Rule> rules = playList3.get(playList3.size() - 1).getRules();
                        rules.add(rule);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return insertDataModel;
    }
}
