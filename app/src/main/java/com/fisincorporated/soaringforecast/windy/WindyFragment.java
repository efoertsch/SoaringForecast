package com.fisincorporated.soaringforecast.windy;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.fisincorporated.soaringforecast.R;
import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.common.Constants;
import com.fisincorporated.soaringforecast.databinding.WindyView;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.repository.TaskTurnpoint;
import com.fisincorporated.soaringforecast.task.TaskActivity;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.support.DaggerFragment;

public class WindyFragment extends DaggerFragment {

    @Inject
    @Named("appWindyUrl")
    String appWindyUrl;

    @Inject
    AppPreferences appPreferences;

    @Inject
    AppRepository appRepository;

    private WindyViewModel windyViewModel;
    private WindyView windyView;
    private WebView webView;
    private MenuItem clearTaskMenuItem;
    private boolean showClearTaskMenuItem;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        windyViewModel = ViewModelProviders.of(this)
                .get(WindyViewModel.class)
                .setAppPreferences(appPreferences)
                .setAppRepository(appRepository);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        windyView = DataBindingUtil.inflate(inflater, R.layout.fragment_windy, container, false);
        setupViews();


        return windyView.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupViews() {
        webView = windyView.fragmentWindyWebview;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Uri.parse(url).getHost().equals(appWindyUrl)) {
                    // This is app related url so ignore
                    return false;
                }
                // Otherwise, start browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                setObservers();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(windyViewModel, "android");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // prevent popups and new windows (but do not override the onCreateWindow() )
        webView.getSettings().setSupportMultipleWindows(true);

        Button testButton = windyView.fragmentWindyTestButton;
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windyViewModel.getTask();
            }
        });

        webView.loadUrl(appWindyUrl);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.windy_menu, menu);
        clearTaskMenuItem = menu.findItem(R.id.forecast_menu_clear_task);
        if (clearTaskMenuItem != null) {
            clearTaskMenuItem.setVisible(showClearTaskMenuItem);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.forecast_menu_select_task:
                selectTask();
                return true;
            case R.id.forecast_menu_clear_task:
                removeTaskTurnpoints();
                windyViewModel.setTaskId(-1);
                displayTaskClearMenuItem(false);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeTaskTurnpoints() {
        //TODO remove points from Windy overlay
    }

    private void displayTaskClearMenuItem(boolean visible) {
        showClearTaskMenuItem = visible;
        getActivity().invalidateOptionsMenu();
    }


    public void drawLine(double fromLat, double fromLong, double toLat, double toLong) {
        String command = "javascript:drawLine( " + fromLat + "," + fromLong
                + "," + toLat + "," + toLong + ")";
        executeJavaScriptCommand(command);
    }

    public void setObservers() {
        windyViewModel.getCommand().observe(this, command -> {
            // execute javascript command
            executeJavaScriptCommand(command);
        });

        windyViewModel.getTaskTurnpoints().observe(this, taskTurnpoints -> {
            plotTask(taskTurnpoints);

        });
    }

    private void plotTask(List<TaskTurnpoint> taskTurnpoints) {
        Gson gson = new Gson();
        String taskJson = gson.toJson(taskTurnpoints);
        String command = new StringBuilder().append("javascript:").append("drawTask(")
                .append(taskJson).append(")").toString();
        executeJavaScriptCommand(command);

    }

    private void executeJavaScriptCommand(String command) {
        webView.evaluateJavascript(command, null);
    }

    private void selectTask() {
        TaskActivity.Builder builder = TaskActivity.Builder.getBuilder();
        builder.displayTaskList().enableClickTask(true);
        startActivityForResult(builder.build(this.getContext()), 999);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle;
        if (requestCode == 999 && data != null) {
            if ((bundle = data.getExtras()) != null) {
                long taskId = bundle.getLong(Constants.SELECTED_TASK);
                if (taskId != 0) {
                    windyViewModel.setTaskId(taskId);
                }
            }
        }
    }

}
