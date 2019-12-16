package com.heaven7.android.common.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * dialog translate callback. auto trans in and out.
 * @author heaven7
 */
public abstract class AbstractTranslateDialogCallback extends CommonDialogFragment.Callback {

    private final boolean receiveEventOnOutSide;
    private final float dimAmount; //-1 means default
    protected DialogAnimateCallback enterAnimateCallback;
    protected DialogAnimateCallback exitAnimateCallback;


    public AbstractTranslateDialogCallback() {
        this(false, -1);
    }

    public AbstractTranslateDialogCallback(boolean receiveEventOnOutSide, float dimAmount) {
        this.receiveEventOnOutSide = receiveEventOnOutSide;
        this.dimAmount = dimAmount;
    }

    public DialogAnimateCallback getEnterAnimateCallback() {
        return enterAnimateCallback;
    }

    public void setEnterAnimateCallback(DialogAnimateCallback enterAnimateCallback) {
        this.enterAnimateCallback = enterAnimateCallback;
    }

    public DialogAnimateCallback getExitAnimateCallback() {
        return exitAnimateCallback;
    }

    public void setExitAnimateCallback(DialogAnimateCallback exitAnimateCallback) {
        this.exitAnimateCallback = exitAnimateCallback;
    }

    @Override
    public boolean canActivityReceiveEventOnOutside() {
        return true;
    }

    protected int getWindowLayoutHeight(Context context) {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public void onSetWindow(Window window, DisplayMetrics dm) {
        if (receiveEventOnOutSide) {
            // Make us non-modal, so that others can receive touch events.
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            // ...but notify us that it happened.
            window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        }
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = getWidth(dm);
        wlp.height = getWindowLayoutHeight(getActivity());
        wlp.gravity = getGravity();
        if (this.dimAmount >= 0) {
            wlp.dimAmount = this.dimAmount; // default is 0.6f
        }
        window.setAttributes(wlp);
    }

    protected int getWidth(DisplayMetrics dm) {
        return dm.widthPixels;
    }

    protected int getGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    public void onSetDialog(Dialog dialog) {
        // dialog.setCancelable(false);
        // dialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected BaseAnimator getEnterAnimator() {
        return new BaseAnimator() {
            @Override
            public void setAnimation(AnimatorSet set, View view, int constraintWidth, int constraintHeight) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", constraintWidth, 0)
                        .setDuration(250);
                if (exitAnimateCallback != null) {
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    AnimateListenerImpl listener = new AnimateListenerImpl(enterAnimateCallback, constraintWidth, 0);
                    animator.addUpdateListener(listener);
                    animator.addListener(listener);
                }
                set.playTogether(animator);
            }
        };
    }

    @Override
    protected BaseAnimator getExitAnimator() {
        return new BaseAnimator() {
            @Override
            public void setAnimation(AnimatorSet set, View view, int constraintWidth, int constraintHeight) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, constraintWidth)
                        .setDuration(250);
                if (exitAnimateCallback != null) {
                    AnimateListenerImpl listener = new AnimateListenerImpl(exitAnimateCallback, 0, constraintWidth);
                    animator.addUpdateListener(listener);
                    animator.addListener(listener);
                }
                set.playTogether(animator);
            }
        };
    }

    private static class AnimateListenerImpl extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        final int start;
        final int end;
        DialogAnimateCallback mCallback;

        AnimateListenerImpl(DialogAnimateCallback mCallback, int start, int end) {
            this.mCallback = mCallback;
            this.start = start;
            this.end = end;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            Number num = (Number) animation.getAnimatedValue();
            float percent = Math.abs(start - num.floatValue()) / Math.abs(start - end);
            if (mCallback != null) {
                mCallback.onUpdate(percent);
            }
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (mCallback != null) {
                mCallback.onStart();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mCallback != null) {
                mCallback.onEnd();
            }
        }
    }

    /**
     * dialog animate callback
     */
    public interface DialogAnimateCallback {
        /**
         * called on animate update.
         *
         * @param percent from [0,1]
         */
        void onUpdate(float percent);

        /**
         * called on start animation
         */
        void onStart();

        /**
         * called on end animation
         */
        void onEnd();
    }
}