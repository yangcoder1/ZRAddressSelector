package com.zr.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


import com.zr.addressselector.BottomSelectorDialog;
import com.zr.addressselector.listener.OnAddressSelectedListener;
import com.zr.addressselector.model.Area;
import com.zr.addressselector.model.Area;
import com.zr.addressselector.model.Area;
import com.zr.addressselector.model.Area;
import com.zr.addressselector.util.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnAddressSelectedListener {

    BottomSelectorDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonBottomDialog = (Button) findViewById(R.id.buttonBottomDialog);
        buttonBottomDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new BottomSelectorDialog(MainActivity.this);
                dialog.setOnAddressSelectedListener(MainActivity.this);
                dialog.show();
                // TODO: 17/2/7 实时请求省份数据
                Area province = new Area();
                province.id = 1;
                province.name = "浙江省";
                dialog.getSelector().setProvinces(Collections.singletonList(province));
//                dialog.getSelector().setAreas(null);
            }
        });

        Button hasMsgButton = (Button) findViewById(R.id.buttonBottomDialogWithMessage);
        hasMsgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Area province = new Area();
                province.id = 1;
                province.name = "省份" + province.id;
                List<Area> provinces = Collections.singletonList(province);

                Area city1 = new Area();
                city1.parentId = province.id;
                city1.id = province.id * 100 + 1;
                city1.name = "城市" + city1.id;

                Area city2 = new Area();
                city2.parentId = province.id;
                city2.id = province.id * 100 + 2;
                city2.name = "城市" + city2.id;

                List<Area> cities = new ArrayList<>();
                cities.add(city1);
                cities.add(city2);

                long cityid = 101;
                Area county11 = new Area();
                county11.parentId = cityid;
                county11.id = cityid * 100 + 1;
                county11.name = "区县" + county11.id;

                Area county12 = new Area();
                county12.parentId = cityid;
                county12.id = cityid * 100 + 2;
                county12.name = "区县" + county12.id;

                List<Area> counties = new ArrayList<>();
                counties.add(county11);
                counties.add(county12);

                dialog = new BottomSelectorDialog(MainActivity.this);
                dialog.setOnAddressSelectedListener(MainActivity.this);
                dialog.show();

                dialog.getSelector().setAddressSelector(provinces, 0, cities, 0, counties, 0);
            }
        });
    }

    @Override
    public void onAddressSelected(Area province, Area city, Area county, Area street) {
        String s =
                (province == null ? "" : province.name) +
                        (city == null ? "" : "\n" + city.name) +
                        (county == null ? "" : "\n" + county.name) +
                        (street == null ? "" : "\n" + street.name);

        ToastUtils.showShort(MainActivity.this, s);
    }

    @Override
    public void onProvinceSelected(Area province) {
        System.out.println("onAreaSelected");
//        ToastUtil.showToast("点击新省份,获取市数据");

        // TODO: 2017/2/5 请求城市数据
        Area city1 = new Area();
        city1.parentId = province.id;
        city1.id = province.id * 100 + 1;
        city1.name = "杭州市";

        Area city2 = new Area();
        city2.parentId = province.id;
        city2.id = province.id * 100 + 2;
        city2.name = "衢州市";

        List<Area> list = new ArrayList<>();
        list.add(city1);
        list.add(city2);
        dialog.getSelector().setCities(list);
    }

    @Override
    public void onCitySelected(Area city) {
        System.out.println("onAreaSelected " + city.id);
//        ToastUtil.showToast("点击新城市,获取区县数据");

        // TODO: 2017/2/5 请求县乡数据
        if (city.id == 101) {
            Area county11 = new Area();
            county11.parentId = city.id;
            county11.id = city.id * 100 + 1;
            county11.name = "西湖区";

            Area county12 = new Area();
            county12.parentId = city.id;
            county12.id = city.id * 100 + 2;
            county12.name = "滨江区";

            List<Area> list = new ArrayList<>();
            list.add(county11);
            list.add(county12);
            dialog.getSelector().setCountries(list);
        } else if (city.id == 102) {
            Area county21 = new Area();
            county21.parentId = city.id;
            county21.id = city.id * 100 + 1;
            county21.name = "衢江区";

            Area county22 = new Area();
            county22.parentId = city.id;
            county22.id = city.id * 100 + 2;
            county22.name = "江山县";

            List<Area> list2 = new ArrayList<>();
            list2.add(county21);
            list2.add(county22);
            dialog.getSelector().setCountries(list2);
        }

    }

    @Override
    public void onCountySelected(Area county) {
        System.out.println("onAreaSelected");
//        ToastUtil.showToast("点击新区县数据,获取街道数据");
        // TODO: 17/2/7 实时获取街道信息
        Area street = new Area();
        street.id = county.id * 100 + 1;
        street.parentId = county.id;
        street.name = "街道_" + street.id;

        dialog.getSelector().setStreets(Collections.singletonList(street));
    }

}

