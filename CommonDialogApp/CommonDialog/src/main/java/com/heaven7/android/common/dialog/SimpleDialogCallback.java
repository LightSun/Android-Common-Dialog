package com.heaven7.android.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * the simple dialog callback
 * @author heaven7
 */
public abstract class SimpleDialogCallback extends CommonDialogFragment.Callback {

    @Override
    public void onSetWindow(Window window, DisplayMetrics dm) {
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = dm.widthPixels * 4 / 5;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
    }

    @Override
    public void onSetDialog(Dialog dialog) {
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void beforeDismiss(View view) {
        // Logger.i(TAG, "beforeDismiss", "" + view);
    }

    @Override
    public void afterShow(View view) {
        //Logger.i(TAG, "afterShow", "" + view);
    }

    @Override
    public void onBindData(Context context, View view, Bundle arguments) {

    }
}