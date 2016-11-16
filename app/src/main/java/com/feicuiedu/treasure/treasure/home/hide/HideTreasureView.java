package com.feicuiedu.treasure.treasure.home.hide;

/**
 * Created by 123 on 2016/11/15.
 */

public interface HideTreasureView {

    /**
     * 分析视图：
     * 1. 显示一个进度：告诉你正在上传
     * 2. 隐藏进度
     * 3. 显示信息：弹出吐司
     * 4. 成功上传以后，返回到之前的页面
     *
     */

    void showProgress();
    void hideProgress();
    void showMessage(String msg);
    void navigationToHome();

}
