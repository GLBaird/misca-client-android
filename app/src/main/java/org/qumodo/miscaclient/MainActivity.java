package org.qumodo.miscaclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.qumodo.R;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "Activity resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Activity Pausing");
    }
}
