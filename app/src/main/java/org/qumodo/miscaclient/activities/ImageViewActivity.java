package org.qumodo.miscaclient.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.fragments.QImageViewFragment;
import org.qumodo.services.QTCPSocketService;

public class ImageViewActivity extends Activity {

    public static final String INTENT_IMAGE_PATH = "org.qumodo.miscaclient.ImageViewActivity.IntentImagePath";
    public static final String INTENT_IMAGE_ID = "org.qumodo.miscaclient.ImageViewActivity.IntentImageID";
    public static final String INTENT_SERVICE = "org.qumodo.miscaclient.ImageViewActivity.IntentService";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                    return;
            switch (action) {
                case QTCPSocketService.DELEGATE_SOCKET_CLOSED:
                    Intent startupActivity = new Intent(ImageViewActivity.this, StartupActivity.class);
                    startupActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startupActivity);

                    finish();
                    break;
            }
        }
    };

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        String imageID = getIntent().getStringExtra(INTENT_IMAGE_ID);
        String imagePath = getIntent().getStringExtra(INTENT_IMAGE_PATH);
        int service = getIntent().getIntExtra(INTENT_SERVICE, 0);

        Fragment fragment = QImageViewFragment.newInstance(imagePath, service, imageID);
        fragment.setRetainInstance(true);
        getFragmentManager()
                .beginTransaction()
                .add(R.id.image_view_fragment_container, fragment, QImageViewFragment.TAG)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QTCPSocketService.DELEGATE_SOCKET_CLOSED);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

}
