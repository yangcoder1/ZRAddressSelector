package com.zr.addressselector

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.WindowManager
import com.zr.addressselector.AddressSelector.OnAddressSelectedListener

class BottomSelectorDialog : Dialog {
    var selector: AddressSelector? = null
        private set

    constructor(context: Context) : super(context, R.style.bottom_dialog) {
        init(context)
    }

    constructor(context: Context, themeResId: Int) : super(context, themeResId) {
        init(context)
    }

    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener) {
        init(context)
    }

    private fun init(context: Context) {
        selector = AddressSelector(context)
        setContentView(selector!!.view)
        val window = window
        val params = window.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = dp2px(context, 256f)
        window.attributes = params
        window.setGravity(Gravity.BOTTOM)
    }

    fun setOnAddressSelectedListener(listener: OnAddressSelectedListener?) {
        selector!!.onAddressSelectedListener = listener
    }

    override fun dismiss() {
        super.dismiss()
        selector!!.clearCacheData()
    }

    companion object {
        fun dp2px(context: Context, dipValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dipValue * scale + 0.5f).toInt()
        }

        @JvmOverloads
        fun show(context: Context, listener: OnAddressSelectedListener? = null): BottomSelectorDialog {
            val dialog = BottomSelectorDialog(context, R.style.bottom_dialog)
            dialog.selector!!.onAddressSelectedListener = listener
            dialog.show()
            return dialog
        }
    }
}