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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

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
        if(mCallback.isFullScreen()){
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        }else {
            setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Material_Dialog_NoActionBar);
        }
        if (savedInstanceState != null) {
            mLayoutId = savedInstanceState.getInt(KEY_LAYOUT_ID);
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
        mCallback.onBindData(getContext(), view, getArguments(), new ActionProvider() {
            @Override
            public void dismissDialog() {
                dismiss();
            }
        });
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
                .withEnterAnimator(mCallback.isEnableAnim() ? mCallback.getEnterAnimator():null);
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
        BaseAnimator mExitAnim = mCallback.isEnableAnim() ? mCallback.getExitAnimator(): null;
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
     * the action provider help we handle the dialog
     */
    public static abstract class ActionProvider {

        /**
         * dismiss the dialog. if you want to dismiss , please call this.
         */
        public abstract void dismissDialog();

    }

    /**
     * the callback of CommonDialogFragment.
     */
    public static abstract class Callback extends CommonDialog.Callback implements DialogInterface.OnKeyListener{

        private boolean enableAnim = true;
        private WeakReference<CommonDialogFragment> mWeakDF;

        /**
         * called on create the dialog
         * @param cdf the common dialog fragment.
         */
        /*public*/ void attachDialogFragment(CommonDialogFragment cdf){
            this.mWeakDF = new WeakReference<CommonDialogFragment>(cdf);
        }
        public FragmentActivity getActivity(){
            return getDialogFragment().getActivity();
        }
        public Resources getResources(){
            return getActivity().getResources();
        }
        public CommonDialogFragment getDialogFragment(){
            return mWeakDF.get();
        }

        public boolean isEnableAnim() {
            return enableAnim;
        }
        public void setEnableAnim(boolean enableAnim) {
            this.enableAnim = enableAnim;
        }

        protected void onSaveInstanceState(Bundle outState) {

        }

        protected void onRestoreInstanceState(Bundle savedInstanceState) {

        }
        protected void onDismiss(CommonDialogFragment cdf, DialogInterface dialog) {

        }

        @Nullable
        protected BaseAnimator getEnterAnimator(){
            return null;
        }
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
                //dispatch back to activity.
                if(activity != null && activity.dispatchKeyEvent(event)){
                    return true;
                }
            }
            return false;
        }

        protected boolean onBackPressed() {
            return false;
        }

        protected boolean isFullScreen(){
            return false;
        }

        /**
         * set the dialog, called when create the dialog
         *
         * @param dialog the dialog, often is an instance of {@link CommonDialog}.
         */
        public abstract void onSetDialog(Dialog dialog);

        /**
         * called on start which give a last chance to set Window.
         *
         * @param window the window from dialog
         * @param dm     the DisplayMetrics
         */
        public abstract void onSetWindow(Window window, DisplayMetrics dm);

        /**
         * bind the data for the view which is the content of fragment
         *
         * @param context   the context
         * @param view      the view of dialog.
         * @param provider  the action provider . help we handle dialog .such as {@link ActionProvider#dismiss()}.
         * @param arguments the arguments which is set by calling {@link Fragment#setArguments(Bundle)}.
         */
        public abstract void onBindData(Context context, View view, Bundle arguments, ActionProvider provider);

    }

    public abstract static class SimpleCallback extends Callback {

        @Override
        public void onSetWindow(Window window, DisplayMetrics dm) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.width = dm.widthPixels * 4 / 5;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }

        @Override
        public void onSetDialog(Dialog dialog) {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }

        @Override
        public void beforeDismiss(View view) {
            // Logger.i(TAG, "beforeDismiss", "" + view);
        }

        @Override
        public void afterShow(View view) {
            //Logger.i(TAG, "afterShow", "" + view);
        }

        @Override
        public void onBindData(Context context, View view, Bundle arguments, ActionProvider provider) {

        }
    }

    public static class Builder {
        private int layoutId;
        private boolean retain;
        private Callback callback;
        private Bundle args;

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

        private CommonDialogFragment build() {
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
            return fragment;
        }

        public CommonDialogFragment show(FragmentManager fm, String tag) {
            //final CommonDialogFragment mFragment = this.mFragment;
            CommonDialogFragment fragment = build();
            if (fragment == null) {
                throw new IllegalStateException("you must call build() first");
            }
            fragment.show(fm, tag);
            return fragment;
        }

        public CommonDialogFragment show(FragmentActivity activity, String tag) {
            return show(activity.getSupportFragmentManager(), tag);
        }
    }

}
