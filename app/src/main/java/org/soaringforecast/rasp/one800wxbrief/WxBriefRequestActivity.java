package org.soaringforecast.rasp.one800wxbrief;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.soaringforecast.rasp.common.MasterActivity;

import androidx.fragment.app.Fragment;

public class WxBriefRequestActivity extends MasterActivity {

    private static final String TASK_ID = "TASK_ID";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment createFragment() {
        long taskId = getIntent().getExtras().getLong(TASK_ID, -1);
        return getWxBriefRequestFragment(taskId);
    }

    private Fragment getWxBriefRequestFragment(long taskId) {
        return   WxBriefRequestFragment.newInstance(taskId);
    }

    public static class Builder {

        private Bundle bundle;

        private Builder() {
            bundle = new Bundle();
        }

        public static WxBriefRequestActivity.Builder getBuilder() {
            return new WxBriefRequestActivity.Builder();
        }


        public WxBriefRequestActivity.Builder setWxBriefForTaskId(long taskId) {
            bundle.putLong(TASK_ID, taskId);
            return this;
        }


        public Intent build(Context context) {
            Intent intent = new Intent(context, WxBriefRequestActivity.class);
            intent.putExtras(bundle);
            return intent;
        }
    }

}