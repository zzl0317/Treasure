package com.feicuiedu.treasure.treasure.home.detail;


import java.util.List;

/**
 * Created by 123 on 2016/9/22.
 */
public interface TreasureDetialView{

    // 视图的方法
    void showMessage(String msg);// 显示信息

    void setData(List<TreasureDetailResult> results);// 设置数据

}
