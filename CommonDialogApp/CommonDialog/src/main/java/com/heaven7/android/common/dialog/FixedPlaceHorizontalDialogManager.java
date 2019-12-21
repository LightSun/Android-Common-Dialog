package com.heaven7.android.common.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * the fixed place(the left and top position ) horizontal dialog manager
 * Created by heaven7 on 2018/10/8 0008.
 */
public abstract class FixedPlaceHorizontalDialogManager extends FixedPlaceDialogManager {

    public FixedPlaceHorizontalDialogManager(LocationHelper.Builder builder, View anchor, boolean mCanReceiveEventOut, float dimAmount) {
        super(builder, anchor, mCanReceiveEventOut, dimAmount);
    }
    public FixedPlaceHorizontalDialogManager(LocationHelper.Builder builder, View anchor) {
        super(builder, anchor);
    }

    public FixedPlaceHorizontalDialogManager(LocationHelper.Builder builder, View anchor, float dimAmount) {
        super(builder, anchor, dimAmount);
    }

    /**
     * get the mark view which used to mark the horizontal dialog.
     * @return the mark view
     */
    protected View getMarkView(){
        return null;
    }
    @Override
    protected void apply(LocationHelper lh, View anchor) {
        lh.applyHorizontal(anchor);
    }

    /**
     * called on bind data
     * @param context the context
     * @param view the view
     * @param arguments the arguments from dialog fragment
     */
    protected abstract void onBindDataImpl(Context context, View view, Bundle arguments);
}
