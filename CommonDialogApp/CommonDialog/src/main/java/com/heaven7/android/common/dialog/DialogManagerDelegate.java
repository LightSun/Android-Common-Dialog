package com.heaven7.android.common.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by heaven7 on 2018/6/25 0025.
 */
public interface DialogManagerDelegate{

    /**
     * show dialog of the dialog manager
     * @param context the activity
     */
    void show(FragmentActivity context);

    /**
     * show dialog
     * @param context the context
     * @param argument the arguments
     */
    void show(FragmentActivity context, Bundle argument);

    /**
     * dismiss dialog
     */
    void dismiss();
}
