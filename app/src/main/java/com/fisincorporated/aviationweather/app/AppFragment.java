package com.fisincorporated.aviationweather.app;

import android.content.Context;
import android.support.v4.app.Fragment;

public class AppFragment extends Fragment {

    protected DataLoading dataLoading = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() instanceof DataLoading)
            dataLoading = (DataLoading) getActivity();
    }

}


