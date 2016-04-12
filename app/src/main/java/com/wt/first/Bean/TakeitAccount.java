package com.wt.first.Bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by jing107 on 2016/3/25 0025.
 *
 * 账本类，对应于数据库中的账本表
 */
public class TakeitAccount extends BmobObject {

    /**
     * 账本名，可以为空
     */
    private String accountName;

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }
}
