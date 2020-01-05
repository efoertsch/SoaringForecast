package org.soaringforecast.rasp.windy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.MasterActivity;

import androidx.fragment.app.Fragment;



public class WindyActivity extends MasterActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityTitle(R.string.windy);
    }

    protected Fragment createFragment(){
        return new WindyFragment();
    }

    public static class Builder {
        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static WindyActivity.Builder getBuilder() {
            return new WindyActivity.Builder();
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, WindyActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
