package com.map.library.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.DriveWayView;
import com.amap.api.navi.view.NextTurnTipView;
import com.amap.api.navi.view.OverviewButtonView;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.library.R;
import com.amap.library.R2;
import com.blankj.utilcode.util.ConvertUtils;
import com.map.library.DistanceUtils;
import com.map.library.TimeHelper;
import com.map.library.base.BaseNaviFragment;
import com.map.library.data.RouteData;
import com.map.library.data.RouteNaviSettingData;
import com.map.library.mvp.IRouteNavi;
import com.map.library.mvp.presenter.RouteNaviPresenterImpl;
import com.yisingle.amapview.lib.base.view.marker.BaseMarkerView;
import com.yisingle.amapview.lib.view.PathPlaningView;
import com.yisingle.amapview.lib.view.PointMarkerView;
import com.yisingle.amapview.lib.viewholder.MapInfoWindowViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * Created by jikun on 17/6/29.
 */

public class NaviFragment extends BaseNaviFragment<RouteNaviPresenterImpl> implements IRouteNavi.RouteView {

    private String TAG = NaviFragment.class.getSimpleName();

    @BindView(R2.id.flmapContent)
    RelativeLayout flmapContent;


    @BindView(R2.id.textureMapView)
    TextureMapView textureMapView;

    @BindView(R2.id.flNavContent)
    RelativeLayout flNavContent;


    @BindView(R2.id.navi_view)
    AMapNaviView naviView;


    @BindView(R2.id.mZoomInIntersectionView)
    ImageView mZoomInIntersectionView;


    @BindView(R2.id.driverWayView)
    DriveWayView driverWayView;


    @BindView(R2.id.tv_distance)
    TextView tv_distance;

    @BindView(R2.id.tv_road_name)
    TextView tv_road_name;

    @BindView(R2.id.myDirectionView)
    NextTurnTipView myNextTurnView;//转向图标


    @BindView(R2.id.ll_little_navi_title)
    LinearLayout ll_little_navi_title;


    @BindView(R2.id.tv_little_road_name)
    TextView tv_little_road_name;

    @BindView(R2.id.tv_distance_little)
    TextView tv_distance_little;


    @BindView(R2.id.nv_little_turn_view)
    NextTurnTipView nv_little_turn_view;//转向图标


    @BindView(R2.id.ib_switch)
    ImageButton ib_switch;


    @BindView(R2.id.rl_map_loading)
    RelativeLayout rl_map_loading;

    @BindView(R2.id.map_progressBar)
    ProgressBar map_progressBar;


    @BindView(R2.id.bt_map_error)
    Button bt_map_error;

    @BindView(R2.id.tv_all_ditance)
    TextView tv_all_ditance;

    @BindView(R2.id.tv_all_time)
    TextView tv_all_time;


    @BindView(R2.id.rl_navi_loading)
    RelativeLayout rl_navi_loading;

    @BindView(R2.id.navi_progressBar)
    ProgressBar navi_progressBar;


    @BindView(R2.id.bt_navi_error)
    Button bt_navi_error;

    private OnNaviFragmentCallBack callBack;

    private RouteNaviSettingData currentSettingData;

    @BindView(R2.id.overviewButtonView)
    OverviewButtonView overviewButtonView;


    protected PathPlaningView<String, String> carToTargetPathPlaningView;


    protected PathPlaningView<String, String> startToEndPathPlaningView;


    @Override
    public AMapNaviView getNaviView() {
        return naviView;
    }


    private static final String BUNDLE_KEY = "RouteNaviSettingData";


    @Override
    public void initViewsinitNaviViewOver() {

        myNextTurnView.setCustomIconTypes(getResources(), customIconTypes);
        nv_little_turn_view.setCustomIconTypes(getResources(), customIconTypes);


        mAMapNavi.addAMapNaviListener(this);
        getBundleDataDoShowRoute();
        initOverviewButtonView();


        carToTargetPathPlaningView = new PathPlaningView.Builder(getContext(), getaMap())
                .setStartMarkBuilder(
                        new PointMarkerView.Builder(getContext(), getaMap())
                                .setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_car))
                )
                .setEndMarkBuilder(
                        new PointMarkerView.Builder(getContext(), getaMap())
                                .setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start))

                )
                .create();

//        carToTargetPathPlaningView.bingStartInfoWindowView(new BaseMarkerView.InfoWindowView<String>() {
//            @Override
//            public void bindData(MapInfoWindowViewHolder viewHolder, String data) {
//
//            }
//        });

        startToEndPathPlaningView = new PathPlaningView.Builder(getContext(), getaMap())
                .setStartMarkBuilder(
                        new PointMarkerView.Builder(getContext(), getaMap())
                                .setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_start))
                )
                .setEndMarkBuilder(
                        new PointMarkerView.Builder(getContext(), getaMap())
                                .setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_end))
                )
                .create();
    }


    private void initOverviewButtonView() {
        Bitmap bitmap2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.drive_map_icon_preview_portrait_day);

        Bitmap bitmap1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.drive_map_icon_preview_portrait_day_checked);
        overviewButtonView.reDrawBackground(bitmap1, bitmap2);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_navi;
    }

    @Override
    protected RouteNaviPresenterImpl createPresenter() {
        return new RouteNaviPresenterImpl(getContext(), this);
    }

    @Override
    protected boolean isregisterEventBus() {
        return false;
    }


    @Override
    public void startNav(NaviLatLng startLatlng, NaviLatLng endLatlng) {

        if (null != naviView && null != naviView.getMap()) {
            naviView.getMap().clear();
        }
        if (null != mAMapNavi) {
            mAMapNavi.stopNavi();
        }

        flmapContent.setVisibility(View.GONE);

        flNavContent.setVisibility(View.VISIBLE);
        super.startNav(startLatlng, endLatlng);
    }


    @OnClick(R2.id.ib_switch)
    public void switchRoad() {
        if (null != mAMapNavi) {
            mAMapNavi.switchParallelRoad();
        }
    }

    @OnClick(R2.id.ib_close)
    public void closeNaviButton() {
        if (null != callBack) {
            callBack.onCloseNaviButton();
        }
    }


    public static NaviFragment newInstance(RouteNaviSettingData data) {

        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_KEY, data);

        NaviFragment fragment = new NaviFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void getBundleDataDoShowRoute() {
        Bundle bundle = getArguments();
        RouteNaviSettingData data = bundle.getParcelable(BUNDLE_KEY);
        if (null != bundle && null != data) {
            showRoute(data);

        }
    }


    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        //Log.e("测试代码", TAG + "测试代码onNaviInfoUpdate");
        if (naviInfo != null) {

            String distance = DistanceUtils.getDistance(naviInfo.getCurStepRetainDistance());
            String roadName = naviInfo.getNextRoadName();
            String allditance = DistanceUtils.getDistance(naviInfo.getPathRetainDistance());
            String allTime = TimeHelper.secToTime(naviInfo.getPathRetainTime());
            tv_distance.setText(distance);
            tv_road_name.setText(roadName);
            myNextTurnView.setIconType(naviInfo.getIconType());


            tv_distance_little.setText(distance);
            tv_little_road_name.setText(roadName);
            nv_little_turn_view.setIconType(naviInfo.getIconType());


            tv_all_ditance.setText("剩余" + allditance);

            tv_all_time.setText("预计" + allTime);


        }

    }


    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

        ll_little_navi_title.setVisibility(View.VISIBLE);
        mZoomInIntersectionView.setImageBitmap(aMapNaviCross.getBitmap());


    }

    @Override
    public void hideCross() {
        ll_little_navi_title.setVisibility(View.GONE);


        Log.e("测试代码", TAG + "测试代码hideCross");
    }


    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
        driverWayView.setVisibility(View.VISIBLE);
        driverWayView.loadDriveWayBitmap(aMapLaneInfo);

    }


    @Override
    public void hideLaneInfo() {
        driverWayView.setVisibility(View.GONE);
        Log.e("测试代码", TAG + "测试代码hideLaneInfo");
    }


    @Override
    public void notifyParallelRoad(int i) {
        // parallelRoadType - 0表示隐藏 1 表示显示主路 2 表示显示辅路
        Log.e("测试代码", TAG + "测试代码notifyParallelRoad");
        switch (i) {
            //0表示隐藏
            case 0:
                ib_switch.setVisibility(View.GONE);
                break;
            //1 表示显示主路
            case 1:
                //因为现在在主路所以控件显示在辅路
                ib_switch.setVisibility(View.VISIBLE);
                ib_switch.setSelected(true);
                break;
            //2 表示显示辅路
            case 2:
                ib_switch.setVisibility(View.VISIBLE);
                ib_switch.setSelected(false);
                //因为现在在辅路所以控件显示在主路
                break;

            default:
                break;
        }

    }


    @Override
    protected void showNaviLoading() {
        rl_navi_loading.setVisibility(View.VISIBLE);
        navi_progressBar.setVisibility(View.VISIBLE);
        bt_navi_error.setVisibility(View.GONE);
    }

    @Override
    protected void dimissNaviLoading() {
        rl_navi_loading.setVisibility(View.VISIBLE);
        navi_progressBar.setVisibility(View.GONE);
        bt_navi_error.setVisibility(View.GONE);
    }

    @Override
    protected void naviBeginSuccess() {
        rl_navi_loading.setVisibility(View.GONE);
        navi_progressBar.setVisibility(View.GONE);
        bt_navi_error.setVisibility(View.GONE);
    }

    @Override
    protected void naviBeginFailed() {
        rl_navi_loading.setVisibility(View.VISIBLE);
        navi_progressBar.setVisibility(View.GONE);
        bt_navi_error.setVisibility(View.VISIBLE);
    }

    @OnClick(R2.id.bt_navi_error)
    public void onClickErrorNavi() {
        startNav(mStartLatlng, mEndLatlng);
    }


//---地图加载-----------------------------------------------------------------------------------------------------------------------------//


    /**
     * 定位点到终点的地图路径规划
     *
     * @param naviSettingData
     */
    public void showRoute(RouteNaviSettingData naviSettingData) {
        currentSettingData = naviSettingData;
        if (null != mAMapNavi) {
            mAMapNavi.stopNavi();
        }
        flmapContent.setVisibility(View.VISIBLE);

        flNavContent.setVisibility(View.GONE);

        switch (naviSettingData.getType()) {
            case RouteNaviSettingData.TYPE.DO_NO_THING:
                break;
            case RouteNaviSettingData.TYPE.DO_ONE_CAR_TO_START_ROUTE:
                mPresenter.drawSingleRoute(getContext(), naviSettingData.getStartLatLng());


                break;
            case RouteNaviSettingData.TYPE.DO_ONE_START_TO_END_ROUTE:

                mPresenter.drawSingleRoute(getContext(), naviSettingData.getStartLatLng(), naviSettingData.getEndLatLng());
                break;
            case RouteNaviSettingData.TYPE.DO_TWO_ROUTE:
                mPresenter.drawTwoRoute(getContext(), naviSettingData.getTargetLatLng(), naviSettingData.getStartLatLng(), naviSettingData.getEndLatLng());
                break;
            default:
                break;
        }


    }


    @Override
    protected TextureMapView getTextureMapView() {
        return textureMapView;
    }

    @Override
    protected void initMapLoad() {
        setMapUiSetting();

    }

    @Override
    public void ondrawRouteSuccess(List<RouteData> routeResultList) {
        showMapLoadingViewSuccess();
        boolean isMove = true;
        for (int i = 0; i < routeResultList.size(); i++) {
            RouteData routeData = routeResultList.get(i);
            if (routeData.getType() == RouteData.Type.CAR_TO_TARGET) {
                carToTargetPathPlaningView.draw(routeData.getDriveRouteResult());
                if (isMove) {
                    moveToCamera(routeData.getDriveRouteResult().getStartPos(), routeData.getDriveRouteResult().getTargetPos());
                    isMove = false;
                }
            } else {
                startToEndPathPlaningView.draw(routeData.getDriveRouteResult());
                if (isMove) {
                    moveToCamera(routeData.getDriveRouteResult().getStartPos(), routeData.getDriveRouteResult().getTargetPos());
                }

            }

        }

        if (routeResultList != null & routeResultList.size() > 0) {

            DriveRouteResult result = routeResultList.get(0).getDriveRouteResult();
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {

                DrivePath drivePath = result.getPaths()
                        .get(0);
                callBack.onDrawRouteBack(drivePath);
            }
        }
    }


    public void moveToCamera(LatLonPoint start, LatLonPoint end) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();


        builder.include(new LatLng(start.getLatitude(), start.getLongitude()));
        builder.include(new LatLng(end.getLatitude(), end.getLongitude()));
        LatLngBounds latLngBounds = builder.build();
        //newLatLngBoundsRect(LatLngBounds latlngbounds,
        //int paddingLeft,设置经纬度范围和mapView左边缘的空隙。
        //int paddingRight,设置经纬度范围和mapView右边缘的空隙
        //int paddingTop,设置经纬度范围和mapView上边缘的空隙。
        //int paddingBottom)设置经纬度范围和mapView下边缘的空隙。
        int paddingTop = ConvertUtils.dp2px(220);
        int paddingBottom = ConvertUtils.dp2px(160);
        getaMap().animateCamera(CameraUpdateFactory.newLatLngBoundsRect(latLngBounds, 80, 80, paddingTop, paddingBottom));

    }

    public interface OnNaviFragmentCallBack {
        void onCloseNaviButton();

        void onDrawRouteBack(DrivePath drivePath);

    }


    private void showMapLoadingView() {

        rl_map_loading.setVisibility(View.VISIBLE);
        map_progressBar.setVisibility(View.VISIBLE);
        bt_map_error.setVisibility(View.GONE);


    }

    private void dimissMapLoadingView() {
        rl_map_loading.setVisibility(View.VISIBLE);
        map_progressBar.setVisibility(View.GONE);
        bt_map_error.setVisibility(View.GONE);

    }

    private void showMapLoadingViewSuccess() {
        rl_map_loading.setVisibility(View.GONE);
        map_progressBar.setVisibility(View.GONE);
        bt_map_error.setVisibility(View.GONE);

    }

    private void showMapLoadingViewError() {
        rl_map_loading.setVisibility(View.VISIBLE);
        map_progressBar.setVisibility(View.GONE);
        bt_map_error.setVisibility(View.VISIBLE);

    }

    @Override
    public void showLoading(int type) {
        showMapLoadingView();
    }


    @Override
    public void dismissLoading(int type) {
        dimissMapLoadingView();
    }

    @Override
    public void ondrawRouteFailed(Throwable e) {

        showMapLoadingViewError();
        Log.e("测试代码", "测试代码ondrawRouteFailed-----" + e.toString());

    }

    @OnClick(R2.id.bt_map_error)
    public void onClickErrorMap() {
        showRoute(currentSettingData);
    }


    @Override
    public void onError(int type) {
        Log.e("测试代码", "测试代码onError-----");
    }

    public void setCallBack(OnNaviFragmentCallBack callBack) {
        this.callBack = callBack;
    }


    boolean isDisplayOverview = false;


    @OnClick(R2.id.overviewButtonView)
    public void overViewButtonView() {

        isDisplayOverview = !isDisplayOverview;
        showDisPlayViewOrRecoverLockMode(isDisplayOverview);
    }

    /**
     * 显示全览或者锁车模式
     *
     * @param isdiplay true全览 false锁车
     */
    private void showDisPlayViewOrRecoverLockMode(boolean isdiplay) {
        isDisplayOverview = isdiplay;
        if (null != getNaviView() && null != mAMapNavi) {

            if (isdiplay) {
                getNaviView().displayOverview();
                overviewButtonView.setChecked(isdiplay);
            } else {
                getNaviView().recoverLockMode();
                overviewButtonView.setChecked(isdiplay);
                mAMapNavi.resumeNavi();
            }

        }
    }

    @OnClick(R2.id.btn_Location)
    public void locationTo() {
        //锁车模式
        showDisPlayViewOrRecoverLockMode(false);
    }
}
