package com.heaven7.android.common.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.lang.ref.WeakReference;

/**
 * common dialog fragment
 * Created by heaven7 on 2017/1/16.
 */

public class CommonDialogFragment extends DialogFragment {

    private static final String TAG = CommonDialogFragment.class.getSimpleName();
    private static final String KEY_LAYOUT_ID = "h7:CommonDialogFragment:layout_id";

    private static final DisplayMetrics DM = new DisplayMetrics();

    private Callback mCallback;
    private int mLayoutId;

    public static Builder newBuilder() {
        return new Builder();
    }
    @Override
    public void onStart() {
        super.onStart();
        //Logger.i(TAG, "onStart", ""); // called before dialog.onAttachedToWindow.
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(DM);
        final Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mCallback.onSetWindow(window, DM);
        }
        mCallback.onSetDialog(getDialog());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLayoutId = savedInstanceState.getInt(KEY_LAYOUT_ID);
        }
        if(mCallback.isFullScreen()){
            setStyle(DialogFragment.STYLE_NORMAL, Build.VERSION.SDK_INT >= 21 ?
                    android.R.style.Theme_Material_Light_NoActionBar_Fullscreen : android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        }else {
            setStyle(DialogFragment.STYLE_NO_TITLE, Build.VERSION.SDK_INT >= 21 ?
                    android.R.style.Theme_Material_Dialog_NoActionBar : android.R.style.Theme_Light_NoTitleBar);
        }
        mCallback.onRestoreInstanceState(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(mLayoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCallback.onBindData(getContext(), view, getArguments());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_LAYOUT_ID, mLayoutId);

        mCallback.onSaveInstanceState(outState);
    }
    /**
     * {@inheritDoc}
     * <p> if you call this from on activity result. please use handler to send a delay message to show. or else may cause bug.</p>
     * @param activity The activity
     * @param tag The tag for this fragment, as per
     * {@link FragmentTransaction#add(Fragment, String) FragmentTransaction.add}.
     */
    public void show(FragmentActivity activity, String tag){
        try {
            show(activity.getSupportFragmentManager(), tag);
        }catch (IllegalStateException e){
            //this exception often caused by show dialog onActivityResult
            new DialogActivityCallback(activity, this, tag).register();
        }
    }
    /**
     * {@inheritDoc}
     * <p> if you call this from on activity result. please use handler to send a delay message to show. or else may cause bug.</p>
     * @param manager The FragmentManager this fragment will be added to.
     * @param tag The tag for this fragment, as per
     * {@link FragmentTransaction#add(Fragment, String) FragmentTransaction.add}.
     */
    //this method just called internal
    @Override
    public void show(FragmentManager manager, String tag) {
        /*
         *in extreme case, manager may have been destroy so just check it.
         */
        if(manager.isDestroyed()){
            return;
        }
        super.show(manager, tag);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCallback.attachDialogFragment(this);
        CommonDialog dialog = new CommonDialog(getContext(), getTheme())
                .callback(mCallback)
                .withEnterAnimator(mCallback.isAnimationEnabled() ? mCallback.getEnterAnimator():null);
        //for execute animate in dialog.dismiss() have bugs. so can't execute animate in dialog. just in fragment
              //  .withExitAnimator(mCallback.getExitAnimator());
        dialog.setOnKeyListener(mCallback);
        return dialog;
    }

    @Override
    public void dismiss() {
        CommonDialog dialog = (CommonDialog) getDialog();
        if(dialog == null){
            //Logger.w(TAG, "dismiss", "dialog = null !!!");
            dismissInternal();
            return;
        }
        if (mCallback != null) {
            mCallback.beforeDismiss(dialog.getRealContentView());
        }
        View animateView = dialog.getAnimateView();
        BaseAnimator mExitAnim = mCallback.isAnimationEnabled() ? mCallback.getExitAnimator(): null;
        if (animateView == null) {
            dismissInternal();
        }else{
            if(mExitAnim != null){
                mExitAnim.listener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dismissInternal();
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        dismissInternal();
                    }
                }).playOn(animateView, dialog.getWindow() != null
                        ? dialog.getWindow().getAttributes() : null);
            }else{
                dismissInternal();
            }
        }
    }

    private void dismissInternal(){
        if(getHostFragmentManager() == null){
            return;
        }
        //if not resume. dismiss allow state loss.
        if(!isResumed()){
            dismissAllowingStateLoss();
            return;
        }
        super.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //super.onDismiss(dialog);
        /*
         * sometimes we need manual remove fragment. eg screen off.
         * or else may cause bug when screen on (eg: show dialog double).
         */
        final FragmentManager fm = getHostFragmentManager();
        if(fm != null){
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(this);
            ft.commitAllowingStateLoss();
        }
        mCallback.onDismiss(this, dialog);
    }

    @Override
    public void onDestroyView() {
        //Logger.i(TAG, "onDestroyView", ""); //called after dialog#dismiss.
        Dialog dialog = getDialog();
        if (dialog != null) {
            if (getRetainInstance()) {
                dialog.setDismissMessage(null);
            }
        }
        super.onDestroyView();
    }

    public FragmentManager getHostFragmentManager() {
        FragmentManager fm = getFragmentManager();
        if (fm == null && isAdded() && getActivity() != null ) {
            fm = getActivity().getSupportFragmentManager();
        }
        return fm;
    }
    /**
     * the callback of CommonDialogFragment.
     */
    public static abstract class Callback extends CommonDialog.Callback implements DialogInterface.OnKeyListener{

        private boolean mAnimationEnabled = true;
        private WeakReference<CommonDialogFragment> mWeakDF;

        /**
         * called on create the dialog
         * @param cdf the common dialog fragment.
         */
        /*public*/ void attachDialogFragment(CommonDialogFragment cdf){
            this.mWeakDF = new WeakReference<CommonDialogFragment>(cdf);
        }

        public void dismiss() {
            CommonDialogFragment cdf = getDialogFragment();
            if (cdf != null) {
                cdf.dismiss();
                mWeakDF.clear();
            }
        }
        /**
         * get the activity which attach to dialog fragment
         * @return the activity
         */
        public FragmentActivity getActivity(){
            return getDialogFragment().getActivity();
        }

        /**
         * get resource
         * @return the resource
         */
        public Resources getResources(){
            return getActivity().getResources();
        }

        /**
         * get the dialog fragment. if attached return a valid object. or else return null.
         * @return the dialog fragment
         */
        public CommonDialogFragment getDialogFragment(){
            return mWeakDF.get();
        }

        /**
         * indicate the animation is enabled or not
          * @return true if animation enabled
         */
        public boolean isAnimationEnabled() {
            return mAnimationEnabled;
        }

        /**
         * set animation enabled or not
         * @param mAnimationEnabled true if enable
         */
        public void setAnimationEnabled(boolean mAnimationEnabled) {
            this.mAnimationEnabled = mAnimationEnabled;
        }

        /**
         * called on save instance state
         * @param outState the out state
         */
        protected void onSaveInstanceState(Bundle outState) {

        }

        /**
         * called on restore instance state
         * @param savedInstanceState the saved state. may be null
         */
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
        }

        /**
         * called on dialog dismiss
         * @param cdf the dialog fragment
         * @param dialog the dialog interface
         */
        protected void onDismiss(CommonDialogFragment cdf, DialogInterface dialog) {
        }

        /**
         * get the enter animator
         * @return the enter animator. can be null
         */
        @Nullable
        protected BaseAnimator getEnterAnimator(){
            return null;
        }
        /**
         * get the exit animator
         * @return the exit animator. can be null
         */
        @Nullable
        protected BaseAnimator getExitAnimator(){
            return null;
        }

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_BACK){
                if(onBackPressed()){
                    return true;
                }
                Activity activity = getDialogFragment().getActivity();
                if(activity != null && activity.dispatchKeyEvent(event)){
                    return true;
                }
            }
            return false;
        }

        /**
         * called on back pressed. this is called before activity callback.
         * @return true handled back pressed.
         */
        protected boolean onBackPressed() {
            return false;
        }

        /**
         * indicate the dialog will show as full screen or not.
         * @return true if full screen. false otherwise. default is false.
         */
        protected boolean isFullScreen(){
            return false;
        }

        /**
         * set the dialog, called when create the dialog
         * you can see a sample {@linkplain SimpleDialogCallback}
         * @param dialog the dialog, often is an instance of {@link CommonDialog}.
         */
        public abstract void onSetDialog(Dialog dialog);

        /**
         * called on start which give a last chance to set Window.
         * you can see a sample {@linkplain SimpleDialogCallback}
         * @param window the window from dialog
         * @param dm     the DisplayMetrics
         */
        public abstract void onSetWindow(Window window, DisplayMetrics dm);

        /**
         * bind the data for the view which is the content of fragment
         *
         * @param context   the context
         * @param view      the view of dialog.
         * @param arguments the arguments which is set by calling {@link Fragment#setArguments(Bundle)}.
         */
        public abstract void onBindData(Context context, View view, Bundle arguments);

    }

    public static class Builder {
        private int layoutId;
        private boolean retain;
        private Callback callback;
        private Bundle args;
        private CommonDialogFragment mCDF;

        public Builder layoutId(@LayoutRes int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        public Builder retain(boolean retain) {
            this.retain = retain;
            return this;
        }

        public Builder callback(Callback callback) {
            this.callback = callback;
            return this;
        }
        public Builder arguments(Bundle args) {
            this.args = args;
            return this;
        }
        public Builder build() {
            if (callback == null) {
                throw new IllegalStateException("callback can't be null ! you must set callback first.");
            }
            if (layoutId <= 0) {
                throw new IllegalStateException("layoutId must > 0 ! you must set layoutId first.");
            }
            CommonDialogFragment fragment = new CommonDialogFragment();
            fragment.setRetainInstance(retain);
            fragment.mLayoutId = layoutId;
            fragment.mCallback = callback;
            fragment.setArguments(args);
            mCDF = fragment;
            return this;
        }
        public CommonDialogFragment show(FragmentActivity activity, String tag){
            mCDF.show(activity, tag);
            return mCDF;
        }
    }

}
