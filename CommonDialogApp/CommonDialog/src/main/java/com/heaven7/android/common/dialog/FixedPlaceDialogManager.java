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
public abstract class FixedPlaceDialogManager extends SimpleDialogManager
        implements LocationHelper.Callback {

    private final LocationHelper.Builder builder;
    private final View anchor;
    private int x, y;

    public FixedPlaceDialogManager(LocationHelper.Builder builder,View anchor) {
        this.builder = builder;
        this.anchor = anchor;
    }

    @Override
    public final void onBindData(Context context, View view, Bundle arguments) {
        onBindDataImpl(context, view, arguments);
        //locate
        view.measure(0, 0);
        LocationHelper helper = builder
                .setPlaceView(view)
                .setMarkView(getMarkView())
                .setAutoFitEdge(true)
                .setCallback(this)
                .build();
                /*.setLocate(LocationHelper.LOCATE_TOP)
                .setLocateOffset(mLocationOffset)
                .setGravity(LocationHelper.GRAVITY_CENTER)
                .setRetainSpace(mRetainSpace)*/
        apply(helper, anchor);
    }

    @Override
    public void onSetDialog(Dialog dialog) {
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
    }

    @Override
    public final void onSetWindow(Window window, DisplayMetrics dm) {
        if(canActivityReceiveEventOnOutside()) {
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
        if(getDimAmount() >= 0){
            wlp.dimAmount = getDimAmount();
        }
        window.setAttributes(wlp);
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
     * apply anchor to the location helper. you should call {@linkplain LocationHelper#applyHorizontal(View)}
     * or {@linkplain LocationHelper#applyVertical(View)} (View)}.
     * @param lh the location helper
     * @param anchor the anchor
     */
    protected abstract void apply(LocationHelper lh, View anchor);
    /**
     * called on bind data
     * @param context the context
     * @param view the view
     * @param arguments the arguments from dialog fragment
     */
    protected abstract void onBindDataImpl(Context context, View view, Bundle arguments);
}
