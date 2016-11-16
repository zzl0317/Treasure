package com.feicuiedu.treasure.treasure.home.map;

import com.feicuiedu.treasure.treasure.Treasure;

import java.util.List;

/**
 * Created by 123 on 2016/11/11.
 */

public interface MapMvpView {

    void showMessage(String msg);

    void setData(List<Treasure> list);
}
