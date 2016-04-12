package com.wt.first.Bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by jing107 on 2016/3/25 0025.
 *
 * 账单类
 */
public class TakeitBill extends BmobObject {

    /**
     * 账单发起人
     */
    private TakeitUser user;

    /**
     * 账单所属类别
     */
    private Integer type;

    /**
     * 账单所属账本
     */
    private TakeitAccount account;

    /**
     * 该账单金额
     */
    private Float money;

    /**
     * 账单说明
     */
    private String content;


    public TakeitUser getUser() {
        return user;
    }

    public void setUser(TakeitUser user) {
        this.user = user;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public TakeitAccount getAccount() {
        return account;
    }

    public void setAccount(TakeitAccount account) {
        this.account = account;
    }

    public Float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
