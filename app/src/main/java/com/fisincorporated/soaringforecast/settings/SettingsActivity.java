package com.fisincorporated.soaringforecast.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fisincorporated.soaringforecast.R;


// Cribbed various code
// http://alvinalexander.com/android/android-tutorial-preferencescreen-preferenceactivity
// -preferencefragment
// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android
// -preference-in-the-preference-su/4325239#4325239
public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new
                SettingsPreferenceFragment()).commit();
    }

}
