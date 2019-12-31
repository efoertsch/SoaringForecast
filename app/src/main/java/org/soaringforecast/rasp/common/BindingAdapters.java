package org.soaringforecast.rasp.common;

import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;


// Currently used to set error msg in TextInputLayout
public class BindingAdapters {
    @BindingAdapter("app:errorText")
    public static void setErrorMessage(TextInputLayout view, String errorMessage) {
        view.setError(errorMessage);
    }
}
