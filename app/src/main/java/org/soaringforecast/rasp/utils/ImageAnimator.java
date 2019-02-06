package org.soaringforecast.rasp.utils;

import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

public class ImageAnimator {
    /**
     * @param startIndex - starting value in ValueAnimator
     * @param numberPeriods  ending value in ValueAnimator.
     * @param duration   seconds to complete 1 interaction
     * @param repeatCount  eg ValueAnimator.INFINITE
     * @return
     */
    public static ValueAnimator getInitAnimator(int startIndex , int numberPeriods, int duration, int repeatCount){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startIndex, numberPeriods);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.setRepeatCount(repeatCount);
        return valueAnimator;
    }
}
