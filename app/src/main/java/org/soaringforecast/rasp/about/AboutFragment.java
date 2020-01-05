package org.soaringforecast.rasp.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.databinding.AboutView;
import org.soaringforecast.rasp.repository.AppRepository;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.support.DaggerFragment;

public class AboutFragment extends DaggerFragment {

    @Inject
    public AppRepository appRepository;

    private AboutViewModel aboutViewModel;


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
        AboutView aboutView = DataBindingUtil.inflate(inflater, R.layout.about_soaring_weather_fragment, container, false);
        aboutView.setLifecycleOwner(this);
        aboutView.setViewModel(aboutViewModel);
        return aboutView.getRoot();
    }

}
