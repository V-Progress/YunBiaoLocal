package com.yunbiao.cccm.net2.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Administrator on 2019/11/19.
 */
@Entity
public class ItemBlock {

    @Id
    private Long id;

    private String dateTime;

//    @Unique
    private String unique;

    private String url;

    private String name;

    private String path;

    @Generated(hash = 1325450605)
    public ItemBlock(Long id, String dateTime, String unique, String url,
            String name, String path) {
        this.id = id;
        this.dateTime = dateTime;
        this.unique = unique;
        this.url = url;
        this.name = name;
        this.path = path;
    }

    @Generated(hash = 1758219059)
    public ItemBlock() {
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ItemBlock{" +
                ", dateTime='" + dateTime + '\'' +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
