package com.zr.addressselector

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.util.ArrayMap
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import java.util.*

class AddressSelector(private val context: Context) : OnItemClickListener {
    private val handler = Handler { msg ->
        when (msg.what) {
            WHAT_PROVINCES_PROVIDED -> {
                provinces = msg.obj as List<Area>
                provinceAdapter!!.notifyDataSetChanged()
                listView!!.adapter = provinceAdapter
            }
            WHAT_CITIES_PROVIDED -> {
                cities = msg.obj as List<Area>
                cityAdapter!!.notifyDataSetChanged()
                if (notEmpty(cities)) {
                    // 以次级内容更新列表
                    listView!!.adapter = cityAdapter
                    // 更新索引为次级
                    tabIndex = INDEX_TAB_CITY

                    // 缓存省-市数据
                    val provinceId = cities!![0].parentId
                    if (!province2city.containsKey(provinceId)) {
                        val cityList: MutableList<Area> = ArrayList()
                        copy(cities, cityList)
                        province2city[provinceId] = cityList

//                            System.out.println("***** save !!!!!");
//                            System.out.println("cities = " + cities.toString());
                    }
                } else {
                    // 次级无内容，回调
                    callbackInternal()
                }
            }
            WHAT_COUNTIES_PROVIDED -> {
                counties = msg.obj as List<Area>
                countyAdapter!!.notifyDataSetChanged()
                if (notEmpty(counties)) {
                    listView!!.adapter = countyAdapter
                    tabIndex = INDEX_TAB_COUNTY

                    // 缓存市-区数据
                    val cityId = counties!![0].parentId
                    if (!city2county.containsKey(cityId)) {
                        val countyList: MutableList<Area> = ArrayList()
                        copy(counties, countyList)
                        city2county[cityId] = countyList
                    }
                } else {
                    callbackInternal()
                }
            }
            WHAT_STREETS_PROVIDED -> {
                streets = msg.obj as List<Area>
                streetAdapter!!.notifyDataSetChanged()
                if (notEmpty(streets)) {
                    listView!!.adapter = streetAdapter
                    tabIndex = INDEX_TAB_STREET
                    // 缓存市-区数据
                    val countryId = streets!![0].parentId
                    if (!county2street.containsKey(countryId)) {
                        val streetList: MutableList<Area> = ArrayList()
                        copy(streets, streetList)
                        county2street[countryId] = streetList
                    }
                } else {
                    callbackInternal()
                }
            }
        }
        updateTabsVisibility()
        updateProgressVisibility()
        updateIndicator()
        true
    }

    /**
     * 设置回调接口
     *
     * @param listener
     */
    var onAddressSelectedListener: OnAddressSelectedListener? = null
    lateinit var view: View
    private var indicator: View? = null
    private var textViewProvince: TextView? = null
    private var textViewCity: TextView? = null
    private var textViewCounty: TextView? = null
    private var textViewStreet: TextView? = null
    private var progressBar: ProgressBar? = null
    private var listView: ListView? = null
    private var provinceAdapter: ProvinceAdapter? = null
    private var cityAdapter: CityAdapter? = null
    private var countyAdapter: CountyAdapter? = null
    private var streetAdapter: StreetAdapter? = null
    private var provinces: List<Area>? = null
    private var cities: List<Area>? = null
    private var counties: List<Area>? = null
    private var streets: List<Area>? = null

    /**
     * 缓存数据:省-市
     */
    private val province2city = ArrayMap<Long, List<Area>>()

    /**
     * 缓存数据:市-区
     */
    private val city2county = ArrayMap<Long, List<Area>>()

    /**
     * 缓存数据:区-街道
     */
    private val county2street = ArrayMap<Long, List<Area>>()
    private var provinceIndex = INDEX_INVALID
    private var cityIndex = INDEX_INVALID
    private var countyIndex = INDEX_INVALID
    private var streetIndex = INDEX_INVALID
    private var tabIndex = INDEX_TAB_PROVINCE
    private fun initAdapters() {
        provinceAdapter = ProvinceAdapter()
        cityAdapter = CityAdapter()
        countyAdapter = CountyAdapter()
        streetAdapter = StreetAdapter()
    }

    fun clearCacheData() {
        province2city.clear()
        city2county.clear()
        county2street.clear()

        // 清空子级数据
        provinces = null
        cities = null
        counties = null
        streets = null
        provinceAdapter!!.notifyDataSetChanged()
        cityAdapter!!.notifyDataSetChanged()
        countyAdapter!!.notifyDataSetChanged()
        streetAdapter!!.notifyDataSetChanged()
        provinceIndex = INDEX_INVALID
        cityIndex = INDEX_INVALID
        countyIndex = INDEX_INVALID
        streetIndex = INDEX_INVALID
        tabIndex = INDEX_TAB_PROVINCE
        textViewProvince!!.text = "请选择"
        updateTabsVisibility()
        updateProgressVisibility()
        updateIndicator()
    }

    private fun initViews() {
        view = LayoutInflater.from(context).inflate(R.layout.address_selector, null)
        progressBar = view.findViewById(R.id.progressBar) as ProgressBar
        listView = view.findViewById(R.id.listView) as ListView
        indicator = view.findViewById(R.id.indicator)
        textViewProvince = view.findViewById(R.id.textViewProvince) as TextView
        textViewCity = view.findViewById(R.id.textViewCity) as TextView
        textViewCounty = view.findViewById(R.id.textViewCounty) as TextView
        textViewStreet = view.findViewById(R.id.textViewStreet) as TextView
        textViewProvince!!.setOnClickListener(OnProvinceTabClickListener())
        textViewCity!!.setOnClickListener(OnCityTabClickListener())
        textViewCounty!!.setOnClickListener(OnCountyTabClickListener())
        textViewStreet!!.setOnClickListener(OnStreetTabClickListener())
        listView!!.onItemClickListener = this
        updateIndicator()
        progressBar!!.visibility = View.VISIBLE
    }

    private fun updateIndicator() {
        view.post {
            when (tabIndex) {
                INDEX_TAB_PROVINCE -> buildIndicatorAnimatorTowards(textViewProvince).start()
                INDEX_TAB_CITY -> buildIndicatorAnimatorTowards(textViewCity).start()
                INDEX_TAB_COUNTY -> buildIndicatorAnimatorTowards(textViewCounty).start()
                INDEX_TAB_STREET -> buildIndicatorAnimatorTowards(textViewStreet).start()
            }
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun buildIndicatorAnimatorTowards(tab: TextView?): AnimatorSet {
        val xAnimator = ObjectAnimator.ofFloat(indicator, "X", indicator!!.x, tab!!.x)
        val params = indicator!!.layoutParams
        val widthAnimator = ValueAnimator.ofInt(params.width, tab.measuredWidth)
        widthAnimator.addUpdateListener { animation ->
            params.width = animation.animatedValue as Int
            indicator!!.layoutParams = params
        }
        val set = AnimatorSet()
        set.interpolator = FastOutSlowInInterpolator()
        set.playTogether(xAnimator, widthAnimator)
        return set
    }

    interface OnAddressSelectedListener {
        // 获取地址完成回调
        fun onAddressSelected(province: Area?, city: Area?, county: Area?, street: Area?)

        // 选取省份完成回调
        fun onProvinceSelected(province: Area?)

        // 选取城市完成回调
        fun onCitySelected(city: Area?)

        // 选取区/县完成回调
        fun onCountySelected(county: Area?)
    }

    private inner class OnProvinceTabClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            tabIndex = INDEX_TAB_PROVINCE
            listView!!.adapter = provinceAdapter
            if (provinceIndex != INDEX_INVALID) {
                listView!!.setSelection(provinceIndex)
            }
            updateTabsVisibility()
            updateIndicator()
        }
    }

    private inner class OnCityTabClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            tabIndex = INDEX_TAB_CITY
            listView!!.adapter = cityAdapter
            if (cityIndex != INDEX_INVALID) {
                listView!!.setSelection(cityIndex)
            }
            updateTabsVisibility()
            updateIndicator()
        }
    }

    private inner class OnCountyTabClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            tabIndex = INDEX_TAB_COUNTY
            listView!!.adapter = countyAdapter
            if (countyIndex != INDEX_INVALID) {
                listView!!.setSelection(countyIndex)
            }
            updateTabsVisibility()
            updateIndicator()
        }
    }

    private inner class OnStreetTabClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            tabIndex = INDEX_TAB_STREET
            listView!!.adapter = streetAdapter
            if (streetIndex != INDEX_INVALID) {
                listView!!.setSelection(streetIndex)
            }
            updateTabsVisibility()
            updateIndicator()
        }
    }

    private fun updateTabsVisibility() {
        textViewProvince!!.visibility = if (notEmpty(provinces)) View.VISIBLE else View.GONE
        textViewCity!!.visibility = if (notEmpty(cities)) View.VISIBLE else View.GONE
        textViewCounty!!.visibility = if (notEmpty(counties)) View.VISIBLE else View.GONE
        textViewStreet!!.visibility = if (notEmpty(streets)) View.VISIBLE else View.GONE
        textViewProvince!!.isEnabled = tabIndex != INDEX_TAB_PROVINCE
        textViewCity!!.isEnabled = tabIndex != INDEX_TAB_CITY
        textViewCounty!!.isEnabled = tabIndex != INDEX_TAB_COUNTY
        textViewStreet!!.isEnabled = tabIndex != INDEX_TAB_STREET
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        when (tabIndex) {
            INDEX_TAB_PROVINCE -> {
                val province = provinceAdapter!!.getItem(position)

                // 更新当前级别及子级标签文本
                textViewProvince!!.text = province.name
                textViewCity!!.text = "请选择"
                textViewCounty!!.text = "请选择"
                textViewStreet!!.text = "请选择"

                // 清空子级数据
                cities = null
                counties = null
                streets = null
                cityAdapter!!.notifyDataSetChanged()
                countyAdapter!!.notifyDataSetChanged()
                streetAdapter!!.notifyDataSetChanged()

                // 更新已选中项
                provinceIndex = position
                cityIndex = INDEX_INVALID
                countyIndex = INDEX_INVALID
                streetIndex = INDEX_INVALID

                // 更新选中效果
                provinceAdapter!!.notifyDataSetChanged()

                // 有缓存则直接使用缓存,否则去重新请求
                if (province2city.containsKey(province.id)) {
                    setCities(province2city[province.id])
                } else {
                    progressBar!!.visibility = View.VISIBLE
                    onAddressSelectedListener!!.onProvinceSelected(province)
                }
            }
            INDEX_TAB_CITY -> {
                val city = cityAdapter!!.getItem(position)
                textViewCity!!.text = city.name
                textViewCounty!!.text = "请选择"
                textViewStreet!!.text = "请选择"
                counties = null
                streets = null
                countyAdapter!!.notifyDataSetChanged()
                streetAdapter!!.notifyDataSetChanged()
                cityIndex = position
                countyIndex = INDEX_INVALID
                streetIndex = INDEX_INVALID
                cityAdapter!!.notifyDataSetChanged()
                println(city2county.toString())

                // 有缓存则直接使用缓存,否则去重新请求
                if (city2county.containsKey(city.id)) {
                    println("cityId = " + city.id)
                    setCountries(city2county[city.id])
                } else {
                    progressBar!!.visibility = View.VISIBLE
                    onAddressSelectedListener!!.onCitySelected(city)
                }
            }
            INDEX_TAB_COUNTY -> {
                val county = countyAdapter!!.getItem(position)
                textViewCounty!!.text = county.name
                textViewStreet!!.text = "请选择"
                streets = null
                streetAdapter!!.notifyDataSetChanged()
                countyIndex = position
                streetIndex = INDEX_INVALID
                countyAdapter!!.notifyDataSetChanged()

                // 有缓存则直接使用缓存,否则去重新请求
                if (county2street.containsKey(county.id)) {
                    setStreets(county2street[county.id])
                } else {
                    progressBar!!.visibility = View.VISIBLE
                    onAddressSelectedListener!!.onCountySelected(county)
                }
            }
            INDEX_TAB_STREET -> {
                val street = streetAdapter!!.getItem(position)
                textViewStreet!!.text = street.name
                streetIndex = position
                streetAdapter!!.notifyDataSetChanged()
                callbackInternal()
            }
        }
        updateTabsVisibility()
        updateIndicator()
    }

    /**
     * 地址选择完成时调用的方法
     */
    private fun callbackInternal() {
        if (onAddressSelectedListener != null) {
            val province = if (provinces == null || provinceIndex == INDEX_INVALID) null else provinces!![provinceIndex]
            val city = if (cities == null || cityIndex == INDEX_INVALID) null else cities!![cityIndex]
            val county = if (counties == null || countyIndex == INDEX_INVALID) null else counties!![countyIndex]
            val street = if (streets == null || streetIndex == INDEX_INVALID) null else streets!![streetIndex]
            onAddressSelectedListener!!.onAddressSelected(province, city, county, street)
        }
    }

    private fun updateProgressVisibility() {
        val adapter = listView!!.adapter
        val itemCount = adapter.count
        progressBar!!.visibility = if (itemCount > 0) View.GONE else View.VISIBLE
    }

    private inner class ProvinceAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return if (provinces == null) 0 else provinces!!.size
        }

        override fun getItem(position: Int): Area {
            return provinces!![position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            val holder: Holder
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false)
                holder = Holder()
                holder.textView = convertView.findViewById(R.id.textView) as TextView
                holder.imageViewCheckMark = convertView.findViewById(R.id.imageViewCheckMark) as ImageView
                convertView.tag = holder
            } else {
                holder = convertView.tag as Holder
            }
            val item = getItem(position)
            holder.textView!!.text = item.name
            val checked = provinceIndex != INDEX_INVALID && provinces!![provinceIndex].id == item.id
            holder.textView!!.isEnabled = !checked
            holder.imageViewCheckMark!!.visibility = if (checked) View.VISIBLE else View.GONE
            return convertView
        }

        inner class Holder {
            var textView: TextView? = null
            var imageViewCheckMark: ImageView? = null
        }
    }

    private inner class CityAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return if (cities == null) 0 else cities!!.size
        }

        override fun getItem(position: Int): Area {
            return cities!![position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            val holder: Holder
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false)
                holder = Holder()
                holder.textView = convertView.findViewById(R.id.textView) as TextView
                holder.imageViewCheckMark = convertView.findViewById(R.id.imageViewCheckMark) as ImageView
                convertView.tag = holder
            } else {
                holder = convertView.tag as Holder
            }
            val item = getItem(position)
            holder.textView!!.text = item.name
            val checked = cityIndex != INDEX_INVALID && cities!![cityIndex].id == item.id
            holder.textView!!.isEnabled = !checked
            holder.imageViewCheckMark!!.visibility = if (checked) View.VISIBLE else View.GONE
            return convertView
        }

        inner class Holder {
            var textView: TextView? = null
            var imageViewCheckMark: ImageView? = null
        }
    }

    private inner class CountyAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return if (counties == null) 0 else counties!!.size
        }

        override fun getItem(position: Int): Area {
            return counties!![position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            val holder: Holder
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false)
                holder = Holder()
                holder.textView = convertView.findViewById(R.id.textView) as TextView
                holder.imageViewCheckMark = convertView.findViewById(R.id.imageViewCheckMark) as ImageView
                convertView.tag = holder
            } else {
                holder = convertView.tag as Holder
            }
            val item = getItem(position)
            holder.textView!!.text = item.name
            val checked = countyIndex != INDEX_INVALID && counties!![countyIndex].id == item.id
            holder.textView!!.isEnabled = !checked
            holder.imageViewCheckMark!!.visibility = if (checked) View.VISIBLE else View.GONE
            return convertView
        }

        inner class Holder {
            var textView: TextView? = null
            var imageViewCheckMark: ImageView? = null
        }
    }

    private inner class StreetAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return if (streets == null) 0 else streets!!.size
        }

        override fun getItem(position: Int): Area {
            return streets!![position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            val holder: Holder
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_area, parent, false)
                holder = Holder()
                holder.textView = convertView.findViewById(R.id.textView) as TextView
                holder.imageViewCheckMark = convertView.findViewById(R.id.imageViewCheckMark) as ImageView
                convertView.tag = holder
            } else {
                holder = convertView.tag as Holder
            }
            val item = getItem(position)
            holder.textView!!.text = item.name
            val checked = streetIndex != INDEX_INVALID && streets!![streetIndex].id == item.id
            holder.textView!!.isEnabled = !checked
            holder.imageViewCheckMark!!.visibility = if (checked) View.VISIBLE else View.GONE
            return convertView
        }

        inner class Holder {
            var textView: TextView? = null
            var imageViewCheckMark: ImageView? = null
        }
    }

    /**
     * 设置省列表
     *
     * @param provinces 省份列表
     */
    fun setProvinces(provinces: List<Area?>?) {
        handler.sendMessage(Message.obtain(handler, WHAT_PROVINCES_PROVIDED, provinces))
    }

    /**
     * 设置市列表
     *
     * @param cities 城市列表
     */
    fun setCities(cities: List<Area>?) {
        handler.sendMessage(Message.obtain(handler, WHAT_CITIES_PROVIDED, cities))
    }

    /**
     * 设置区列表
     *
     * @param countries 区/县列表
     */
    fun setCountries(countries: List<Area>?) {
        handler.sendMessage(Message.obtain(handler, WHAT_COUNTIES_PROVIDED, countries))
    }

    /**
     * 设置街道列表
     *
     * @param streets 街道列表
     */
    fun setStreets(streets: List<Area>?) {
        handler.sendMessage(Message.obtain(handler, WHAT_STREETS_PROVIDED, streets))
    }

    /**
     * 有地址数据的时候,直接设置地址选择器
     *
     * @param provinces     省份列表
     * @param provinceIndex 当前省在列表中的位置
     * @param cities        当前省份的城市列表
     * @param cityIndex     当前城市在列表中的位置
     * @param countries     当前城市的区县列表
     * @param countyIndex   当前区县在列表中的位置
     */
    fun setAddressSelector(provinces: List<Area>, provinceIndex: Int, cities: List<Area>, cityIndex: Int, countries: List<Area>?, countyIndex: Int) {
        if (provinces == null || provinces.size == 0) {
            return
        } else if (cities == null || cities.size == 0) {
            setProvinces(provinces, provinceIndex)
        } else {
            setProvinces(provinces, provinceIndex)
            setCities(cities, cityIndex)
            setCountries(countries, countyIndex)
        }
        refreshSelector()
    }

    /**
     * 隐藏loading
     */
    fun hideLoading() {
        progressBar!!.visibility = View.GONE
    }

    private fun setProvinces(provinces: List<Area>?, position: Int) {
        if (provinces == null || provinces.size == 0) {
            return
        }
        this.provinces = provinces
        tabIndex = INDEX_TAB_PROVINCE
        provinceIndex = position
        val province = this.provinces!![position]
        textViewProvince!!.text = province.name
        listView!!.adapter = provinceAdapter
        if (provinceIndex != INDEX_INVALID) {
            listView!!.setSelection(provinceIndex)
        }
    }

    private fun setCities(cities: List<Area>?, position: Int) {
        if (cities == null || cities.size == 0) {
            return
        }
        this.cities = cities
        tabIndex = INDEX_TAB_CITY
        cityIndex = position
        val city = this.cities!![position]
        textViewCity!!.text = city.name
        // 缓存省-市数据
        val provinceId = cities[0].parentId
        if (!province2city.containsKey(provinceId)) {
            val cityList: MutableList<Area> = ArrayList()
            copy(cities, cityList)
            province2city[provinceId] = cityList
        }
        listView!!.adapter = cityAdapter
        if (cityIndex != INDEX_INVALID) {
            listView!!.setSelection(cityIndex)
        }
    }

    private fun setCountries(countries: List<Area>?, position: Int) {
        if (countries == null || countries.size == 0) {
            return
        }
        counties = countries
        tabIndex = INDEX_TAB_COUNTY
        countyIndex = position
        val county = counties!![position]
        textViewCounty!!.text = county.name
        // 缓存市-区数据
        val cityId = counties!![0].parentId
        if (!city2county.containsKey(cityId)) {
            val countyList: MutableList<Area> = ArrayList()
            copy(counties, countyList)
            city2county[cityId] = countyList
        }
        listView!!.adapter = countyAdapter
        if (countyIndex != INDEX_INVALID) {
            listView!!.setSelection(countyIndex)
        }
    }

    /**
     * 刷新地址选择器
     */
    private fun refreshSelector() {
        progressBar!!.visibility = View.GONE
        updateTabsVisibility()
        updateIndicator()
    }

    class Area {
        @JvmField
        var id: Long = 0

        @JvmField
        var parentId: Long = 0

        @JvmField
        var name: String? = null
    }

    companion object {
        private const val INDEX_TAB_PROVINCE = 0
        private const val INDEX_TAB_CITY = 1
        private const val INDEX_TAB_COUNTY = 2
        private const val INDEX_TAB_STREET = 3
        private const val INDEX_INVALID = -1
        private const val WHAT_PROVINCES_PROVIDED = 0
        private const val WHAT_CITIES_PROVIDED = 1
        private const val WHAT_COUNTIES_PROVIDED = 2
        private const val WHAT_STREETS_PROVIDED = 3
        fun isEmpty(list: List<*>?): Boolean {
            return list == null || list.size == 0
        }

        fun notEmpty(list: List<*>?): Boolean {
            return list != null && list.size > 0
        }

        /**
         * 拷贝list
         */
        fun copy(src: List<Area>?, dest: MutableList<Area>) {
            for (i in src!!.indices) {
                dest.add(src[i])
            }
        }
    }

    init {
        initViews()
        initAdapters()
    }
}