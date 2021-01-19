package com.zr.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.zr.addressselector.AddressSelector;
import com.zr.addressselector.BottomSelectorDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddressSelector.OnAddressSelectedListener {

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
                AddressSelector.Area province = new AddressSelector.Area();
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
                AddressSelector.Area province = new AddressSelector.Area();
                province.id = 1;
                province.name = "省份" + province.id;
                List<AddressSelector.Area> provinces = Collections.singletonList(province);

                AddressSelector.Area city1 = new AddressSelector.Area();
                city1.parentId = province.id;
                city1.id = province.id * 100 + 1;
                city1.name = "城市" + city1.id;

                AddressSelector.Area city2 = new AddressSelector.Area();
                city2.parentId = province.id;
                city2.id = province.id * 100 + 2;
                city2.name = "城市" + city2.id;

                List<AddressSelector.Area> cities = new ArrayList<>();
                cities.add(city1);
                cities.add(city2);

                long cityid = 101;
                AddressSelector.Area county11 = new AddressSelector.Area();
                county11.parentId = cityid;
                county11.id = cityid * 100 + 1;
                county11.name = "区县" + county11.id;

                AddressSelector.Area county12 = new AddressSelector.Area();
                county12.parentId = cityid;
                county12.id = cityid * 100 + 2;
                county12.name = "区县" + county12.id;

                List<AddressSelector.Area> counties = new ArrayList<>();
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
    public void onAddressSelected(AddressSelector.Area province, AddressSelector.Area city, AddressSelector.Area county, AddressSelector.Area street) {
        String s =
                (province == null ? "" : province.name) +
                        (city == null ? "" : "\n" + city.name) +
                        (county == null ? "" : "\n" + county.name) +
                        (street == null ? "" : "\n" + street.name);

        ToastUtils.showShort(MainActivity.this, s);
        dialog.dismiss();
    }

    @Override
    public void onProvinceSelected(AddressSelector.Area province) {
        System.out.println("onAreaSelected");
//        ToastUtil.showToast("点击新省份,获取市数据");

        // TODO: 2017/2/5 请求城市数据
        AddressSelector.Area city1 = new AddressSelector.Area();
        city1.parentId = province.id;
        city1.id = province.id * 100 + 1;
        city1.name = "杭州市";

        AddressSelector.Area city2 = new AddressSelector.Area();
        city2.parentId = province.id;
        city2.id = province.id * 100 + 2;
        city2.name = "衢州市";

        List<AddressSelector.Area> list = new ArrayList<>();
        list.add(city1);
        list.add(city2);
        dialog.getSelector().setCities(list);
    }

    @Override
    public void onCitySelected(AddressSelector.Area city) {
        System.out.println("onAreaSelected " + city.id);
//        ToastUtil.showToast("点击新城市,获取区县数据");

        // TODO: 2017/2/5 请求县乡数据
        if (city.id == 101) {
            AddressSelector.Area county11 = new AddressSelector.Area();
            county11.parentId = city.id;
            county11.id = city.id * 100 + 1;
            county11.name = "西湖区";

            AddressSelector.Area county12 = new AddressSelector.Area();
            county12.parentId = city.id;
            county12.id = city.id * 100 + 2;
            county12.name = "滨江区";

            List<AddressSelector.Area> list = new ArrayList<>();
            list.add(county11);
            list.add(county12);
            dialog.getSelector().setCountries(list);
        } else if (city.id == 102) {
            AddressSelector.Area county21 = new AddressSelector.Area();
            county21.parentId = city.id;
            county21.id = city.id * 100 + 1;
            county21.name = "衢江区";

            AddressSelector.Area county22 = new AddressSelector.Area();
            county22.parentId = city.id;
            county22.id = city.id * 100 + 2;
            county22.name = "江山县";

            List<AddressSelector.Area> list2 = new ArrayList<>();
            list2.add(county21);
            list2.add(county22);
            dialog.getSelector().setCountries(list2);
        }

    }

    @Override
    public void onCountySelected(AddressSelector.Area county) {
        System.out.println("onAreaSelected");
//        ToastUtil.showToast("点击新区县数据,获取街道数据");
        // TODO: 17/2/7 实时获取街道信息
        AddressSelector.Area street = new AddressSelector.Area();
        street.id = county.id * 100 + 1;
        street.parentId = county.id;
        street.name = "街道_" + street.id;

        dialog.getSelector().setStreets(Collections.singletonList(street));
    }

}

