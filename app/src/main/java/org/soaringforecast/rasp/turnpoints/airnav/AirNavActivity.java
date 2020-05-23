package org.soaringforecast.rasp.turnpoints.airnav;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.common.MasterActivity;

import androidx.fragment.app.Fragment;

public class AirNavActivity extends MasterActivity {

    private static final String AIRPORT_CODE = "AIRPORT_CODE";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setActivityTitle(R.string.airnav);
        }

        @Override
        protected Fragment createFragment(){
            String airportCode = getIntent().getExtras().getString(AIRPORT_CODE);
            return  AirNavFragment.newInstance(airportCode);
        }

        public static class Builder {
            private Bundle bundle;

            private Builder() {
                bundle = new Bundle();
            }

            public static AirNavActivity.Builder getBuilder() {
                return new AirNavActivity.Builder();
            }

            public AirNavActivity.Builder setAirportCode(String airportCode){
                bundle.putString(AIRPORT_CODE, airportCode);
                return this;
            }

            public Intent build(Context context) {
                Intent intent = new Intent(context, AirNavActivity.class);
                intent.putExtras(bundle);
                return intent;
            }
        }
}
