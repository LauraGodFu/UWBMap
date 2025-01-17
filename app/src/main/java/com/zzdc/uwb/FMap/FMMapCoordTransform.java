package com.zzdc.uwb.FMap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


import com.zzdc.uwb.Utils.FileUtils;
import com.zzdc.uwb.Utils.ViewHelper;
import com.fengmap.android.FMErrorMsg;
import com.fengmap.android.data.OnFMDownloadProgressListener;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapCoordZType;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.geometry.FMScreenCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.marker.FMImageMarker;
import com.zzdc.uwb.R;


/**
 * 地图坐标转换
 * <p>地图提供了屏幕坐标转换为地图坐标{@link FMMap#toFMScreenCoord(int, FMMapCoordZType, FMMapCoord)},
 * 地图坐标转换为屏幕坐标{@link FMMap#toFMMapCoord(int, FMScreenCoord)}
 *
 * @author hezutao@fengmap.com
 * @version 2.0.0
 */
public class FMMapCoordTransform extends Activity implements OnFMMapInitListener, OnFMMapClickListener {

    private FMMap mMap;
    private FMImageLayer mImageLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fmap_transform);

        openMapByPath();
    }

    /**
     * 加载地图数据
     */
    private void openMapByPath() {
        FMMapView mapView = (FMMapView) findViewById(R.id.map_view);
        mMap = mapView.getFMMap();
        mMap.setOnFMMapInitListener(this);
        mMap.setOnFMMapClickListener(this);
        //加载离线数据
        String path = FileUtils.getDefaultMapPath(this);
        mMap.openMapByPath(path);
    }

    /**
     * 地图加载成功回调事件
     *
     * @param path 地图所在sdcard路径
     */
    @Override
    public void onMapInitSuccess(String path) {
        //加载离线主题
        mMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));

        //添加图片图层
        mImageLayer = mMap.getFMLayerProxy().createFMImageLayer(mMap.getFocusGroupId());
        mMap.addLayer(mImageLayer);
    }

    /**
     * 地图加载失败回调事件
     *
     * @param path      地图所在sdcard路径
     * @param errorCode 失败加载错误码，可以通过{@link FMErrorMsg#getErrorMsg(int)}获取加载地图失败详情
     */
    @Override
    public void onMapInitFailure(String path, int errorCode) {
        //TODO 可以提示用户地图加载失败原因，进行地图加载失败处理
    }

    /**
     * 当{@link FMMap#openMapById(String, boolean)}设置openMapById(String, false)时地图不自动更新会
     * 回调此事件，可以调用{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}进行
     * 地图下载更新
     *
     * @param upgradeInfo 地图版本更新详情,地图版本号{@link FMMapUpgradeInfo#getVersion()},<br/>
     *                    地图id{@link FMMapUpgradeInfo#getMapId()}
     * @return 如果调用了{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}地图下载更新，
     * 返回值return true,因为{@link FMMap#upgrade(FMMapUpgradeInfo, OnFMDownloadProgressListener)}
     * 会自动下载更新地图，更新完成后会加载地图;否则return false。
     */
    @Override
    public boolean onUpgrade(FMMapUpgradeInfo upgradeInfo) {
        //TODO 获取到最新地图更新的信息，可以进行地图的下载操作
        return false;
    }

    /**
     * 地图销毁调用
     */
    @Override
    public void onBackPressed() {
        if (mMap != null) {
            mMap.onDestroy();
        }
        super.onBackPressed();
    }

    @Override
    public void onMapClick(float x, float y) {
        mImageLayer.removeAll();
        //添加图片标注
        FMPickMapCoordResult mapCoordResult = mMap.pickMapCoord(x, y);
        if (mapCoordResult != null) {
            FMMapCoord mapCoord = mapCoordResult.getMapCoord();
            FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), mapCoord);
            mImageLayer.addMarker(imageMarker);
        }

        int groupId = mMap.getFocusGroupId();
        //屏幕坐标转换为地图坐标
        FMScreenCoord screenCoord = new FMScreenCoord(x, y);
        FMMapCoord convertMapCoord = mMap.toFMMapCoord(groupId, screenCoord);

        //地图坐标转换为屏幕坐标
        FMScreenCoord convertScreenCoord = mMap.toFMScreenCoord(groupId,
            FMMapCoordZType.MAPCOORDZ_MODEL, convertMapCoord);

        //显示转换结果
        TextView mapResult = ViewHelper.getView(FMMapCoordTransform.this, R.id.map_result);
        mapResult.setText(getString(R.string.map_transform_tips, groupId, x, y, convertMapCoord.x, convertMapCoord.y,
            convertScreenCoord.x, convertScreenCoord.y));
    }
}
