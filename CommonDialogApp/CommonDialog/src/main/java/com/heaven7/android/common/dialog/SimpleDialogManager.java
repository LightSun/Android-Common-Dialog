package com.heaven7.android.common.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * simple dialog manager
 * @author heaven7
 */
public abstract class SimpleDialogManager extends AbstractTranslateDialogCallback implements DialogManagerDelegate {

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

    protected abstract int getLayoutId();
}