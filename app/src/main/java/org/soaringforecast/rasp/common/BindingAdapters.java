package org.soaringforecast.rasp.common;

import androidx.databinding.BindingAdapter;
import com.google.android.material.textfield.TextInputLayout;


// Currently used to set error msg in TextInputLayout
public class BindingAdapters {
    @BindingAdapter("app:errorText")
    public static void setErrorMessage(TextInputLayout view, String errorMessage) {
        view.setError(errorMessage);
    }
}
