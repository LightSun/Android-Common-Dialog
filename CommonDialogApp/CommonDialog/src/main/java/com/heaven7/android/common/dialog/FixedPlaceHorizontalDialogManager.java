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
 * the fixed place(the left and top position ) horizontal dialog manager
 * Created by heaven7 on 2018/10/8 0008.
 */
public abstract class FixedPlaceHorizontalDialogManager extends SimpleDialogManager
        implements LocationHelper.Callback {

    private final boolean mCanReceiveEventOut;
    private final float dimAmount; //-1 means default.
    private int x, y;
    private View anchor;
    private int mLocationOffset;
    private int mRetainSpace;

    public FixedPlaceHorizontalDialogManager(boolean mCanReceiveEventOut, float dimAmount) {
        this.mCanReceiveEventOut = mCanReceiveEventOut;
        this.dimAmount = dimAmount;
    }
    public void setAnchorView(View anchor){
        this.anchor = anchor;
    }
    public void setLocationOffset(int mLocationOffset) {
        this.mLocationOffset = mLocationOffset;
    }

    /**
     * the retain space of left-right or top-bottom
     * @param mRetainSpace the retain space
     */
    public void setRetainSpace(int mRetainSpace) {
        this.mRetainSpace = mRetainSpace;
    }
    @Override
    public final void onBindData(Context context, View view, Bundle arguments) {
        onBindDataImpl(context, view, arguments);
        //locate
        view.measure(0, 0);
        new LocationHelper.Builder()
                .setPlaceView(view)
                .setMarkView(getMarkView())
                .setAutoFitEdge(true)
                .setCallback(this)
                .setLocate(LocationHelper.LOCATE_TOP)
                .setLocateOffset(mLocationOffset)
                .setGravity(LocationHelper.GRAVITY_CENTER)
                .setRetainSpace(mRetainSpace)
                .build()
                .applyHorizontal(anchor);
    }

    @Override
    public void onSetDialog(Dialog dialog) {
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public void onSetWindow(Window window, DisplayMetrics dm) {
        if(mCanReceiveEventOut) {
            // Make us non-modal, so that others can receive touch events.
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            // ...but notify us that it happened.
            window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.x = x;
        wlp.y = y;
        wlp.gravity = Gravity.TOP | Gravity.START;
        if(dimAmount >= 0){
            wlp.dimAmount = dimAmount;
        }
        window.setAttributes(wlp);
    }
    @Override
    public final boolean canActivityReceiveEventOnOutside() {
        return mCanReceiveEventOut;
    }
    @Override
    public final void applyLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
    @Override
    public void applyMarkMargin(View markView, int marginLeft) {
        throw new UnsupportedOperationException();
    }

    /**
     * get the mark view which used to mark the horizontal dialog.
     * @return the mark view
     */
    protected View getMarkView(){
        return null;
    }

    /**
     * called on bind data
     * @param context the context
     * @param view the view
     * @param arguments the arguments from dialog fragment
     */
    protected abstract void onBindDataImpl(Context context, View view, Bundle arguments);
}
