package com.fisincorporated.soaringforecast.windy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.common.MasterActivity;



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
            WindyActivity.Builder builder = new WindyActivity.Builder();
            return builder;
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, WindyActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }
}
