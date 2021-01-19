package com.zr.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.zr.addressselector.AddressSelector
import com.zr.addressselector.AddressSelector.OnAddressSelectedListener
import com.zr.addressselector.BottomSelectorDialog
import java.util.*

class MainActivity : AppCompatActivity(), OnAddressSelectedListener {
    var dialog: BottomSelectorDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val buttonBottomDialog = findViewById(R.id.buttonBottomDialog) as Button
        buttonBottomDialog.setOnClickListener {
            dialog = BottomSelectorDialog(this@MainActivity)
            dialog!!.setOnAddressSelectedListener(this@MainActivity)
            dialog!!.show()
            // TODO: 17/2/7 实时请求省份数据
            val province = AddressSelector.Area()
            province.id = 1
            province.name = "浙江省"
            dialog!!.selector!!.setProvinces(listOf(province))
            //                dialog.getSelector().setAreas(null);
        }
        val hasMsgButton = findViewById(R.id.buttonBottomDialogWithMessage) as Button
        hasMsgButton.setOnClickListener {
            val province = AddressSelector.Area()
            province.id = 1
            province.name = "省份" + province.id
            val provinces = listOf(province)
            val city1 = AddressSelector.Area()
            city1.parentId = province.id
            city1.id = province.id * 100 + 1
            city1.name = "城市" + city1.id
            val city2 = AddressSelector.Area()
            city2.parentId = province.id
            city2.id = province.id * 100 + 2
            city2.name = "城市" + city2.id
            val cities: MutableList<AddressSelector.Area> = ArrayList()
            cities.add(city1)
            cities.add(city2)
            val cityid: Long = 101
            val county11 = AddressSelector.Area()
            county11.parentId = cityid
            county11.id = cityid * 100 + 1
            county11.name = "区县" + county11.id
            val county12 = AddressSelector.Area()
            county12.parentId = cityid
            county12.id = cityid * 100 + 2
            county12.name = "区县" + county12.id
            val counties: MutableList<AddressSelector.Area> = ArrayList()
            counties.add(county11)
            counties.add(county12)
            dialog = BottomSelectorDialog(this@MainActivity)
            dialog!!.setOnAddressSelectedListener(this@MainActivity)
            dialog!!.show()
            dialog!!.selector!!.setAddressSelector(provinces, 0, cities, 0, counties, 0)
        }
    }

    override fun onAddressSelected(province: AddressSelector.Area?, city: AddressSelector.Area?, county: AddressSelector.Area?, street: AddressSelector.Area?) {
        val s = (if (province == null) "" else province.name) +
                (if (city == null) "" else """
     
     ${city.name}
     """.trimIndent()) +
                (if (county == null) "" else """
     
     ${county.name}
     """.trimIndent()) +
                if (street == null) "" else """
     
     ${street.name}
     """.trimIndent()
        ToastUtils.showShort(this@MainActivity, s)
        dialog!!.dismiss()
    }

    override fun onProvinceSelected(province: AddressSelector.Area?) {
        println("onAreaSelected")
        //        ToastUtil.showToast("点击新省份,获取市数据");

        // TODO: 2017/2/5 请求城市数据
        val city1 = AddressSelector.Area()
        city1.parentId = province!!.id
        city1.id = province.id * 100 + 1
        city1.name = "杭州市"
        val city2 = AddressSelector.Area()
        city2.parentId = province.id
        city2.id = province.id * 100 + 2
        city2.name = "衢州市"
        val list: MutableList<AddressSelector.Area> = ArrayList()
        list.add(city1)
        list.add(city2)
        dialog!!.selector!!.setCities(list)
    }

    override fun onCitySelected(city: AddressSelector.Area?) {
        println("onAreaSelected " + city!!.id)
        //        ToastUtil.showToast("点击新城市,获取区县数据");

        // TODO: 2017/2/5 请求县乡数据
        if (city.id == 101L) {
            val county11 = AddressSelector.Area()
            county11.parentId = city.id
            county11.id = city.id * 100 + 1
            county11.name = "西湖区"
            val county12 = AddressSelector.Area()
            county12.parentId = city.id
            county12.id = city.id * 100 + 2
            county12.name = "滨江区"
            val list: MutableList<AddressSelector.Area> = ArrayList()
            list.add(county11)
            list.add(county12)
            dialog!!.selector!!.setCountries(list)
        } else if (city.id == 102L) {
            val county21 = AddressSelector.Area()
            county21.parentId = city.id
            county21.id = city.id * 100 + 1
            county21.name = "衢江区"
            val county22 = AddressSelector.Area()
            county22.parentId = city.id
            county22.id = city.id * 100 + 2
            county22.name = "江山县"
            val list2: MutableList<AddressSelector.Area> = ArrayList()
            list2.add(county21)
            list2.add(county22)
            dialog!!.selector!!.setCountries(list2)
        }
    }

    override fun onCountySelected(county: AddressSelector.Area?) {
        println("onAreaSelected")
        //        ToastUtil.showToast("点击新区县数据,获取街道数据");
        // TODO: 17/2/7 实时获取街道信息
        val street = AddressSelector.Area()
        street.id = county!!.id * 100 + 1
        street.parentId = county.id
        street.name = "街道_" + street.id
        dialog!!.selector!!.setStreets(listOf(street))
    }
}