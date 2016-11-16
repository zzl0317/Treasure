package com.feicuiedu.treasure.user.account;

public interface AccoutView {

    void showProgress();

    void hideProgress();

    void showMessage(String msg);

    /** 更新头像*/
    void updatePhoto(String url);
}
