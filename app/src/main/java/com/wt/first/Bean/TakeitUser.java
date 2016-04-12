package com.wt.first.Bean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by jing107 on 2016/3/25 0025.
 */
public class TakeitUser extends BmobUser {

    /**
     * 每个用户只属于一个账本
     */
    private TakeitAccount account;

    /**
     * 用户的金币数
     */
    private Integer coins;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private BmobFile avatar;

    /**
     *用户在该账单中的金钱数
     * 当用户有所属账本之后，该字段才有效
     */
    private Float money;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public TakeitAccount getAccount() {
        return account;
    }

    public void setAccount(TakeitAccount account) {
        this.account = account;
    }

    public Integer getCoins() {
        return coins;
    }

    public void setCoins(Integer coins) {
        this.coins = coins;
    }

    public Float getMoney() {
        return money;
    }

    public void setMoney(Float money) {
        this.money = money;
    }
}
