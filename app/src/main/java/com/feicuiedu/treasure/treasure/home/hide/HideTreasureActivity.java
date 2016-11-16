package com.feicuiedu.treasure.treasure.home.hide;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.baidu.mapapi.model.LatLng;
import com.feicuiedu.treasure.R;
import com.feicuiedu.treasure.commons.ActivityUtils;
import com.feicuiedu.treasure.treasure.TreasureRepo;
import com.feicuiedu.treasure.user.UserPrefs;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HideTreasureActivity extends AppCompatActivity implements HideTreasureView{

    private static final String KEY_TITLE = "key_title";
    private static final String KEY_LOCATION = "key_location";
    private static final String KEY_LATLNG = "key_latlng";
    private static final String KEY_ALTITUDE = "key_altitude";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_description)
    EditText etDescription;
    private ProgressDialog progressDialog;

    private ActivityUtils activityUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_treasure);
    }

    public static void open(@NonNull Context context, String title, String address, LatLng latLng, double altitude) {
        Intent intent = new Intent(context, HideTreasureActivity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_LOCATION, address);
        intent.putExtra(KEY_LATLNG, latLng);
        intent.putExtra(KEY_ALTITUDE, altitude);
        context.startActivity(intent);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
        activityUtils = new ActivityUtils(this);

        //toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle(getIntent().getStringExtra(KEY_TITLE));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * 实现选项菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hide_treasure,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_send:{
                // 点击，上传宝藏信息:去执行业务。利用MVP来进行

                Intent intent = getIntent();
                String title = intent.getStringExtra(KEY_TITLE);
                String address = intent.getStringExtra(KEY_LOCATION);
                double altitude = intent.getDoubleExtra(KEY_ALTITUDE, 0);
                LatLng latlng = intent.getParcelableExtra(KEY_LATLNG);
                int tokenid = UserPrefs.getInstance().getTokenid();
                String string = etDescription.getText().toString();

                HideTreasure hideTreasure = new HideTreasure();
                hideTreasure.setAltitude(altitude);
                hideTreasure.setDescription(string);
                hideTreasure.setLocation(address);
                hideTreasure.setTitle(title);
                hideTreasure.setTokenId(tokenid);
                hideTreasure.setLatitude(latlng.latitude);
                hideTreasure.setLongitude(latlng.longitude);

                new HideTreasurePresenter(this).hideTreasure(hideTreasure);

            }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


// 视图的具体实现
    @Override
    public void showProgress() {
        progressDialog = ProgressDialog.show(this, "宝藏上传", "宝藏正在上传，不要着急啊~");
    }

    @Override
    public void hideProgress() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void showMessage(String msg) {
        activityUtils.showToast(msg);
    }

    @Override
    public void navigationToHome() {
        finish();
        // 清除存储的宝藏数据
        TreasureRepo.getInstance().clear();
    }
}
