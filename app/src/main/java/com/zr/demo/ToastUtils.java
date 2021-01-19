package com.zr.demo;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by zr on 2017/2/5.
 */

public class ToastUtils {

    /** Show short Toast.
     *
     *  @see Toast#makeText(Context, CharSequence, int)
     *  @see Toast#LENGTH_SHORT
     */
    public static void showShort(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
