package com.fisincorporated.soaringforecast.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.fisincorporated.soaringforecast.R;


/**
 * Need to define in attrs.xml
 * <resources>
 * <declare-styleable name="ProportionalImageView">
 * <attr name="adjustDimension" format="enum">
 * <enum name="width" value="0"/>
 * <enum name="height" value="1"/>
 * </attr>
 * </declare-styleable>
 * </resources>
 */
public class ProportionalImageView extends AppCompatImageView {

    private int adjustDimension = -1;

    public ProportionalImageView(Context context) {
        super(context);
    }

    public ProportionalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getAdjustDimension(context, attrs);
    }

    public void getAdjustDimension(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ProportionalImageView, 0, 0);
        try {
            adjustDimension = a.getInteger(R.styleable.ProportionalImageView_adjustDimension, -1);
        } finally {
            a.recycle();
        }
    }

    public ProportionalImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w;
        int h;

        Drawable d = getDrawable();
        if (d != null) {
            if (adjustDimension == 1) {
                w = MeasureSpec.getSize(widthMeasureSpec);
                h = w * d.getIntrinsicHeight() / d.getIntrinsicWidth();
                setMeasuredDimension(w, h);
            } else if (adjustDimension == 0) {
                h = MeasureSpec.getSize(heightMeasureSpec);
                w = h * d.getIntrinsicWidth() / d.getIntrinsicHeight();
                setMeasuredDimension(w, h);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getAdjustDimension() {
        return adjustDimension;
    }

    public void setAdjustDimension(int adjustDimension) {
        this.adjustDimension = adjustDimension;
        invalidate();
        requestLayout();
    }
}