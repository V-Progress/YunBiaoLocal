package com.yunbiao.cccm.yunbiaolocal.io;

import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.util.Xml;

import com.yunbiao.cccm.yunbiaolocal.APP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XMLParse {
    /**
     * XML解析
     */
    public JSONObject parseXML(DocumentFile file) {
        JSONObject configuration = new JSONObject();
        InputStream fileInputStream = null;
        try {
            DocumentFile config = file.findFile("config.xml");
            if(config == null || (!config.exists())){
                return null;
            }
            fileInputStream = APP.getContext().getContentResolver().openInputStream(config.getUri());
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, "utf-8");
            for (int eventType = xmlPullParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlPullParser.next()) {
                if (eventType != XmlPullParser.START_TAG)
                    continue;
                switch (xmlPullParser.getName()) {
                    case ("start"):
                        configuration.put("start", xmlPullParser.nextText());
                        break;
                    case ("end"):
                        configuration.put("end", xmlPullParser.nextText());
                        break;
                    case ("isdelete"):
                        configuration.put("isdelete", xmlPullParser.nextText());
                        break;
                    case ("playlist"):
                        configuration.put("playlist", new JSONArray());
                        break;
                    case ("play"):
                        configuration.getJSONArray("playlist").put(new JSONObject());
                        break;
                    case ("playday"):
                        JSONArray play1 = configuration.getJSONArray("playlist");
                        play1.getJSONObject(play1.length() - 1).put("playday", xmlPullParser.nextText());
                        break;
                    case ("rules"):
                        JSONArray play2 = configuration.getJSONArray("playlist");
                        play2.getJSONObject(play2.length() - 1).put("rules", new JSONArray());
                        break;
                    case ("rule"):
                        xmlPullParser.nextTag();
                        JSONObject rule = new JSONObject();
                        if ("date".equals(xmlPullParser.getName()))
                            rule.put("date", xmlPullParser.nextText());
                        else if ("res".equals(xmlPullParser.getName()))
                            rule.put("res", xmlPullParser.nextText());
                        xmlPullParser.nextTag();
                        if ("date".equals(xmlPullParser.getName()))
                            rule.put("date", xmlPullParser.nextText());
                        else if ("res".equals(xmlPullParser.getName()))
                            rule.put("res", xmlPullParser.nextText());
                        JSONArray play3 = configuration.getJSONArray("playlist");
                        JSONArray rules = play3.getJSONObject(play3.length() - 1).getJSONArray("rules");
                        rules.put(rule);
                }
            }
            Log.d("XMLConfiguration", configuration.toString());
        } catch (XmlPullParserException | IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return configuration;
    }

    /**
     * XML解析
     */
    public JSONObject parseXML(File file) {
        JSONObject configuration = new JSONObject();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(file, "config.xml"));
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(fileInputStream, "utf-8");
            for (int eventType = xmlPullParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlPullParser.next()) {
                if (eventType != XmlPullParser.START_TAG)
                    continue;
                switch (xmlPullParser.getName()) {
                    case ("start"):
                        configuration.put("start", xmlPullParser.nextText());
                        break;
                    case ("end"):
                        configuration.put("end", xmlPullParser.nextText());
                        break;
                    case ("isdelete"):
                        configuration.put("isdelete", xmlPullParser.nextText());
                        break;
                    case ("playlist"):
                        configuration.put("playlist", new JSONArray());
                        break;
                    case ("play"):
                        configuration.getJSONArray("playlist").put(new JSONObject());
                        break;
                    case ("playday"):
                        JSONArray play1 = configuration.getJSONArray("playlist");
                        play1.getJSONObject(play1.length() - 1).put("playday", xmlPullParser.nextText());
                        break;
                    case ("rules"):
                        JSONArray play2 = configuration.getJSONArray("playlist");
                        play2.getJSONObject(play2.length() - 1).put("rules", new JSONArray());
                        break;
                    case ("rule"):
                        xmlPullParser.nextTag();
                        JSONObject rule = new JSONObject();
                        if ("date".equals(xmlPullParser.getName()))
                            rule.put("date", xmlPullParser.nextText());
                        else if ("res".equals(xmlPullParser.getName()))
                            rule.put("res", xmlPullParser.nextText());
                        xmlPullParser.nextTag();
                        if ("date".equals(xmlPullParser.getName()))
                            rule.put("date", xmlPullParser.nextText());
                        else if ("res".equals(xmlPullParser.getName()))
                            rule.put("res", xmlPullParser.nextText());
                        JSONArray play3 = configuration.getJSONArray("playlist");
                        JSONArray rules = play3.getJSONObject(play3.length() - 1).getJSONArray("rules");
                        rules.put(rule);
                }
            }
            Log.d("XMLConfiguration", configuration.toString());
        } catch (XmlPullParserException | IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return configuration;
    }
}
