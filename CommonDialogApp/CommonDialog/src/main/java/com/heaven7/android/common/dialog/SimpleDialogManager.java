package com.heaven7.android.common.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * simple dialog manager
 * @author heaven7
 */
public abstract class SimpleDialogManager extends AbstractTranslateDialogCallback implements DialogManagerDelegate {

    public SimpleDialogManager() {
    }
    public SimpleDialogManager(boolean receiveEventOnOutSide, float dimAmount) {
        super(receiveEventOnOutSide, dimAmount);
    }

    @Override
    public void show(FragmentActivity context) {
        show(context, null);
    }

    @Override
    public void show(FragmentActivity context, Bundle argument) {
        new CommonDialogFragment.Builder()
                .layoutId(getLayoutId())
                .callback(this)
                .arguments(argument)
                .build()
                .show(context, getClass().getSimpleName());
    }

    @Override
    public void show(FragmentActivity context, FragmentManager fm, Bundle argument) {
        new CommonDialogFragment.Builder()
                .layoutId(getLayoutId())
                .callback(this)
                .arguments(argument)
                .build()
                .show(context, fm, getClass().getSimpleName());
    }

    protected abstract int getLayoutId();
}