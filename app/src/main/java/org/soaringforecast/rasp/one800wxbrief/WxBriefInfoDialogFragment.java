package org.soaringforecast.rasp.one800wxbrief;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.soaringforecast.rasp.R;

public class WxBriefInfoDialogFragment extends DialogFragment {
    private static String TITLE_RES = "TITLE";
    private static String INFOR_RES = "INF";
    public static WxBriefInfoDialogFragment newInstance(@StringRes int  title,  @StringRes int info) {
        WxBriefInfoDialogFragment wxBriefInfoDialogFragment = new WxBriefInfoDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TITLE_RES, title);
        bundle.putInt(INFOR_RES, info);
        wxBriefInfoDialogFragment.setArguments(bundle);
        return wxBriefInfoDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleRes = 0;
        int infoRes = 0;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            titleRes = bundle.getInt(TITLE_RES, -1);
            infoRes = bundle.getInt(INFOR_RES, -1);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.wx_brief_info_layout, null));

        builder.setTitle(titleRes);
        builder.setMessage(titleRes)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       // just close
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
