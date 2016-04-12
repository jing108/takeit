package com.wt.first.Bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by jing107 on 2016/3/25 0025.
 */
public class TakeitType extends BmobObject {

    /**
     * 类别名字，不能为空
     */
    private String typeName;

    /**
     * 类别图片，后续扩展
     */

    /**
     * 类别所属账本，后续扩展
     */

    /**
     * 类别权重，主要用于动态显示，权重大的显示在显眼的位置，后续扩展
     */

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
