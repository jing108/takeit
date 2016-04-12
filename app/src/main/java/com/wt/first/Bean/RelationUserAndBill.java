package com.wt.first.Bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by jing107 on 2016/3/25 0025.
 *
 * 用于联系用户表和账单表之间的多对多的关系
 */
public class RelationUserAndBill extends BmobObject {

    /**
     * 账单
     */
    private String billId;

    /**
     * 用户
     */
    private String userId;

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
