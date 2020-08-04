package org.soaringforecast.rasp.landout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.soaringforecast.rasp.common.MasterActivity;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LandoutActivity extends MasterActivity {
    @Override
    protected Fragment createFragment() {
        return LandoutFragment.newInstance();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static class Builder {
        private Builder() {
        }

        public static LandoutActivity.Builder getBuilder() {
            return new LandoutActivity.Builder();
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, LandoutActivity.class);
            return intent;
        }

    }
}
