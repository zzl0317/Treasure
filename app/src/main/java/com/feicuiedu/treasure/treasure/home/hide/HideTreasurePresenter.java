package com.feicuiedu.treasure.treasure.home.hide;

import com.feicuiedu.treasure.net.NetClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 123 on 2016/11/15.
 */
// 业务类：主要是进行宝藏数据的上传
public class HideTreasurePresenter {

    private Call<HideTreasureResult> resultCall;
    private HideTreasureView treasureView;

    public HideTreasurePresenter(HideTreasureView treasureView) {
        this.treasureView = treasureView;
    }

    // 执行业务的方法
    public void hideTreasure(HideTreasure hideTreasure) {

        treasureView.showProgress();

        resultCall = NetClient.getInstance().getTreasureApi().hideTreasure(hideTreasure);
        resultCall.enqueue(callback);
    }

    private Callback<HideTreasureResult> callback = new Callback<HideTreasureResult>() {

        @Override
        public void onResponse(Call<HideTreasureResult> call, Response<HideTreasureResult> response) {
            treasureView.hideProgress();
            if (response.isSuccessful() && response != null) {
                HideTreasureResult treasureResult = response.body();
                if (treasureResult == null) {
                    treasureView.showMessage("可能发生了错误");
                    return;
                }
                /**
                 * 有数据，code=1 是不是才是真正的上传成功了
                 */
                if (treasureResult.getCode() == 1) {
                    treasureView.navigationToHome();
                }
                treasureView.showMessage(treasureResult.getMsg());
            }
        }

        @Override
        public void onFailure(Call<HideTreasureResult> call, Throwable t) {
            treasureView.hideProgress();
            treasureView.showMessage(t.getMessage());
        }
    };
}
