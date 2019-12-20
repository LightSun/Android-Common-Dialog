package com.heaven7.android.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;

/**
 * common loading callback
 * @author heaven7
 */
public class CommonLoadingCallback extends CommonDialogFragment.Callback {

    private final boolean cancelableOnBack;

    public CommonLoadingCallback(boolean cancelableOnBack) {
        this.cancelableOnBack = cancelableOnBack;
    }

    @Override
    public void onSetDialog(Dialog dialog) {
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(cancelableOnBack);
    }

    @Override
    public void onSetWindow(Window window, DisplayMetrics dm) {

    }

    @Override
    public void onBindData(Context context, View view, Bundle arguments) {

    }
}