package com.heaven7.android.common.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

/**
 * have anim
 * Created by heaven7 on 2017/1/17.
 */

public class CommonDialog extends Dialog {

    private static final String TAG = "CommonDialog";
    private static final boolean DEBUG = true;

    private static final String KEY_POPUP_STYLE = "h7:CommonDialog:popup_style";
    private static final String KEY_AUTO_DISMISS = "h7:CommonDialog:auto_dismiss";
    private static final String KEY_ANIMATE_ROOT = "h7:CommonDialog:animate_root";

    private final WeakHandler mHandler = new WeakHandler<CommonDialog>(Looper.getMainLooper(), this) {};
    /**
     * the anim of show dialog
     */
    private BaseAnimator mEnterAnim ;
    private WeakReference<View> mContentView;

    /**
     * is the popup style like {@link android.widget.PopupWindow}
     */
    private boolean mIsPopupStyle;
    /**
     * is performing the animate
     */
    private boolean mPerformingAnim;
    /**
     * if >0 means support auto dismiss.
     */
    private long mAutoDismissDelay;
    /**
     * true to animate the DecorView.
     */
    private boolean mAnimateOnRootView;

    private Callback mCallback;

    /**
     * the callback help we handle something.
     */
    public static abstract class Callback {
        /**
         * called in {@link Dialog#onAttachedToWindow()} or the onAnimationEnd of enter animation.
         *
         * @param view the content view, not the view of android.R.id.content.
         */
        public void afterShow(View view){

        }

        /**
         * called before {@link Dialog#dismiss()} ()}.
         *
         * @param view the content view not the view of android.R.id.content.
         */
        public void beforeDismiss(View view){

        }

        /**
         * called before enter animation
         * @param view the content view
         */
        public void beforeEnterAnimation(View view) {

        }
        /** called if activity want to listen event on OUTSIDE. */
        public boolean canActivityReceiveEventOnOutside() {
            return false;
        }
    }

    public CommonDialog(Context context) {
        super(context);
    }

    public CommonDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CommonDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
           // Logger.i(TAG, "onAttachedToWindow", ""); //called every show
        }
        //register OnKeyListener if need
        if(mCallback instanceof OnKeyListener){
            setOnKeyListener((OnKeyListener) mCallback);
        }

        final View view = getAnimateView();
        if (mEnterAnim != null) {
            if(mCallback != null){
                mCallback.beforeEnterAnimation(view);
            }
            mEnterAnim.listener(new InternalAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (!applyAutoDismiss()) {
                        dispatchAfterShow();
                    }
                }
            }).playOn(getAnimateView(), getWindow() != null
                    ? getWindow().getAttributes() : null);
        }  else {
            dispatchAfterShow();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
          //  Logger.i(TAG, "onDetachedFromWindow", "");
        }
    }

    @Override
    public void dismiss() {
        dismissWithoutAnimation();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        findRealView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        findRealView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        findRealView();
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_OUTSIDE){
            if(mCallback.canActivityReceiveEventOnOutside()){
                Activity activity = getOwnerActivity();
                if(activity != null){
                    return activity.dispatchTouchEvent(ev);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        final Bundle bundle = super.onSaveInstanceState();
        if (mIsPopupStyle) {
            bundle.putBoolean(KEY_POPUP_STYLE, true);
        }
        if (mAutoDismissDelay != 0) {
            bundle.putLong(KEY_AUTO_DISMISS, mAutoDismissDelay);
        }
        if (mAnimateOnRootView) {
            bundle.putBoolean(KEY_ANIMATE_ROOT, true);
        }
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if(savedInstanceState.getBoolean(KEY_POPUP_STYLE)){
            mIsPopupStyle = true;
        }
        if(savedInstanceState.getBoolean(KEY_ANIMATE_ROOT)){
            mAnimateOnRootView = true;
        }
        mAutoDismissDelay = savedInstanceState.getLong(KEY_AUTO_DISMISS , 0);
        super.onRestoreInstanceState(savedInstanceState);
    }

    //==================================================================//
    private void findRealView() {
        if (getWindow() != null) {
            View view = getWindow().getDecorView();
            mContentView = new WeakReference<View>(((ViewGroup) view.findViewById(android.R.id.content)).getChildAt(0));
        }
    }

    /*private*/ View getAnimateView() {
        View realView = mContentView != null ? mContentView.get() : null;
        if (mAnimateOnRootView || realView == null) {
            return getWindow() != null ? getWindow().getDecorView() : null;
        } else {
            return realView;
        }
    }

    /*private*/ View getRealContentView() {
        return mContentView != null ? mContentView.get() : null;
    }

    private void dispatchAfterShow() {
        if (mCallback != null) {
            mCallback.afterShow(getRealContentView());
        }
    }

    /**
     * @return true if auto dismiss success.
     */
    private boolean applyAutoDismiss() {
        if (mAutoDismissDelay > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismissWithoutAnimation();
                }
            }, mAutoDismissDelay);
            return true;
        }
        return false;
    }

    public void dismissWithoutAnimation() {
        try {
            super.dismiss();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            mContentView = null;
        }
    }
    //==============================================================

    public CommonDialog autoDismiss(long delay) {
        this.mAutoDismissDelay = delay;
        return this;
    }

    public CommonDialog animateOnDecorView(boolean useDecorView) {
        this.mAnimateOnRootView = useDecorView;
        return this;
    }

    public CommonDialog withEnterAnimator(BaseAnimator animator) {
        this.mEnterAnim = animator;
        return this;
    }
    public CommonDialog dimEnabled(boolean isDimEnabled) {
        if (isDimEnabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        return this;
    }

    public CommonDialog callback(Callback callback) {
        this.mCallback = callback;
        return this;
    }

    public CommonDialog popupStyle(boolean enable) {
        mIsPopupStyle = enable;
        return this;
    }

    public void showAtLocation(int gravity, int x, int y) {
        if (mIsPopupStyle) {
            Window window = getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            window.setGravity(gravity);
            params.x = x;
            params.y = y;
        }
        show();
    }
    public void showAtLocation(int x, int y) {
        showAtLocation(Gravity.START | Gravity.TOP, x, y);
    }

    private class InternalAnimatorListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            mPerformingAnim = true;
        }
        @Override
        public void onAnimationEnd(Animator animation) {
            mPerformingAnim = false;
        }
        @Override
        public void onAnimationCancel(Animator animation) {
            mPerformingAnim = false;
        }
    }

}
