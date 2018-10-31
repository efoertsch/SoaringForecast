package com.fisincorporated.soaringforecast.common;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fisincorporated.soaringforecast.R;

import dagger.android.support.DaggerAppCompatActivity;

public abstract class MasterActivity extends DaggerAppCompatActivity {

    protected ActionBar actionBar;
    protected Toolbar toolbar;
    private View rootView;

    protected abstract Fragment createFragment();

    //TODO allow for master/detail or side by side fragments for large screens
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        rootView = findViewById(R.id.task_activity_content);

        // implement this in superclass?
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            setActionBarBackgroundColorToDefault();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
        if (fragment == null) {
            fragment = createFragment();
            if (fragment != null) {
                fm.beginTransaction().add(R.id.fragmentContainer, fragment)
                        .commit();
            }
            else {
                finish();
            }
        }
    }

    public void displayNewFragment(Fragment fragment){
        FragmentManager fm = getSupportFragmentManager();
        if (fragment != null) {
            fm.beginTransaction().replace(R.id.fragmentContainer, fragment)
                    .commit();
        }

    }

    public void setActionBarBackgroundColorToDefault() {
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }
    }

    public void setActivityTitle(@StringRes int stringRes) {
        setActivityTitle(getString(stringRes));
    }

    public void setActivityTitle(String title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    public void displayFragment(Fragment fragment, boolean addToBackstack) {
        // Replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment);
        if(addToBackstack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();

    }

    public void showSnackBarMessage(String message){
        Snackbar.make(rootView,message, Snackbar.LENGTH_INDEFINITE).show();
    }
}
