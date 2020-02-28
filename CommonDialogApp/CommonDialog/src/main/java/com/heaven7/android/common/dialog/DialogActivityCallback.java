package com.heaven7.android.common.dialog;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * the dialog activity callback.
 * @author heaven7
 */
/*public*/ class DialogActivityCallback implements Application.ActivityLifecycleCallbacks {

    private FragmentActivity activity;
    private FragmentManager fm;
    private CommonDialogFragment cdf;
    private String tag;

    public DialogActivityCallback(FragmentActivity activity, FragmentManager fm, CommonDialogFragment cdf, String tag) {
        this.activity = activity;
        this.cdf = cdf;
        this.tag = tag;
    }

    public void register(){
        this.activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
         if(this.activity == activity){
             this.activity.getApplication().unregisterActivityLifecycleCallbacks(this);
             cdf.show(this.activity, fm, tag);
         }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
