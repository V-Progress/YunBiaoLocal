package com.yunbiao.cccm.net.resolve;

import android.util.Xml;

import com.yunbiao.cccm.net.model.VideoDataModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLParse {

    /***
     * 直接解析XML内容（解析网络XML数据）
     * @param configXML
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public VideoDataModel parseVideoModel(String configXML) throws XmlPullParserException, IOException {
        InputStream inputStream = new ByteArrayInputStream(configXML.getBytes());
        VideoDataModel videoDataModel = new VideoDataModel();
        XmlPullParser xmlPullParser = Xml.newPullParser();
        xmlPullParser.setInput(inputStream, "utf-8");
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
                case ("playurl"):
                    videoDataModel.getConfig().setPlayurl(xmlPullParser.nextText());
                    break;
                case ("isdelete"):
                    videoDataModel.getConfig().setIsdelete(xmlPullParser.nextText());
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
        return videoDataModel;
    }

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
}
