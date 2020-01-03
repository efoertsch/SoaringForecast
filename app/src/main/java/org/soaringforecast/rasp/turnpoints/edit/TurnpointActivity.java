package org.soaringforecast.rasp.turnpoints.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.soaringforecast.rasp.common.MasterActivity;

public class TurnpointActivity extends MasterActivity {

    private static final String TURNPOINT_ID = "TURNPOINT_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected Fragment createFragment() {
        
        return getTurnpointEditFragment();
    }

    @Nullable
    private Fragment getTurnpointEditFragment() {
        long turnpointID = getIntent().getExtras().getLong(TURNPOINT_ID);
        return  TurnpointEditFragment.newInstance(turnpointID);
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static TurnpointActivity.Builder getBuilder() {
            return new TurnpointActivity.Builder();
        }

        public Intent build(Context context) {
            Intent intent = new Intent(context, TurnpointActivity.class);
            intent.putExtras(bundle);
            return intent;
        }

        public TurnpointActivity.Builder editTurnpoint(long turnpointId) {
            bundle.putLong(TURNPOINT_ID, turnpointId);
            return this;
        }
    }
}
