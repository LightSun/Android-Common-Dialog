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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * common dialog fragment
 * Created by heaven7 on 2017/1/16.
 */

public class CommonDialogFragment extends DialogFragment {

    private static final String TAG = "CommonDialogFragment";
    private static final String KEY_LAYOUT_ID = "h7::CDF:layout_id";
    private static final String KEY_PARAMETER  = "h7::CDF:param";
    private static final String KEY_CALLBACK_CLASS  = "h7::CDF:cb";

    private static final DisplayMetrics DM = new DisplayMetrics();
    //private static LeakyStorage<Callback> sStorage;

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
            if(mCallback != null){
                mCallback.onSetWindow(window, DM);
            }
        }
        if(mCallback != null){
            mCallback.onSetDialog(getDialog());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLayoutId = savedInstanceState.getInt(KEY_LAYOUT_ID);
            String cn = savedInstanceState.getString(KEY_CALLBACK_CLASS);
            try {
                Class<?> clazz = Class.forName(cn);
                try {
                    mCallback = (Callback) clazz.getConstructor().newInstance();
                }catch (Exception e){
                    mCallback = (Callback) clazz.getConstructor(FragmentActivity.class).newInstance(getActivity());
                }
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d(TAG, "create callback from savedInstanceState failed.");
                //Log.d(TAG, "create callback from savedInstanceState failed. use storage to restore callback.");
                //mCallback = getStorage().restore(savedInstanceState);
            }
            if(mCallback != null){
                DialogParameter dp = savedInstanceState.getParcelable(KEY_PARAMETER);
                mCallback.setDialogParameter(dp);
                mCallback.onRestoreInstanceState(savedInstanceState);
            }
        }
        if(mCallback != null && mCallback.isFullScreen()){
            setStyle(DialogFragment.STYLE_NORMAL, Build.VERSION.SDK_INT >= 21 ?
                    android.R.style.Theme_Material_Light_NoActionBar_Fullscreen : android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        }else {
            setStyle(DialogFragment.STYLE_NO_TITLE, Build.VERSION.SDK_INT >= 21 ?
                    android.R.style.Theme_Material_Dialog_NoActionBar : android.R.style.Theme_Light_NoTitleBar);
        }

        FragmentActivity activity = getActivity();
        if(activity != null){
            activity.getLifecycle().addObserver(new LifecycleObserver() {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroy(){
                    if(isStateSaved()){
                        dismissInternal();
                    }
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(mLayoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mCallback != null){
            mCallback.onBindData(getContext(), view, getArguments(), savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_LAYOUT_ID, mLayoutId);
        if(mCallback != null){
            mCallback.onSaveInstanceState(outState);
            outState.putParcelable(KEY_PARAMETER, mCallback.mParameter);
            outState.putString(KEY_CALLBACK_CLASS, mCallback.getClass().getName());
            /*if(!canCreateCallback()){
                getStorage().save(mCallback, outState);
            }*/
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     * <p> if you call this from on activity result. please use handler to send a delay message to show. or else may cause bug.</p>
     * @param activity The activity
     * @param tag The tag for this fragment, as per
     * {@link FragmentTransaction#add(Fragment, String) FragmentTransaction.add}.
     */
    public void show(FragmentActivity activity, String tag){
        show(activity, null, tag);
    }
    /**
     * {@inheritDoc}
     * <p> if you call this from on activity result. please use handler to send a delay message to show. or else may cause bug.</p>
     * @param activity The activity
     * @param tag The tag for this fragment, as per
     * {@link FragmentTransaction#add(Fragment, String) FragmentTransaction.add}.
     * @since 1.0.2
     */
    public void show(FragmentActivity activity, FragmentManager fm, String tag){
        if(fm == null){
            fm = activity.getSupportFragmentManager();
        }
        try {
            show(fm, tag);
        }catch (IllegalStateException e){
            //this exception often caused by show dialog onActivityResult
            new DialogActivityCallback(activity, fm, this, tag).register();
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
        if(mCallback == null){
            return super.onCreateDialog(savedInstanceState);
        }
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
        if (animateView == null || mCallback == null) {
            dismissInternal();
        }else{
            BaseAnimator mExitAnim = mCallback.isAnimationEnabled() ? mCallback.getExitAnimator(): null;
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
        if(mCallback != null){
            mCallback.onDismiss(this, dialog);
        }
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
     * return true if can create callback from class name.
     * */
    private boolean canCreateCallback(){
        Class<? extends Callback> clazz = mCallback.getClass();
        if(!Modifier.isPublic(clazz.getModifiers())){
            return false;
        }
        Constructor constructor = null;
        try {
            constructor = clazz.getConstructor();
        }catch (Exception e){
            try {
                constructor = clazz.getConstructor(FragmentActivity.class);
            } catch (NoSuchMethodException ex) {
            }
        }
        return constructor != null;
    }
   /* private static LeakyStorage<Callback> getStorage(){
        if(sStorage == null){
            sStorage = new LeakyStorage<>();
        }
        return sStorage;
    }*/
    /**
     * the callback of CommonDialogFragment.
     */
    public static abstract class Callback extends CommonDialog.Callback implements DialogInterface.OnKeyListener{

        private transient WeakReference<CommonDialogFragment> mWeakDF;
        private DialogParameter mParameter = new DialogParameter();

        /**
         * set dialog parameter
         * @param parameter the dialog parameter
         * @since 1.0.3
         */
        public void setDialogParameter(DialogParameter parameter){
            if(parameter == null){
                throw new NullPointerException();
            }
           this.mParameter = parameter;
        }

        /**
         * get the dialog parameter
         * @return the dialog parameter
         * @since 1.0.3
         */
        public DialogParameter getDialogParameter(){
            return mParameter;
        }
        /**
         * indicate can receive event out side or not
         * @return true if can receive
         * @since 1.0.2
         */
        public boolean canActivityReceiveEventOnOutSide() {
            return mParameter.isActivityReceiveEventOnOutSide();
        }

        /**
         * set can activity receive event on out side or not
         * @param canReceiveEventOnOutSide can activity receive on out side.
         * @since 1.0.2
         */
        public void setCanActivityReceiveEventOnOutSide(boolean canReceiveEventOnOutSide) {
            mParameter.setActivityReceiveEventOnOutSide(canReceiveEventOnOutSide);
        }
        /**
         * get dim amount .
         * @return dim amount
         * @since 1.0.2
         * @see android.view.WindowManager.LayoutParams#dimAmount
         */
        public float getDimAmount() {
            return mParameter.getDimAmount();
        }
        /**
         * set dim amount .
         * @param dimAmount the dim amount
         * @since 1.0.2
         * @see android.view.WindowManager.LayoutParams#dimAmount
         */
        public void setDimAmount(float dimAmount) {
            mParameter.setDimAmount(dimAmount);
        }

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
        @Override
        public final boolean canActivityReceiveEventOnOutside() {
            return mParameter.isActivityReceiveEventOnOutSide();
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
            if(mWeakDF == null){
                return null;
            }
            return mWeakDF.get();
        }

        /**
         * indicate the animation is enabled or not
          * @return true if animation enabled
         */
        public boolean isAnimationEnabled() {
            return mParameter.isAnimationEnabled();
        }

        /**
         * set animation enabled or not
         * @param mAnimationEnabled true if enable
         */
        public void setAnimationEnabled(boolean mAnimationEnabled) {
            mParameter.setAnimationEnabled(mAnimationEnabled);
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
         * <h2>this will be removed </h2>
         * indicate save state is enabled or not.
         * @return this
         * @since 1.0.1
         */
        @Deprecated
        protected boolean isSaveStateEnabled(){
            return false;
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
         * <p>Use {@linkplain #getHeight(Context, DisplayMetrics)} instead</p>
         * get window layout height
         * @param context the context
         * @return the layout height
         * @since 1.0.2
         */
        @Deprecated
        protected int getWindowLayoutHeight(Context context) {
            return WindowManager.LayoutParams.WRAP_CONTENT;
        }

        /**
         * get the dialog height
         * @param context the context
         * @return the dialog window height
         * @since 1.0.3
         */
        protected int getHeight(Context context, DisplayMetrics dm) {
            return getWindowLayoutHeight(context);
        }
        /**
         * get window layout width actually
         * @param dm the display metrics
         * @return the dialog width
         * @since 1.0.3
         */
        protected int getWidth(Context context,DisplayMetrics dm) {
            return getWidth(dm);
        }
        /**
         * <p>Use {@linkplain #getWidth(Context, DisplayMetrics)} instead</p>
         * get window layout width actually
         * @param dm the display metrics
         * @return the layout width
         * @since 1.0.2
         */
        @Deprecated
        protected int getWidth(DisplayMetrics dm) {
            return dm.widthPixels;
        }

        /**
         * get the gravity
         * @return the gravity
         * @since 1.0.2
         */
        protected int getGravity() {
            return Gravity.BOTTOM;
        }

        /**
         * called on start which give a last chance to set Window.
         * you can see a sample {@linkplain SimpleDialogCallback}
         * @param window the window from dialog
         * @param dm     the DisplayMetrics
         */
        public void onSetWindow(Window window, DisplayMetrics dm) {
            if (canActivityReceiveEventOnOutside()) {
                // Make us non-modal, so that others can receive touch events.
                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                // ...but notify us that it happened.
                window.setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
            }
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.width = getWidth(getActivity(), dm);
            wlp.height = getHeight(getActivity(), dm);
            wlp.gravity = getGravity();
            if (this.getDimAmount() >= 0) {
                wlp.dimAmount = this.getDimAmount(); // default is 0.6f
            }
            window.setAttributes(wlp);
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
         * bind the data for the view which is the content of fragment
         *
         * @param context   the context
         * @param view      the view of dialog.
         * @param arguments the arguments which is set by calling {@link Fragment#setArguments(Bundle)}.
         */
        public abstract void onBindData(Context context, View view, Bundle arguments);

        /**
         * bind the data for the view which is the content of fragment
         *
         * @param context   the context
         * @param view      the view of dialog.
         * @param arguments the arguments which is set by calling {@link Fragment#setArguments(Bundle)}.
         * @param saveState the save state. may be null
         * @since 1.0.3
         */
        public void onBindData(Context context, View view, Bundle arguments, @Nullable Bundle saveState){
            onBindData(context, view, arguments);
        }

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
        public CommonDialogFragment show(FragmentActivity activity, FragmentManager fm, String tag){
            mCDF.show(activity, fm, tag);
            return mCDF;
        }
    }

}
