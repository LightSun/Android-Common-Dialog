package com.heaven7.android.common.dialog;

import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;


/**
 * Created by heaven7 on 2017/3/27 0027.
 */

public final class DialogFragmentHelper {

    /**
     * show loading dialog
     * @param context the context
     * @param callback the callback.
     * @return the dialog fragment
     */
    public static CommonDialogFragment show(FragmentActivity context, @LayoutRes int layoutId,
                                                   CommonDialogFragment.Callback callback){
        return new CommonDialogFragment.Builder()
                .layoutId(layoutId)
                .callback(callback)
                .build()
                .show(context, callback.getClass().getName());
    }
}
