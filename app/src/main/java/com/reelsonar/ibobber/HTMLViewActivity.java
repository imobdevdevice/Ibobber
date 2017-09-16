// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.reelsonar.ibobber.drawer.DeviceDrawerFragment;
import com.reelsonar.ibobber.drawer.HomeDrawerFragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class HTMLViewActivity extends Activity {

    private final static String TAG = "HTMLViewActivity";

    static private View mFragmentView;
    static private boolean mUseTerm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_view);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new HTMLViewFragment())
                    .add(R.id.container, new DeviceDrawerFragment())
                    .add(R.id.container, new HomeDrawerFragment(), "HomeDrawer")
                    .commit();
        }

        Intent intent = getIntent();
        mUseTerm = intent.getBooleanExtra("term", false);

        ((BobberApp)getApplication()).getGaTracker().enableAutoActivityTracking(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String screenName = mUseTerm ? "Settings Terms of Use" : "Settings Privacy";
        Tracker tracker = ((BobberApp)getApplication()).getGaTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();

        ((BobberApp)getApplication()).getGaTracker().enableAutoActivityTracking(true);
    }

    //---------------------------------------------------------------------------------------------
    // loadText
    //
    // Loads one of the html files and places the contents into a scrollable textView
    //
    // This is real fast but transparent text requires the html to have the background transparance
    // set to 0 to work.
    //
    // This done buy putting this in the html
    //     <body style="display: flex; background-color:transparent">some content</body>
    //---------------------------------------------------------------------------------------------

    static public void loadText(final boolean showTerms) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(final Void... params) {
                AssetManager assetManager = BobberApp.getContext().getAssets();

                InputStream inputStream;
                try {
                    if ( showTerms ) {
                        inputStream = assetManager.open("terms.html");
                    } else {
                        inputStream = assetManager.open("privacy.html");
                    }

                    return readTextFile(inputStream);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading webview", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final String s) {
                if (s != null) {
                    WebView webView = (WebView) mFragmentView.findViewById(R.id.webView1);
                    webView.setBackgroundColor(0);
                    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    webView.loadData(s, "text/html", "UTF-8");
                }
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
        }
        return outputStream.toString();
    }

    static public class HTMLViewFragment extends PreferenceFragment {

        public HTMLViewFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_html_view, container, false);

            mFragmentView = rootView;
            loadText(mUseTerm);

            return rootView;
        }

     }

}
