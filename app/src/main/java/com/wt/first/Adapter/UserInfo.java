package com.wt.first.Adapter;

import android.graphics.Bitmap;

/**
 * Created by jing107 on 2016/4/2 0002.
 */
public class UserInfo {

    private boolean beSelected;
//    private int id;
    private Bitmap bitmap;
    private String userName;

    public boolean isBeSelected() {
        return beSelected;
    }

    public void setBeSelected(boolean beSelected) {
        this.beSelected = beSelected;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
