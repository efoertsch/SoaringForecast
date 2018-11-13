package com.fisincorporated.soaringforecast.about;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.databinding.AboutView;
import com.fisincorporated.soaringforecast.repository.AppRepository;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class AboutFragment extends DaggerFragment {

    @Inject
    public AppRepository appRepository;

    private AboutViewModel aboutViewModel;
    private AboutView aboutView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutViewModel = ViewModelProviders.of(this)
                .get(AboutViewModel.class)
                .setRepository(appRepository);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        aboutView = DataBindingUtil.inflate(inflater, R.layout.about_soaring_weather_fragment, container, false);
        aboutView.setLifecycleOwner(this);
        aboutView.setViewModel(aboutViewModel);
        return aboutView.getRoot();
    }

}
