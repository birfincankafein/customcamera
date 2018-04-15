package com.birfincankafein.customcamera;

import android.animation.LayoutTransition;
import android.support.annotation.Nullable;
import android.transition.ChangeTransform;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * Created by metehantoksoy on 6.04.2018.
 */

public class AnimationUtil {
    private final static float DEFAULT_DEGREE = 90;
    private final static int DEFAULT_DURATION = 150;

    public static void startShakeRotateAnimation(View v) {
        AnimationUtil.startShakeRotateAnimation(v, null);
    }

    public static void startShakeRotateAnimation(View v, @Nullable final AnimationListener listener) {
        AnimationUtil.startShakeRotateAnimation(v, DEFAULT_DEGREE, listener);
    }

    public static void startShakeRotateAnimation(View v, float degree, @Nullable final AnimationListener listener) {
        startShakeRotateAnimation(v, degree, DEFAULT_DURATION, listener);
    }

    public static void startShakeRotateAnimation(View v, float degree, int duration, @Nullable final AnimationListener listener) {
        // Initialize a new ChangeTransform transition instance
        ChangeTransform changeTransform = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            changeTransform = new ChangeTransform();
            // Set the duration for transition
            changeTransform.setDuration(duration);
            // Set the transition interpolator
            changeTransform.setInterpolator(new AccelerateInterpolator());
            if (listener != null) {
                changeTransform.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {
                        listener.onStart();
                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        listener.onEnd();
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
            }
            // Begin the delayed transition
            TransitionManager.beginDelayedTransition((ViewGroup) v.getParent(), changeTransform);
            // Toggle rotation state
            v.setRotation(degree);
        } else {
            RotateAnimation rotate = new RotateAnimation(0, degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(duration);
            if (listener != null) {
                rotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        listener.onStart();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        listener.onEnd();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
            rotate.setInterpolator(new LinearInterpolator());
            v.startAnimation(rotate);
        }
    }

    public interface AnimationListener {
        void onStart();
        void onEnd();
    }
}
