package com.heaven7.android.common.dialog;

import android.support.v4.app.FragmentActivity;

/**
 * Created by heaven7 on 2018/6/25 0025.
 */
public interface DialogManagerDelegate{

    void show(FragmentActivity context);

    void dismiss();

    abstract class SimpleDialogManager extends DialogFragmentHelper.DialogTranslateCallback implements DialogManagerDelegate {

        private static final String TAG = "SimpleDialogManager";
        private CommonDialogFragment mCdf;

        @Override
        public void show(FragmentActivity context) {
            mCdf = new CommonDialogFragment.Builder()
                    .layoutId(getLayoutId())
                    .callback(this)
                    .show(context, getClass().getSimpleName());
        }
        @Override
        public void dismiss() {
            if (mCdf != null && mCdf.isAdded()) {
                mCdf.dismiss();
                mCdf = null;
            }
        }
        protected abstract int getLayoutId();
    }
}
