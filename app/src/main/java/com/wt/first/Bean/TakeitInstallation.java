package com.wt.first.Bean;

import android.content.Context;

import cn.bmob.v3.BmobInstallation;

/**
 * Created by jing107 on 2016/3/28 0028.
 */
public class TakeitInstallation extends BmobInstallation {

    /**
     * 用户id，用于将用户和登录设备绑定起来，推送
     * 当用户使用 用户名+密码 方式登录，更新uid和设备id
     */
    private String uid;

    public TakeitInstallation(Context context) {
        super(context);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
