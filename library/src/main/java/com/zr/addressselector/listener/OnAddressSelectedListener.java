package com.zr.addressselector.listener;


import com.zr.addressselector.model.Area;

public interface OnAddressSelectedListener {
    // 获取地址完成回调
    void onAddressSelected(Area province, Area city, Area county, Area street);
    // 选取省份完成回调
    void onProvinceSelected(Area province);
    // 选取城市完成回调
    void onCitySelected(Area city);
    // 选取区/县完成回调
    void onCountySelected(Area county);
}
