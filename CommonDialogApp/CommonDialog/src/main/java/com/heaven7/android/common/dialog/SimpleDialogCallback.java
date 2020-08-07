package com.heaven7.android.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;

/**
 * the simple dialog callback
 * @author heaven7
 */
public abstract class SimpleDialogCallback extends CommonDialogFragment.Callback {

    @Override
    protected int getWidth(DisplayMetrics dm) {
        return dm.widthPixels * 4 / 5;
    }
    @Override
    protected int getGravity() {
        return Gravity.CENTER;
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