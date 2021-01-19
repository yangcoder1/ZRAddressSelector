package com.zr.demo

import android.content.Context
import android.widget.Toast

/**
 * Created by zr on 2017/2/5.
 */
object ToastUtils {
    /** Show short Toast.
     *
     * @see Toast.makeText
     * @see Toast.LENGTH_SHORT
     */
    fun showShort(context: Context?, text: CharSequence?) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}