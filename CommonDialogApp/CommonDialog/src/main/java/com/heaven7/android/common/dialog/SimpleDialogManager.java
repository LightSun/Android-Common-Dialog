package com.heaven7.android.common.dialog;

import android.support.v4.app.FragmentActivity;

/**
 * simple dialog manager
 * @author heaven7
 */
public abstract class SimpleDialogManager extends AbstractTranslateDialogCallback implements DialogManagerDelegate {

    @Override
    public void show(FragmentActivity context) {
        new CommonDialogFragment.Builder()
                .layoutId(getLayoutId())
                .callback(this)
                .build()
                .show(context, getClass().getSimpleName());
    }

    @Override
    public void dismiss() {
        CommonDialogFragment cdf = getDialogFragment();
        if (cdf != null) {
            cdf.dismiss();
        }
    }

    protected abstract int getLayoutId();
}