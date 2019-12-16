package com.heaven7.android.common.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

/**
 * the base animator. design from network.
 * @author heaven7
 */
public abstract class BaseAnimator {

    private final AnimatorSet mAnimator = new AnimatorSet();
    private Animator.AnimatorListener listener;
    private Interpolator interpolator;
    private long mDuration = 250;
    private long delay;

    /**
     * called on set animation for animator
     * @param set the set animators
     * @param view the view to animate. often is content view of dialog
     * @param constraintWidth the width
     * @param constraintHeight the height
     */
    public abstract void setAnimation(AnimatorSet set, View view, int constraintWidth, int constraintHeight);

    private void startImpl(final View view, int constraintWidth, int constraintHeight) {
        reset(view);
        if (constraintWidth <= 0 || constraintHeight <= 0) {
            if (view.getMeasuredWidth() == 0) {
                //not show. the height must measure ourselves.
                view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            }
            constraintWidth = view.getMeasuredWidth();
            constraintHeight = view.getMeasuredHeight();
        }
        setAnimation(mAnimator, view, constraintWidth, constraintHeight);
        //set param
        mAnimator.setDuration(mDuration);
        if (interpolator != null) {
            mAnimator.setInterpolator(interpolator);
        }
        if (delay > 0) {
            mAnimator.setStartDelay(delay);
        }
        if (listener != null) {
            mAnimator.addListener(listener);
        }
        mAnimator.start();
    }

    protected void reset(View view) {
        // ViewHelper.setPivotX(view, view.getMeasuredWidth() / 2.0f);
        // ViewHelper.setPivotY(view, view.getMeasuredHeight() / 2.0f);
        view.setAlpha(1);
        view.setScaleX(1);
        view.setScaleY(1);
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setRotation(0);
        view.setRotationY(0);
        view.setRotationX(0);
    }

    public BaseAnimator duration(long duration) {
        this.mDuration = duration;
        return this;
    }

    public BaseAnimator delay(long delay) {
        this.delay = delay;
        return this;
    }

    public BaseAnimator interpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public BaseAnimator listener(Animator.AnimatorListener listener) {
        this.listener = listener;
        return this;
    }

    public void playOn(View view) {
        playOn(view, 0, 0);
    }

    public void playOn(View view, @Nullable ViewGroup.LayoutParams lp) {
        if (lp == null) {
            playOn(view);
        } else {
            playOn(view, lp.width, lp.height);
        }
    }

    public void playOn(View view, int constraintWidth, int constraintHeight) {
        startImpl(view, constraintWidth, constraintHeight);
    }

}
