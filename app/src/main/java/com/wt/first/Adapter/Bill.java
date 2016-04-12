package com.wt.first.Adapter;

/**
 * Created by jing107 on 2016/3/31 0031.
 */
public class Bill {

    //日期
    private String mDate;
    //时间
    private String mTime;
    //图片id
    private int id;

    //金额
    private String money;
    //说明
    private String content;

    public Bill(String mDate, String mTime, int id, String money) {
        this.mDate = mDate;
        this.mTime = mTime;
        this.id = id;
        this.money = money;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getmDate() {
        return mDate;
    }

    public String getmTime() {
        return mTime;
    }

    public int getId() {
        return id;
    }

    public String getMoney() {
        return money;
    }
}
