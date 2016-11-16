package com.feicuiedu.treasure.treasure.home.map;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.feicuiedu.treasure.R;
import com.feicuiedu.treasure.commons.ActivityUtils;
import com.feicuiedu.treasure.components.TreasureView;
import com.feicuiedu.treasure.treasure.Area;
import com.feicuiedu.treasure.treasure.Treasure;
import com.feicuiedu.treasure.treasure.TreasureRepo;
import com.feicuiedu.treasure.treasure.home.detail.TreasureDetailActivity;
import com.feicuiedu.treasure.treasure.home.hide.HideTreasureActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 放置在HomeActivity里面，主要展示的就是地图，宝藏展示、宝藏详情、埋藏宝藏都是在这个里面来进行
 */
public class MapFragment extends Fragment implements MapMvpView {

    @BindView(R.id.map_frame)
    FrameLayout mapFrame;
    @BindView(R.id.centerLayout)
    RelativeLayout centerLayout;
    @BindView(R.id.treasureView)
    TreasureView treasureView;
    @BindView(R.id.layout_bottom)
    FrameLayout layoutBottom;
    @BindView(R.id.hide_treasure)
    RelativeLayout hideTreasure;
    @BindView(R.id.btn_HideHere)
    Button btnHideHere;
    @BindView(R.id.tv_currentLocation)
    TextView tvCurrentLocation;
    @BindView(R.id.iv_located)
    ImageView ivLocated;
    @BindView(R.id.et_treasureTitle)
    EditText etTreasureTitle;

    private MapView mapView;
    private BaiduMap baiduMap;
    private Unbinder bind;
    private LocationClient locationClient;
    private static LatLng myLocation;
    private ActivityUtils activityUtils;

    private LatLng target;// 用来暂时保存一下当前地图的位置，方便我们判断地图的位置有没有变化

    private static String myAddress;

    private boolean isFirstLocate = true;// 这个主要是用来判断是不是第一进来的时候的定位
    private String address;
    private GeoCoder geoCoder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        bind = ButterKnife.bind(this, view);
        activityUtils = new ActivityUtils(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化百度地图
        initBaiduMap();

        // 初始化定位相关
        initLocation();

        // 初始化地理编码相关
        initGeoCoder();
    }

    private void initGeoCoder() {
        // 创建地理编码查询变量
        geoCoder = GeoCoder.newInstance();
        // 设置地理编码的监听
        geoCoder.setOnGetGeoCodeResultListener(geoCoderResultListener);
    }

    private OnGetGeoCoderResultListener geoCoderResultListener = new OnGetGeoCoderResultListener() {

        // 地理编码：地址--> 经纬度
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

        }

        // 反地理编码：经纬度-->地址
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
            if (reverseGeoCodeResult == null) {
                address = "未知";
                return;
            }
            // 没有问题的时候
            if (reverseGeoCodeResult.error == SearchResult.ERRORNO.NO_ERROR) {
                address = "未知";
            }
            // 得到反地理编码的结果
            address = reverseGeoCodeResult.getAddress();
            // 将结果展示到卡片标题录入的地址上
            tvCurrentLocation.setText(address);
        }
    };

    private void initLocation() {
        /**
         * 1. 开启定位图层
         * 2. 定位类的实例化
         * 3. 定位进行一些相关的设置
         * 4. 设置定位的监听
         * 5. 开始定位（为了处理某些机型初始化定位不成功，需要重新请求定位）
         */
        baiduMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(getContext());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开GPS
        option.setCoorType("bd09ll");// 设置百度坐标类型
        option.setIsNeedAddress(true);// 设置需要地址信息
        locationClient.setLocOption(option);// 要把我们做的设置给LocationClient
        locationClient.registerLocationListener(locationListener);// 设置百度地图的监听
        locationClient.start();// 开始定位
        locationClient.requestLocation();// 为了处理某些机型初始化定位不成功，需要重新请求定位
    }

    private BDLocationListener locationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            // 处理一下定位
            /**
             * 1. 判断有没有定位成功
             * 2. 获得定位信息(经纬度)
             * 3. 定位信息设置到地图上
             * 4. 移动到定位的位置去
             *      第一次进入：一进到项目里面就会移动到定位的位置去
             *      点击定位按钮：其他时候如果需要定位
             */
            if (bdLocation == null) {
                locationClient.requestLocation();
                return;
            }
            double lng = bdLocation.getLongitude();// 经度
            double lat = bdLocation.getLatitude();//纬度

            // 拿到定位的位置
            myLocation = new LatLng(lat, lng);

            // 拿到定位的地址
            myAddress = bdLocation.getAddrStr();

            MyLocationData myLocationData = new MyLocationData.Builder()
                    .longitude(lng)
                    .latitude(lat)
                    .accuracy(100f)// 精度，定位圈的大小
                    .build();

            baiduMap.setMyLocationData(myLocationData);
            if (isFirstLocate) {
                moveToMyLocation();
                isFirstLocate = false;
            }
        }
    };

    public static LatLng getMyLocation() {
        return myLocation;
    }

    public static String getMyAddress() {
        return myAddress;
    }

    private void initBaiduMap() {

        // 查看百度地图的ＡＰＩ

        // 百度地图状态
        MapStatus mapStatus = new MapStatus.Builder()
                .overlook(0)// 0--(-45) 地图的俯仰角度
                .zoom(15)// 3--21 缩放级别
                .build();

        BaiduMapOptions options = new BaiduMapOptions()
                .mapStatus(mapStatus)// 设置地图的状态
                .compassEnabled(true)// 指南针
                .zoomGesturesEnabled(true)// 设置允许缩放手势
                .rotateGesturesEnabled(true)// 旋转
                .scaleControlEnabled(false)// 不显示比例尺控件
                .zoomControlsEnabled(false);// 不显示缩放控件

        // 创建一个MapView
        mapView = new MapView(getContext(), options);

        // 在当前的Layout上面添加MapView
        mapFrame.addView(mapView, 0);

        // MapView 的控制器
        baiduMap = mapView.getMap();

        // 怎么对地图状态进行监听？
        baiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
        baiduMap.setOnMarkerClickListener(markerListener);

    }

    // 地图类型的切换（普通视图--卫星视图）
    @OnClick(R.id.tv_satellite)
    public void switchMapType() {
        // 先获得当前的类型
        int type = baiduMap.getMapType();
        type = type == BaiduMap.MAP_TYPE_NORMAL ? BaiduMap.MAP_TYPE_SATELLITE : BaiduMap.MAP_TYPE_NORMAL;
        baiduMap.setMapType(type);
    }

    // 定位实现
    @OnClick(R.id.tv_located)
    public void moveToMyLocation() {

        // 将地图位置设置成定位的位置
        MapStatus mapStatus = new MapStatus.Builder()
                .target(myLocation)
                .rotate(0)// 地图位置摆正
                .zoom(19)
                .build();
        // 更新地图状态
        MapStatusUpdate update = MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.animateMapStatus(update);
    }

    @OnClick(R.id.tv_compass)
    public void switchCompass() {
        /**
         * 指南针是地图视图的一个图标
         */
        boolean isCompass = baiduMap.getUiSettings().isCompassEnabled();
        baiduMap.getUiSettings().setCompassEnabled(!isCompass);
    }

    @OnClick({R.id.iv_scaleUp, R.id.iv_scaleDown})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_scaleUp:
                baiduMap.setMapStatus(MapStatusUpdateFactory.zoomIn());// 放大
                break;
            case R.id.iv_scaleDown:
                baiduMap.setMapStatus(MapStatusUpdateFactory.zoomOut());// 缩小
                break;
        }
    }

    @OnClick(R.id.treasureView)
    public void clickTreasureView() {
        // 跳转到详情页面，宝藏传递过去
        int id = currentMarker.getExtraInfo().getInt("id");
        Treasure treasure = TreasureRepo.getInstance().getTreasure(id);
        TreasureDetailActivity.open(getContext(), treasure);
    }

    @OnClick(R.id.hide_treasure)
    public void clickHideTreasure() {
        // 处理埋藏宝藏的卡片的标题录入和跳转
        // 找到我们输入的宝藏标题
        String title = etTreasureTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            activityUtils.showToast("请输入宝藏标题");
            return;
        }
        // 跳转到埋藏宝藏详细页面
        LatLng latLng = baiduMap.getMapStatus().target;
        HideTreasureActivity.open(getContext(), title, address, latLng, 0);

    }

    // 百度地图状态的监听
    private BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {

        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            // 当地图的状态发生变化时，动态的去获取某区域内的宝藏数据

            // 地图状态发生变化了
            LatLng target = mapStatus.target;
            // 判断位置有没有变化
            if (target != MapFragment.this.target) {

                // 位置发生变化了，去进行此位置周边的宝藏数据获取，提供方法来进行
                updateMapArea();

                // 在宝藏埋藏模式下
                if (uiMode == UI_MODE_HIDE) {

                    // 设置反地理编码的位置
                    ReverseGeoCodeOption option = new ReverseGeoCodeOption();
                    option.location(target);

                    // 发起反地理编码
                    geoCoder.reverseGeoCode(option);

                    // 反弹动画
                    YoYo.with(Techniques.Bounce).duration(1000).playOn(btnHideHere);
                    YoYo.with(Techniques.Bounce).duration(1000).playOn(ivLocated);
                    YoYo.with(Techniques.FadeIn).duration(1000).playOn(btnHideHere);

                }
                // 将位置更新为变化后的位置
                MapFragment.this.target = target;
            }
        }
    };

    // 位置发生变化了，区域也变化了，更新区域，动态获取区域内数据
    private void updateMapArea() {

        MapStatus mapStatus = baiduMap.getMapStatus();

        // 获取经纬度
        double lng = mapStatus.target.longitude;
        double lat = mapStatus.target.latitude;

        Area area = new Area();
        area.setMaxLat(Math.ceil(lat));//经纬度的向上取整
        area.setMaxLng(Math.ceil(lng));
        area.setMinLat(Math.floor(lat));// 经纬度的向下取整
        area.setMinLng(Math.floor(lng));

        // 根据区域来进行数据的获取
        new MapPresenter(this).getTreasure(area);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind.unbind();
    }

    @Override
    public void showMessage(String msg) {
        activityUtils.showToast(msg);
    }

    @Override
    public void setData(List<Treasure> list) {
        for (Treasure treasure : list) {
            LatLng lat = new LatLng(treasure.getLatitude(), treasure.getLongitude());
            // 要在经纬度处添加覆盖物Marker
            addMarker(lat, treasure.getId());
        }
    }

    private BitmapDescriptor dot_click = BitmapDescriptorFactory.fromResource(R.drawable.treasure_expanded);
    private BitmapDescriptor dot = BitmapDescriptorFactory.fromResource(R.drawable.treasure_dot);

    // 添加宝藏的覆盖物，每一个覆盖物中都包含各自的宝藏信息
    private void addMarker(LatLng lat, int treasureId) {

        MarkerOptions options = new MarkerOptions();
        options.position(lat);// 设置位置
        options.icon(dot);// 设置覆盖物的图标
        options.anchor(0.5f, 0.5f);// 设置描点

        Bundle bundle = new Bundle();
        bundle.putInt("id", treasureId);
        options.extraInfo(bundle);
        baiduMap.addOverlay(options);
    }

    private Marker currentMarker;

    private BaiduMap.OnMarkerClickListener markerListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {

            if (currentMarker != null) {
                currentMarker.setVisible(true);
            }
            currentMarker = marker;
            currentMarker.setVisible(false);

            InfoWindow infoWindow = new InfoWindow(dot_click, marker.getPosition(), 0, infowindowListener);
            baiduMap.showInfoWindow(infoWindow);

            // 取出当前的Marker的宝藏信息
            int id = marker.getExtraInfo().getInt("id");
            Treasure treasure = TreasureRepo.getInstance().getTreasure(id);
            treasureView.bindTreasure(treasure);

            /**
             * 切换到宝藏选中视图
             */
            changeUIMode(UI_MODE_SECLECT);

            return false;
        }
    };
    private InfoWindow.OnInfoWindowClickListener infowindowListener = new InfoWindow.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick() {
            // 切换回普通视图
            changeUIMode(UI_MODE_NORMAL);

        }
    };

    private static final int UI_MODE_NORMAL = 0;// 普通的视图
    private static final int UI_MODE_SECLECT = 1;// 宝藏选中视图
    private static final int UI_MODE_HIDE = 2;// 埋藏宝藏视图

    private int uiMode = UI_MODE_NORMAL;

    // 提供一个方法：用来切换视图
    private void changeUIMode(int uiMode) {

        if (this.uiMode == uiMode) {
            return;
        }
        this.uiMode = uiMode;
        switch (uiMode) {

            // 普通的视图
            case UI_MODE_NORMAL: {
                if (currentMarker != null) {
                    currentMarker.setVisible(true);
                }
                baiduMap.hideInfoWindow();
                layoutBottom.setVisibility(View.GONE);
                centerLayout.setVisibility(View.GONE);
            }
            break;
            // 宝藏选中视图
            case UI_MODE_SECLECT: {
                layoutBottom.setVisibility(View.VISIBLE);
                treasureView.setVisibility(View.VISIBLE);
                centerLayout.setVisibility(View.GONE);
                hideTreasure.setVisibility(View.GONE);
            }
            break;
            // 埋藏宝藏的视图
            case UI_MODE_HIDE: {
                centerLayout.setVisibility(View.VISIBLE);
                layoutBottom.setVisibility(View.GONE);
                btnHideHere.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layoutBottom.setVisibility(View.VISIBLE);
                        hideTreasure.setVisibility(View.VISIBLE);
                        treasureView.setVisibility(View.GONE);
                    }
                });
            }
            break;
        }
    }

    public void switchToHideTreasure() {
        changeUIMode(UI_MODE_HIDE);
    }

    // 按下Back键来调用
    public boolean clickBackPressed(){
        if (this.uiMode!=UI_MODE_NORMAL){
            changeUIMode(UI_MODE_NORMAL);
            return false;
        }
        return true;
    }
}
