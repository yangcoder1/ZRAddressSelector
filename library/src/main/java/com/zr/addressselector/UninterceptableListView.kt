package com.zr.addressselector

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ListView

class UninterceptableListView : ListView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.onTouchEvent(ev)
    }
}